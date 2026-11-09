package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.api.value.MatrixValue;
import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
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

import java.util.UUID;

/**
 * 領域型の魔法式オブジェクト（○○ Zone）を置く作動式の基底（試作）。共通の入力は「範囲」（領域行列）と「サイズ」（定数）。
 * <p>
 * 範囲は行列の中のブロックのうち、行列の中心のブロックを中心にした一辺サイズのブロックの立方体に入るもの（{@link ZoneArea}、
 * 数え方は演算子: 直方体領域と同じ）。コストと規模はサイズだけで
 * 決まる（術式を組んだ時点で分かるのは定数の大きさだけのため）。1 人が置けるのは種類ごとに 1 つで、置き直すと古いものが解ける。
 * 干渉力は効き目に効かず、耐久値にだけ効く。
 */
public abstract class PieceTrickZoneBase<T extends SpellGramZone> extends PieceTrick {
    /** サイズ（立方体の一辺のブロック数）の上限。範囲は最大 65×65×65 ブロック。 */
    public static final int MAX_SIZE = 65;
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
        addParam(region = new ParamMatrix(PsitweaksSpellParams.MATRIX, PsitweaksSpellParams.MATRIX_COLOR, false, false));
        addParam(size = new ParamNumber(PsitweaksSpellParams.SIZE, SpellParam.GREEN, false, true));
        addZoneParams();
    }

    /** 種類ごとの入力（範囲・大きさのあと）。 */
    protected void addZoneParams() {
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
        ZoneArea area = ZoneArea.of(getNotNullParamValue(context, region), clampSize(getNotNullParamValue(context, size)));
        if (area.isEmpty()) {
            throw new SpellRuntimeException(ERROR_EMPTY_ZONE);
        }
        BlockPos center = area.center();
        if (!context.isInRadius(new Vector3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5))) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (context.caster == null || !(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }

        T zone = createZone(level);
        zone.configureZone(area, CombatSpellHelper.spellColor(context.caster));
        configure(zone, context);
        removeExistingZones(level, CasterIds.idOf(context.caster));
        zone.setCaster(context.caster);
        zone.setLifetimeTicks(LIFETIME_TICKS);
        zone.setMaxIntegrity(SpellGramIntegrity.initial(BASE_INTEGRITY, context.caster, 1.0D));
        level.addFreshEntity(zone);

        Vec3 at = zone.position();
        level.playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.6F);
        return null;
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

    /** 同じ術者が置いていた同じ種類の範囲を、読み込まれているすべてのディメンションで解く。 */
    private void removeExistingZones(ServerLevel castingLevel, UUID casterUuid) {
        for (ServerLevel level : castingLevel.getServer().getAllLevels()) {
            for (SpellGramObject object : SpellGramIndex.ofCaster(level, casterUuid)) {
                if (zoneType.isInstance(object)) {
                    object.end(SpellGramObject.EndReason.DISPELLED);
                }
            }
        }
    }
}
