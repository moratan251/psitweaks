package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramLimits;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
 * 円の魔法式オブジェクト（{@link SpellGramCircle}）を床に張る作動式の基底（作動式: ファイアサークル・アイスサークル）。
 * 入力は位置・威力・時間（秒の定数、省略できる）。存続時間・置いたときの耐久値・コストは時間に比例する（領域の「時間」と同じ）。
 * 同じ種類もいくつでも置け、1 人が置ける数は領域と合わせて上限まで（超えると古いものから解ける）。
 */
public abstract class PieceTrickSpellGramCircleBase<T extends SpellGramCircle> extends PieceTrick {

    // バランス調整用の定数（両方の円で共通）
    private static final double MIN_POWER = 0.5D;
    private static final float BASE_DAMAGE = 2.0F;
    private static final float DAMAGE_PER_POWER = 2.0F;
    private static final float FIXED_RADIUS = 8.0F;
    // RenderSpellCircle 側の見た目の半径は「2 * horizontalScale」なので、半径 8.0 に合わせて 4.0 を使う
    private static final float FIXED_VISUAL_SCALE = FIXED_RADIUS * 0.5F;
    private static final int DAMAGE_INTERVAL_TICKS = 10;
    private static final double POTENCY_BASE = 200.0D;
    private static final double POTENCY_PER_POWER = 100.0D;
    private static final double COST_BASE = 2000.0D;
    private static final double COST_PER_POWER = 1200.0D;
    /** コストの下限（時間を短くしても、これより安くはならない）。 */
    public static final int MIN_COST = 50;
    /** 「時間」を省いたときの存続時間（秒）。コストと耐久値はこの時間のときの値を基準に、時間に比例する。 */
    public static final int DEFAULT_TIME_SECONDS = 60;
    /** 「時間」の上限（秒）。 */
    public static final int MAX_TIME_SECONDS = 120;
    /** 「時間」の下限（秒）。 */
    public static final int MIN_TIME_SECONDS = 1;
    /**
     * 強度の固有値（仮）。強度 = 固有値 × 張った時点の事象干渉力 × 時間 ÷ {@link #DEFAULT_TIME_SECONDS}。
     */
    private static final float BASE_INTEGRITY = 50.0F;

    private final Class<T> circleType;

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;
    private SpellParam<Number> time;

    protected PieceTrickSpellGramCircleBase(Spell spell, Class<T> circleType) {
        super(spell);
        this.circleType = circleType;
        setStatLabel(EnumSpellStat.POTENCY,
                new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST,
                new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).mul(COST_PER_POWER).add(COST_BASE)
                        .parenthesize().mul(new StatLabel(SpellParam.GENERIC_NAME_TIME, true)).div(DEFAULT_TIME_SECONDS)
                        .floor().max(MIN_COST));
    }

    /** 新しい円（まだワールドに入れていないもの）。 */
    protected abstract T createCircle(ServerLevel level);

    /** 円ごとのダメージの設定倍率。 */
    protected abstract double damageMultiplier();

    /** 張ったときの音。 */
    protected abstract SoundEvent placeSound();

    /** 張ったときの音の高さ。 */
    protected abstract float placePitch();

    /** 張ったときに散らすパーティクル。 */
    protected abstract ParticleOptions placeParticle();

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.GREEN, true, true));
    }

    /** コスト: max(50, ⌊(2000 + 1200 × 威力) × 時間 ÷ 60⌋)。威力は 0.5 以上、時間は 1〜120 秒にそろえる。 */
    public static int costFor(double power, double seconds) {
        double base = COST_BASE + Math.max(MIN_POWER, power) * COST_PER_POWER;
        return Math.max(MIN_COST, (int) Math.floor(base * clampSeconds(seconds) / DEFAULT_TIME_SECONDS));
    }

    /** 規模: ⌊200 + 100 × 威力⌋（時間によらない）。 */
    public static int potencyFor(double power) {
        return (int) (POTENCY_BASE + Math.max(MIN_POWER, power) * POTENCY_PER_POWER);
    }

    /** 置いたときの耐久値: 50 × 事象干渉力 × 時間 ÷ 60。 */
    public static float integrityFor(@Nullable LivingEntity caster, double seconds) {
        return SpellGramIntegrity.initial(BASE_INTEGRITY, caster, clampSeconds(seconds) / DEFAULT_TIME_SECONDS);
    }

    /**
     * 耐久値の上限（作動式: 魔法式延長で戻せる上限）。「時間」によらず、「時間」を最大（{@link #MAX_TIME_SECONDS} 秒）にして
     * 置いたときの耐久値 × {@link SpellGramObject#DEFAULT_INTEGRITY_CAP_FACTOR}（領域と同じ決まり）。
     */
    public static float integrityCap(@Nullable LivingEntity caster) {
        return integrityFor(caster, MAX_TIME_SECONDS) * SpellGramObject.DEFAULT_INTEGRITY_CAP_FACTOR;
    }

    public static double clampSeconds(double seconds) {
        return Math.max(MIN_TIME_SECONDS, Math.min(MAX_TIME_SECONDS, seconds));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = getParamEvaluation(power);
        if (powerVal == null || powerVal <= 0.0D) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        meta.addStat(EnumSpellStat.POTENCY, potencyFor(powerVal));
        meta.addStat(EnumSpellStat.COST, costFor(powerVal, constantSeconds()));
    }

    /** 術式を組んだ時点の時間（秒）。省けば 60、正の定数でなければエラー。 */
    private double constantSeconds() throws SpellCompilationException {
        Object value = getParamEvaluation(time);
        if (value == null) {
            return DEFAULT_TIME_SECONDS;
        }
        if (!(value instanceof Number number) || number.doubleValue() <= 0.0D) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return clampSeconds(number.doubleValue());
    }

    /** 唱えた時点の時間（秒）。省けば 60。 */
    private double secondsValue(SpellContext context) throws SpellRuntimeException {
        Number value = getParamValue(context, time);
        if (value == null) {
            return DEFAULT_TIME_SECONDS;
        }
        if (value.doubleValue() <= 0.0D) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        return clampSeconds(value.doubleValue());
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        Number powerParam = getParamValue(context, power);
        double powerVal = powerParam == null ? 0.0D : powerParam.doubleValue();

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (powerVal <= 0.0D) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        double seconds = secondsValue(context);
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (context.caster == null || !(context.caster.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        double clampedPower = Math.max(MIN_POWER, powerVal);
        float baseDamage = BASE_DAMAGE + (float) (clampedPower * DAMAGE_PER_POWER);
        float finalDamage = AttackRules.scale(baseDamage, damageMultiplier(), context.caster);

        // 同じ種類もいくつでも置け、1 人が置ける数は領域と合わせて上限まで（超えると古いものから解ける）。
        SpellGramLimits.makeRoom(serverLevel, context.caster);

        Vec3 effectPosition = SableRangeCompat.projectForEffect(serverLevel, positionVal);
        T circle = createCircle(serverLevel);
        circle.setPos(effectPosition.x, effectPosition.y + 0.01D, effectPosition.z);
        // 置いた人の側と置いた時点の事象干渉力も、ここで決まる（重なりと領域干渉で比べる）。
        circle.setCaster(context.caster);
        circle.setPower((float) clampedPower);
        circle.setMaxIntegrity(integrityFor(context.caster, seconds));
        circle.setIntegrityCap(integrityCap(context.caster));
        circle.setSafeToPlayers(SpellSafetyUtils.hasSafeToPlayers(context));
        circle.setVisualScale(FIXED_VISUAL_SCALE);
        circle.setLifetimeTicks((int) Math.round(seconds * 20.0D));
        // 置いたときに払ったコスト（術式を組んだときと同じ式。作動式: 魔法式延長のため）。
        circle.setPlacementCost(costFor(clampedPower, seconds));
        circle.configureAreaDamage(FIXED_RADIUS, finalDamage, DAMAGE_INTERVAL_TICKS);

        serverLevel.addFreshEntity(circle);

        Vec3 center = circle.position();
        serverLevel.playSound(null, center.x, center.y, center.z, placeSound(), SoundSource.PLAYERS, 0.8F, placePitch());
        serverLevel.sendParticles(
                placeParticle(),
                center.x,
                center.y + 0.05D,
                center.z,
                18,
                FIXED_RADIUS * 0.35D,
                0.08D,
                FIXED_RADIUS * 0.35D,
                0.01D
        );

        return null;
    }
}
