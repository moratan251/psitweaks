package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramLimits;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.param.ParamMatrix;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 領域型の魔法式オブジェクト（○○ Zone）を置く作動式の基底（試作）。共通の入力は「範囲」（領域行列、省略できる）と
 * 「サイズ」（定数）。行列を省くと、術者（術式の焦点）のいるブロックを中心にした一辺サイズの立方体になる。
 * <p>
 * 範囲は行列の中のブロックのうち、行列の中心のブロックを中心にした一辺サイズのブロックの立方体に入るもの（{@link ZoneArea}、
 * 数え方は演算子: 直方体領域と同じ）。コストと規模はサイズだけで
 * 決まる（術式を組んだ時点で分かるのは定数の大きさだけのため）。同じ種類をいくつも置け、1 人が置ける数はサークルと合わせて
 * {@link SpellGramLimits} の上限まで（超えると古いものから解ける）。
 * 干渉力は効き目に効かず、耐久値にだけ効く。
 * <p>
 * 機体（Sable のサブレベル）に向けて作った範囲は、置く時点の機体の位置と向きで世界の座標へ写してから置く
 * （{@link #projectOutOfSubLevel}）。
 */
public abstract class PieceTrickZoneBase<T extends SpellGramZone> extends PieceTrick {
    /**
     * サイズ（立方体の一辺のブロック数）の上限。範囲は最大 32×32×32 ブロック（2026-11-10 ユーザーの決定）。これより大きいサイズは
     * 上限に切り詰める。演算子: 直方体領域そのものの上限（65）とは別。
     */
    public static final int MAX_SIZE = 32;
    public static final String ERROR_EMPTY_ZONE = "psitweaks.spellerror.zone_empty";
    /** 存続時間（仮、30 秒）。 */
    protected static final int LIFETIME_TICKS = 30 * 20;
    /** 耐久値の固有値（仮）。耐久値 = これ × 置いた時点の事象干渉力。 */
    protected static final float BASE_INTEGRITY = 30.0F;

    private final Class<T> zoneType;
    private SpellParam<MatrixValue> region;
    private SpellParam<Number> size;

    protected PieceTrickZoneBase(Spell spell, Class<T> zoneType) {
        super(spell);
        this.zoneType = zoneType;
    }

    @Override
    public void initParams() {
        addParam(region = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, true, false));
        addParam(size = new ParamNumber(PsitweaksSpellParams.SIZE, SpellParam.GREEN, false, true));
        addZoneParams();
    }

    /** 種類ごとの入力（範囲・大きさのあと）。 */
    protected void addZoneParams() {
    }

    /** 威力の上限（加熱・凍結、仮）。 */
    public static final double MAX_ZONE_POWER = 10.0;

    /** 「威力」の入力（定数、コストを決めるため）を足す。 */
    protected SpellParam<Number> addPowerParam() {
        ParamNumber power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true);
        addParam(power);
        return power;
    }

    /** 術式を組んだ時点の威力（正の定数、{@link #MAX_ZONE_POWER} で頭打ち）。 */
    protected double constantPower(SpellParam<Number> power) throws SpellCompilationException {
        Object value = getParamEvaluation(power);
        if (!(value instanceof Number number) || number.doubleValue() <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return Math.min(MAX_ZONE_POWER, number.doubleValue());
    }

    /** 唱えた時点の威力（正、{@link #MAX_ZONE_POWER} で頭打ち）。 */
    protected float powerValue(SpellContext context, SpellParam<Number> power) throws SpellRuntimeException {
        double value = getNotNullParamValue(context, power).doubleValue();
        if (value <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        return (float) Math.min(MAX_ZONE_POWER, value);
    }

    /** 範囲の広さのラベル: サイズ²。 */
    protected static StatLabel areaLabel() {
        return new StatLabel(PsitweaksSpellParams.SIZE, true).square();
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        int area = areaOf(constantSize());
        meta.addStat(EnumSpellStat.POTENCY, potency(area));
        meta.addStat(EnumSpellStat.COST, cost(area));
    }

    /** 範囲の広さ（いちばん広い面のブロック数）から規模。 */
    protected abstract int potency(int area) throws SpellCompilationException;

    /** 範囲の広さから コスト。 */
    protected abstract int cost(int area) throws SpellCompilationException;

    /** 新しい範囲（まだワールドに入れていないもの）。 */
    protected abstract T createZone(ServerLevel level);

    /** 種類ごとの設定（入力の値）。 */
    protected abstract void configure(T zone, SpellContext context) throws SpellRuntimeException;

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int sizeValue = clampSize(getNotNullParamValue(context, size));
        MatrixValue matrix = getParamValue(context, region);
        ZoneArea area;
        if (matrix != null) {
            area = ZoneArea.of(matrix, sizeValue);
        } else if (context.focalPoint != null) {
            // 行列を省いたとき: 術者（術式の焦点）のいるブロックを中心にした、一辺サイズの立方体。
            area = ZoneArea.cube(context.focalPoint.blockPosition(), sizeValue);
        } else {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (area.isEmpty()) {
            throw new SpellRuntimeException(ERROR_EMPTY_ZONE);
        }
        area = projectOutOfSubLevel(context, area);
        BlockPos center = area.center();
        if (!context.isInRadius(new Vector3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5))) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (context.caster == null || !(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }

        T zone = createZone(level);
        zone.configureZone(area, CombatSpellHelper.spellColor(context.caster));
        // 他 mod の機械を動かす速さ（一括洗浄・一括燻製・熱）は置いた時点の事象干渉力で決まる。
        double interference = SpellGramObject.interferenceOf(context.caster);
        zone.setMachineSpeed(SpellGramZone.machineSpeedFor(interference));
        // 領域干渉と比べるための、置いた人の側（自律詠唱機は所有者の UUID を名乗る）と事象干渉力。
        zone.setPlacement(context.caster.getUUID(), interference);
        configure(zone, context);
        SpellGramLimits.makeRoom(level, context.caster);
        zone.setCaster(context.caster);
        zone.setLifetimeTicks(LIFETIME_TICKS);
        zone.setMaxIntegrity(SpellGramIntegrity.initial(BASE_INTEGRITY, context.caster, 1.0D));
        level.addFreshEntity(zone);

        Vec3 at = zone.position();
        level.playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.6F);
        return null;
    }

    /**
     * 範囲が機体（Sable のサブレベル、Create Aeronautics の機体など）の座標で作られていれば（機体に視線を向けたときなど）、置く時点の
     * 機体の位置と向きで世界の座標へ写す（2026-11-11 ユーザーの決定: 方針 A）。写した領域はその場に残り、機体と一緒には動かない
     * （サークルと同じ）。機体の座標のままだと、範囲が見た目の場所から遠く離れ、輪郭も描かれず生き物にも効かなかった。
     */
    private static ZoneArea projectOutOfSubLevel(SpellContext context, ZoneArea area) throws SpellRuntimeException {
        Entity anchor = context.caster != null ? context.caster : context.focalPoint;
        if (anchor == null) {
            return area;
        }
        double[] pose = SableRangeCompat.subLevelPoseAt(anchor.level(), Vec3.atCenterOf(area.center()));
        if (pose == null) {
            return area;
        }
        ZoneArea projected = area.transformedBy(pose);
        return projected.isEmpty() ? area : projected;
    }

    private int constantSize() throws SpellCompilationException {
        Object value = getParamEvaluation(size);
        if (!(value instanceof Number number) || number.doubleValue() <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return clampSize(number);
    }

    private static int clampSize(Number value) {
        return Math.max(1, Math.min(MAX_SIZE, (int) Math.floor(value.doubleValue())));
    }

    /** サイズ {@code size} の範囲のいちばん広い面のブロック数。 */
    public static int areaOf(int size) {
        return size * size;
    }
}
