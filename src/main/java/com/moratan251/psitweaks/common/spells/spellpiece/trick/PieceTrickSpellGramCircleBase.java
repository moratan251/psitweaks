package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramCircle;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

import java.util.UUID;

/**
 * 円の魔法式オブジェクト（{@link SpellGramCircle}）を床に張る作動式の基底（作動式: ファイアサークル・アイスサークル）。
 * 入力は位置と威力。張り直すと、同じ術者が張っていた同じ種類の円は解ける（1 人 1 つずつ）。
 */
public abstract class PieceTrickSpellGramCircleBase<T extends SpellGramCircle> extends PieceTrick {

    // バランス調整用の定数（両方の円で共通）
    private static final double MIN_POWER = 0.5D;
    private static final float BASE_DAMAGE = 2.0F;
    private static final float DAMAGE_PER_POWER = 2.0F;
    private static final float FIXED_RADIUS = 8.0F;
    // RenderSpellCircle 側の見た目の半径は「2 * horizontalScale」なので、半径 8.0 に合わせて 4.0 を使う
    private static final float FIXED_VISUAL_SCALE = FIXED_RADIUS * 0.5F;
    private static final int LIFETIME_TICKS = 60 * 20; // 60 秒
    private static final int DAMAGE_INTERVAL_TICKS = 10;
    private static final double POTENCY_BASE = 200.0D;
    private static final double POTENCY_PER_POWER = 100.0D;
    private static final double COST_BASE = 2000.0D;
    private static final double COST_PER_POWER = 1200.0D;
    /**
     * 強度の固有値（仮）。強度 = 固有値 × 張った時点の事象干渉力。術式解体や干渉力での主導権の奪い合いを作るときに、
     * 魔法式オブジェクト全体でそろえ直す。
     */
    private static final float BASE_INTEGRITY = 50.0F;

    private final Class<T> circleType;

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;

    protected PieceTrickSpellGramCircleBase(Spell spell, Class<T> circleType) {
        super(spell);
        this.circleType = circleType;
        setStatLabel(EnumSpellStat.POTENCY,
                new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST,
                new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(COST_PER_POWER).add(COST_BASE).floor());
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
        addParam(power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = getParamEvaluation(power);
        if (powerVal == null || powerVal <= 0.0D) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }

        powerVal = Math.max(MIN_POWER, powerVal);
        meta.addStat(EnumSpellStat.POTENCY, (int) (POTENCY_BASE + powerVal * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, (int) (COST_BASE + powerVal * COST_PER_POWER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        double powerVal = getParamValue(context, power).doubleValue();

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (powerVal <= 0.0D) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (context.caster == null || !(context.caster.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        double clampedPower = Math.max(MIN_POWER, powerVal);
        float baseDamage = BASE_DAMAGE + (float) (clampedPower * DAMAGE_PER_POWER);
        float finalDamage = AttackRules.scale(baseDamage, damageMultiplier(), context.caster);

        removeExistingCircles(serverLevel, CasterIds.idOf(context.caster));

        Vec3 effectPosition = SableRangeCompat.projectForEffect(serverLevel, positionVal);
        T circle = createCircle(serverLevel);
        circle.setPos(effectPosition.x, effectPosition.y + 0.01D, effectPosition.z);
        circle.setCaster(context.caster);
        circle.setMaxIntegrity(SpellGramIntegrity.initial(BASE_INTEGRITY, context.caster, 1.0D));
        circle.setSafeToPlayers(SpellSafetyUtils.hasSafeToPlayers(context));
        circle.setVisualScale(FIXED_VISUAL_SCALE);
        circle.setLifetimeTicks(LIFETIME_TICKS);
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

    /** 同じ術者が張っていた同じ種類の円を、読み込まれているすべてのディメンションで解く。 */
    private void removeExistingCircles(ServerLevel castingLevel, UUID casterUuid) {
        for (ServerLevel level : castingLevel.getServer().getAllLevels()) {
            for (var object : SpellGramIndex.ofCaster(level, casterUuid)) {
                if (circleType.isInstance(object)) {
                    object.end(SpellGramObject.EndReason.DISPELLED);
                }
            }
        }
    }
}
