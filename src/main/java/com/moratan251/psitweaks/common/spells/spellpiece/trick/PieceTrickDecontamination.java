package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.compat.ZoneRadiationCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityDecontamination;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramLimits;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
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
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 除染（Decontamination、Mekanism があるときだけ登録）。「位置」に除染の核（{@link EntityDecontamination}、領域ではない
 * 常駐魔法式）を置き、半径 32 ブロックの中の放射線源を取り除く（生き物の被ばく量には触れない）。入力は位置・威力（1 以上の定数、10 を
 * 超えた分はコスト＝除染能力だけを増やす）・時間（秒の定数、省略できる）。
 * コスト = (50 + 威力 × 100) × 時間 ÷ 30、規模 = 50 + min(威力, 10) × 30。Mekanism の設定で放射線が切ってあれば、置かずに警告する。
 */
public class PieceTrickDecontamination extends PieceTrick {
    /** コストの固定分（30 秒）。 */
    public static final int COST_BASE = 50;
    /** 威力 1 あたりのコスト（30 秒）。 */
    public static final int COST_PER_POWER = 100;
    /** 規模の固定分。 */
    public static final int POTENCY_BASE = 50;
    /** 威力 1 あたりの規模（威力 10 まで）。 */
    public static final int POTENCY_PER_POWER = 30;
    /** 威力の下限と上限（上限はコストの桁あふれよけ）。 */
    public static final double MIN_POWER = 1.0;
    public static final double MAX_POWER = 1000.0;
    /** 規模に数える威力の上限。 */
    public static final double MAX_POTENCY_POWER = 10.0;
    /** 「時間」を省いたときの存続時間（秒）。コストと耐久値はこの時間を基準に、時間に比例する。 */
    public static final int DEFAULT_TIME_SECONDS = 30;
    public static final int MIN_TIME_SECONDS = 1;
    public static final int MAX_TIME_SECONDS = 120;
    /** 耐久値の固有値（仮）。耐久値 = これ × 置いた時点の事象干渉力 × 時間 ÷ 30。 */
    private static final float BASE_INTEGRITY = 30.0F;

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;
    private SpellParam<Number> time;

    public PieceTrickDecontamination(Spell spell) {
        super(spell);
        StatLabel powerLabel = new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).min(MAX_POWER);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .min(MAX_POTENCY_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, powerLabel.mul(COST_PER_POWER).add(COST_BASE).parenthesize()
                .mul(new StatLabel(SpellParam.GENERIC_NAME_TIME, true)).div(DEFAULT_TIME_SECONDS).ceil());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.GREEN, true, true));
    }

    /** コスト: (50 + 威力 × 100) × 時間 ÷ 30（切り上げ、威力は 1〜1000、時間は 1〜120 秒にそろえる）。 */
    public static int costFor(double power, double seconds) {
        double base = COST_BASE + clampPower(power) * COST_PER_POWER;
        return Math.max(1, (int) Math.ceil(base * clampSeconds(seconds) / DEFAULT_TIME_SECONDS));
    }

    /** 規模: 50 + min(威力, 10) × 30。 */
    public static int potencyFor(double power) {
        return POTENCY_BASE + (int) Math.floor(Math.min(clampPower(power), MAX_POTENCY_POWER) * POTENCY_PER_POWER);
    }

    /** 置いたときの耐久値: 30 × 置いた時点の事象干渉力 × 時間 ÷ 30（領域と同じ決まり）。 */
    public static float integrityFor(@Nullable LivingEntity caster, double seconds) {
        return SpellGramIntegrity.initial(BASE_INTEGRITY, caster, clampSeconds(seconds) / DEFAULT_TIME_SECONDS);
    }

    /** 耐久値の上限（作動式: 魔法式延長で戻せる上限）: 「時間」を最大にして置いたときの耐久値の 2 倍。 */
    public static float integrityCap(@Nullable LivingEntity caster) {
        return integrityFor(caster, MAX_TIME_SECONDS) * SpellGramObject.DEFAULT_INTEGRITY_CAP_FACTOR;
    }

    private static double clampPower(double power) {
        return Math.max(MIN_POWER, Math.min(MAX_POWER, power));
    }

    private static double clampSeconds(double seconds) {
        return Math.max(MIN_TIME_SECONDS, Math.min(MAX_TIME_SECONDS, seconds));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Object powerValue = getParamEvaluation(power);
        if (!(powerValue instanceof Number number) || number.doubleValue() <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        meta.addStat(EnumSpellStat.POTENCY, potencyFor(number.doubleValue()));
        meta.addStat(EnumSpellStat.COST, costFor(number.doubleValue(), constantSeconds()));
    }

    /** 術式を組んだ時点の時間（秒）。省けば 30、正の定数でなければエラー。 */
    private double constantSeconds() throws SpellCompilationException {
        Object value = getParamEvaluation(time);
        if (value == null) {
            return DEFAULT_TIME_SECONDS;
        }
        if (!(value instanceof Number number) || number.doubleValue() <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return clampSeconds(number.doubleValue());
    }

    private double secondsValue(SpellContext context) throws SpellRuntimeException {
        Number value = getParamValue(context, time);
        if (value == null) {
            return DEFAULT_TIME_SECONDS;
        }
        if (value.doubleValue() <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        return clampSeconds(value.doubleValue());
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (!ZoneRadiationCompat.isRadiationEnabled()) {
            SpellWarning.warn(context, SpellWarning.RADIATION_DISABLED);
            return null;
        }
        Vector3 positionValue = getParamValue(context, position);
        if (positionValue == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        double powerValue = getNotNullParamValue(context, power).doubleValue();
        if (powerValue <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        double seconds = secondsValue(context);
        if (!context.isInRadius(positionValue)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (context.caster == null || !(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }

        SpellGramLimits.makeRoom(level, context.caster);
        Vec3 at = SableRangeCompat.projectForEffect(level, positionValue);
        EntityDecontamination core = new EntityDecontamination(level);
        core.setPos(at.x, at.y, at.z);
        core.setCaster(context.caster);
        core.configure((float) clampPower(powerValue), CombatSpellHelper.spellColor(context.caster));
        core.setMaxIntegrity(integrityFor(context.caster, seconds));
        core.setIntegrityCap(integrityCap(context.caster));
        core.setLifetimeTicks((int) Math.round(seconds * 20.0));
        core.setPlacementCost(costFor(powerValue, seconds));
        level.addFreshEntity(core);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.8F);
        return null;
    }
}
