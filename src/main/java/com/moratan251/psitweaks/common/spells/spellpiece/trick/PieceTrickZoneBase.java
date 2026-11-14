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
 * 数え方は演算子: 直方体領域と同じ）。行列が軸にそっていなければ、行列を囲む軸にそった箱で数え、効く範囲はいつも輪郭の線の
 * とおりにする（{@link ZoneArea#boxed}、2026-11-12 ユーザー）。コストと規模はサイズだけで
 * 決まる（術式を組んだ時点で分かるのは定数の大きさだけのため）。同じ種類をいくつも置け、1 人が置ける数はサークルと合わせて
 * {@link SpellGramLimits} の上限まで（超えると古いものから解ける）。
 * 干渉力は効き目に効かず、耐久値にだけ効く。
 * <p>
 * 「時間」（秒の定数、省略すると {@link #DEFAULT_TIME_SECONDS} 秒、2026-11-11 ユーザー）は存続時間で、置いたときの耐久値と
 * コストも時間に比例する。耐久値は毎 tick 同じ量ずつ減るので、干渉力が同じなら減り方は時間によらず一定で、長く置いた領域ほど
 * 術式解体などに強い。加重領域は入力の空きがないので {@link #hasTimeParam} で外し、30 秒のまま。
 * <p>
 * 機体（Sable のサブレベル）に向けて作った範囲は、置く時点の機体の位置と向きで世界の座標へ写してから置く
 * （{@link #projectOutOfSubLevel}）。
 */
public abstract class PieceTrickZoneBase<T extends SpellGramZone> extends PieceTrick {
    /**
     * サイズ（立方体の一辺のブロック数）の上限。範囲は最大 16×16×16 ブロック（2026-11-12 ユーザーの決定で 32 から変更: コストは面積に
     * 比例し効き目は体積に効くので、大きいほど割安になりすぎる。パーティクルが薄くなる・見て回る負荷も理由）。これより大きいサイズは
     * 上限に切り詰める。演算子: 直方体領域そのものの上限（65）とは別。
     */
    public static final int MAX_SIZE = 16;
    public static final String ERROR_EMPTY_ZONE = "psitweaks.spellerror.zone_empty";
    /** 「時間」を省いたとき（と加重領域）の存続時間（秒）。 */
    public static final int DEFAULT_TIME_SECONDS = 30;
    /** 「時間」の上限（秒、仮）。 */
    public static final int MAX_TIME_SECONDS = 120;
    /** 「時間」の下限（秒）。 */
    public static final int MIN_TIME_SECONDS = 1;
    /** 耐久値の固有値（仮）。耐久値 = これ × 置いた時点の事象干渉力 × 時間 ÷ {@link #DEFAULT_TIME_SECONDS}。 */
    protected static final float BASE_INTEGRITY = 30.0F;

    private final Class<T> zoneType;
    private SpellParam<MatrixValue> region;
    private SpellParam<Number> size;
    private SpellParam<Number> time;

    protected PieceTrickZoneBase(Spell spell, Class<T> zoneType) {
        super(spell);
        this.zoneType = zoneType;
    }

    @Override
    public void initParams() {
        time = null;
        addParam(region = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, true, false));
        addParam(size = new ParamNumber(PsitweaksSpellParams.SIZE, SpellParam.GREEN, false, true));
        addZoneParams();
        if (hasTimeParam()) {
            addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.BLUE, true, true));
        }
    }

    /** 唱えた時点の「範囲」（領域行列、省けば null）。 */
    protected MatrixValue regionValue(SpellContext context) throws SpellRuntimeException {
        return getParamValue(context, region);
    }

    /** 種類ごとの入力（範囲・大きさのあと、時間の前）。 */
    protected void addZoneParams() {
    }

    /** 「時間」の入力を持つか（入力の空きがない加重領域は持たない）。 */
    protected boolean hasTimeParam() {
        return true;
    }

    /** コストの表示: 時間を持つ領域は、種類ごとの式 × 時間 ÷ 30。 */
    @Override
    public void setStatLabel(EnumSpellStat stat, StatLabel label) {
        if (stat == EnumSpellStat.COST && hasTimeParam()) {
            label = label.parenthesize().mul(new StatLabel(SpellParam.GENERIC_NAME_TIME, true)).div(DEFAULT_TIME_SECONDS).ceil();
        }
        super.setStatLabel(stat, label);
    }

    /** 威力の上限（加重領域・領域干渉）。 */
    public static final double MAX_ZONE_POWER = 10.0;
    /**
     * 熱・冷却・FE・風力の総量を持つ領域（加熱・凍結・乱流・荷電）の威力の上限（2026-11-15 ユーザー: 小さい領域でも高いコストを
     * 払えば多くのリソースを扱えるよう、10 より上も入れられる）。10 を超えた分は総量（コスト）だけを増やし、1 体・1 か所あたりの
     * 強さは威力 10 で頭打ち（{@code SpellGramZone.MAX_EFFECT_POWER}）。上限はコストの桁あふれよけで、規模の上限より先には届かない。
     */
    public static final double MAX_RESOURCE_ZONE_POWER = 1000.0;
    /**
     * 威力の下限（2026-11-14 ユーザー）。威力は「広さあたりの強さ＝広さあたりのコスト」を決めるコストのつまみで、効くか効かないか
     * だけの効き目（一括燻製・氷を溶かすなど）を一番安い威力で使えないよう、下限を 1 にする。0 より大きく 1 より小さい威力は 1、
     * 10 より大きい威力は 10 として扱う（0 以下はエラー）。
     */
    public static final double MIN_ZONE_POWER = 1.0;
    /**
     * 威力のある領域の規模の係数（2026-11-14 ユーザー: 終盤なら最大の領域を 2 つ同時に置ける程度）: 規模 = 10 + ⌊広さ × 威力 × 0.27⌋。
     * サイズ 16・威力 10 で 701 になり、プシオニックメタルの CAD（規模 1500）で 2 つ置ける。
     */
    public static final double POWERED_POTENCY_FACTOR = 0.27;
    /** 規模の固定分。 */
    public static final int POTENCY_BASE = 10;

    /** 威力を 1〜10 にそろえる。 */
    public static double clampPower(double power) {
        return Math.max(MIN_ZONE_POWER, Math.min(MAX_ZONE_POWER, power));
    }

    /** 威力のラベル（1〜10 にそろえた値）。 */
    protected static StatLabel powerLabel() {
        return powerLabel(MAX_ZONE_POWER);
    }

    /** 威力のラベル（1〜{@code max} にそろえた値）。 */
    protected static StatLabel powerLabel(double max) {
        return new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_ZONE_POWER).min(max);
    }

    /** このピースの威力の上限（{@link #MAX_ZONE_POWER}、総量を持つ領域は {@link #MAX_RESOURCE_ZONE_POWER}）。 */
    protected double maxPower() {
        return MAX_ZONE_POWER;
    }

    private double clampOwnPower(double power) {
        return Math.max(MIN_ZONE_POWER, Math.min(maxPower(), power));
    }

    /** 威力のある領域の規模: 10 + ⌊広さ × 威力 × 0.27⌋。 */
    public static int poweredPotency(int area, double power) {
        return POTENCY_BASE + (int) Math.floor(area * power * POWERED_POTENCY_FACTOR);
    }

    /** {@link #poweredPotency} のラベル。 */
    protected static StatLabel poweredPotencyLabel() {
        return poweredPotencyLabel(MAX_ZONE_POWER);
    }

    /** {@link #poweredPotency} のラベル（威力の上限 {@code max}）。 */
    protected static StatLabel poweredPotencyLabel(double max) {
        return areaLabel().mul(powerLabel(max)).mul(POWERED_POTENCY_FACTOR).add(POTENCY_BASE).floor();
    }

    /** 「威力」の入力（定数、コストを決めるため）を足す。 */
    protected SpellParam<Number> addPowerParam() {
        ParamNumber power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true);
        addParam(power);
        return power;
    }

    /** 術式を組んだ時点の威力（正の定数、1〜{@link #maxPower()} にそろえる）。 */
    protected double constantPower(SpellParam<Number> power) throws SpellCompilationException {
        Object value = getParamEvaluation(power);
        if (!(value instanceof Number number) || number.doubleValue() <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return clampOwnPower(number.doubleValue());
    }

    /** 唱えた時点の威力（正、1〜{@link #maxPower()} にそろえる）。 */
    protected float powerValue(SpellContext context, SpellParam<Number> power) throws SpellRuntimeException {
        double value = getNotNullParamValue(context, power).doubleValue();
        if (value <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        return (float) clampOwnPower(value);
    }

    /** 範囲の広さのラベル: サイズ²。 */
    public static StatLabel areaLabel() {
        return new StatLabel(PsitweaksSpellParams.SIZE, true).square();
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        int area = areaOf(constantSize());
        meta.addStat(EnumSpellStat.POTENCY, potency(area));
        meta.addStat(EnumSpellStat.COST, timedCost(cost(area), constantSeconds()));
    }

    /** 時間に比例させたコスト: 種類ごとのコスト × 時間 ÷ 30（切り上げ）。 */
    public static int timedCost(int cost, double seconds) {
        return Math.max(1, (int) Math.ceil(cost * seconds / DEFAULT_TIME_SECONDS));
    }

    /** 術式を組んだ時点の時間（秒）。省けば 30、正の定数でなければエラー。 */
    private double constantSeconds() throws SpellCompilationException {
        if (time == null) {
            return DEFAULT_TIME_SECONDS;
        }
        Object value = getParamEvaluation(time);
        if (value == null) {
            return DEFAULT_TIME_SECONDS;
        }
        if (!(value instanceof Number number) || number.doubleValue() <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return clampSeconds(number.doubleValue());
    }

    /** 唱えた時点の時間（秒）。 */
    private double secondsValue(SpellContext context) throws SpellRuntimeException {
        if (time == null) {
            return DEFAULT_TIME_SECONDS;
        }
        Number value = getParamValue(context, time);
        if (value == null) {
            return DEFAULT_TIME_SECONDS;
        }
        if (value.doubleValue() <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        return clampSeconds(value.doubleValue());
    }

    private static double clampSeconds(double seconds) {
        return Math.max(MIN_TIME_SECONDS, Math.min(MAX_TIME_SECONDS, seconds));
    }

    /**
     * 領域の耐久値の上限（作動式: 魔法式延長で戻せる上限）。「時間」によらず、「時間」を最大（{@link #MAX_TIME_SECONDS} 秒）にして
     * 置いたときの耐久値 × {@link SpellGramObject#DEFAULT_INTEGRITY_CAP_FACTOR}（2026-11-14 ユーザー）。
     */
    public static float integrityCap(@org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity caster) {
        return SpellGramIntegrity.initial(BASE_INTEGRITY, caster, (double) MAX_TIME_SECONDS / DEFAULT_TIME_SECONDS)
                * SpellGramObject.DEFAULT_INTEGRITY_CAP_FACTOR;
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
        double seconds = secondsValue(context);
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
        // 効く範囲は輪郭の線のとおり（2026-11-12 ユーザー）: 回した行列や傾いた機体から写した範囲は、輪郭の箱にする。
        area = projectOutOfSubLevel(context, area).boxed();
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
        // 時間に比例して、存続時間・置いたときの耐久値・コストが増える（耐久値の減り方は時間によらない）。
        zone.setLifetimeTicks((int) Math.round(seconds * 20.0));
        zone.setPlacementCost(timedCost(runtimeCost(sizeValue, context), seconds));
        zone.setMaxIntegrity(SpellGramIntegrity.initial(BASE_INTEGRITY, context.caster, seconds / DEFAULT_TIME_SECONDS));
        // 耐久値の上限は「時間」によらず一定（2026-11-14 ユーザー）: 「時間」を最大にして置いたときの上限（その耐久値の 2 倍）。
        zone.setIntegrityCap(integrityCap(context.caster));
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

    /**
     * 置いたときに払ったコストのうち、時間を掛ける前の分（作動式: 魔法式延長のため）。術式を組んだときと同じ式で求める。式の入力が読めないとき（術式の
     * 盤面を持たない GameTest など）は、広さだけで見積もる。
     */
    private int placementCost(int size) {
        return placementCostOf(size);
    }

    private int placementCostOf(int size) {
        try {
            return cost(areaOf(size));
        } catch (SpellCompilationException e) {
            return Math.max(1, areaOf(size));
        }
    }

    /**
     * 置いたときに払ったコストのうち、時間を掛ける前の分を、唱えた時点の入力で求める（術式の盤面の定数と同じ値になる）。荷電領域の
     * 充電能力や加熱領域の熱の総量はこのコストに比例する（2026-11-14 ユーザー）。威力を持つ領域は上書きして、唱えた時点の威力で
     * 求める。既定は {@link #placementCost} と同じ。
     */
    protected int runtimeCost(int size, SpellContext context) throws SpellRuntimeException {
        return placementCostOf(size);
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
