package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.combat.EntityChainLightning;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Strikes the target with lightning that then jumps to the nearest hostile creature it can see, up to Number times.
 * Each jump deals half the damage of the one before (but at least 1) and lands a moment after the previous one; targets
 * in water or rain take extra damage. With a non-zero Indiscriminate input it jumps to any creature instead (see
 * {@link CombatSpellHelper#isIndiscriminateTarget}).
 */
public class PieceTrickChainLightning extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 9.0F;
    private static final float DAMAGE_PER_POWER = 9.0F;
    private static final float JUMP_DAMAGE_FACTOR = 0.5F;
    /** The jumps never fall below this, unless the first hit itself is weaker. */
    private static final float MIN_JUMP_DAMAGE = 1.0F;
    private static final double JUMP_RANGE = 6.0;
    public static final int MAX_CHAINS = 8;
    private static final double POTENCY_BASE = 60.0;
    private static final double POTENCY_PER_POWER = 40.0;
    private static final double POTENCY_PER_CHAIN = 30.0;
    private static final double COST_BASE = 150.0;
    private static final double COST_PER_POWER = 120.0;
    private static final double COST_PER_CHAIN = 80.0;

    private SpellParam<Entity> target;
    private SpellParam<Number> power;
    private SpellParam<Number> chains;
    private SpellParam<Number> indiscriminate;

    public PieceTrickChainLightning(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(POTENCY_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER, true).min(MAX_CHAINS).mul(POTENCY_PER_CHAIN))
                .add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(COST_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER, true).min(MAX_CHAINS).mul(COST_PER_CHAIN))
                .add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(chains = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.GREEN, false, true));
        addParam(indiscriminate = new ParamNumber(PsitweaksSpellParams.INDISCRIMINATE, SpellParam.PURPLE, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        int chainCount = Math.min(MAX_CHAINS, CombatTrickStats.positiveInteger(getParamEvaluation(chains), x, y));
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + powerVal * POTENCY_PER_POWER + chainCount * POTENCY_PER_CHAIN));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE + powerVal * COST_PER_POWER + chainCount * COST_PER_CHAIN));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity first = getParamValue(context, target);
        if (CombatSpellHelper.skipFallenTarget(context, first)) {
            return null;
        }
        CombatSpellHelper.verifyTarget(context, first);
        if (!(first instanceof LivingEntity firstLiving)) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (!context.isInRadius(first)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }

        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        int chainCount = Mth.clamp(getParamValue(context, chains).intValue(), 1, MAX_CHAINS);
        boolean safeToPlayers = SpellSafetyUtils.hasSafeToPlayers(context);
        boolean anyCreature = CombatSpellHelper.isIndiscriminate(getParamValue(context, indiscriminate));
        LivingEntity caster = context.caster;
        float damage = CombatSpellHelper.scaleDamage(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.chainLightningDamageMultiplier.get(), caster);
        DamageSource source = CombatSpellHelper.damageSource(level, PsitweaksDamageTypes.CHAIN_LIGHTNING, caster, caster);

        int color = CombatSpellHelper.spellColor(caster);
        List<Vec3> points = new ArrayList<>();
        points.add(CombatSpellHelper.castPoint(caster));
        Set<UUID> struck = new HashSet<>();
        // The first target is hit at once; the jumps land one by one, in step with the arcs.
        EntityChainLightning jumps = null;
        LivingEntity current = firstLiving;
        for (int jump = 0; current != null; jump++) {
            points.add(CombatSpellHelper.center(current));
            struck.add(current.getUUID());
            float dealt = jumpDamage(damage, jump);
            if (jump == 0) {
                if (CombatSpellHelper.canHarm(caster, current, safeToPlayers)) {
                    EntityChainLightning.strike(level, current, source, dealt, jump);
                }
            } else {
                if (jumps == null) {
                    jumps = new EntityChainLightning(level, points.get(1));
                    jumps.configure(caster, safeToPlayers, damage, color);
                }
                jumps.schedule(current, jump, dealt);
            }
            if (jump >= chainCount) {
                break;
            }
            current = nextTarget(level, context, caster, current, struck, safeToPlayers, anyCreature);
        }
        if (jumps != null) {
            level.addFreshEntity(jumps);
        }

        CombatEffects.lightningArcs(level, points, color, level.getRandom().nextLong());
        Vec3 firstHit = points.get(1);
        CombatEffects.sound(level, firstHit, SoundEvents.TRIDENT_THUNDER, 0.7F, 1.6F);
        CombatEffects.sound(level, firstHit, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.9F, 1.5F);
        return null;
    }

    /** The damage of jump number {@code jump} (the first target is jump 0): halved at each jump, but at least 1. */
    public static float jumpDamage(float firstHit, int jump) {
        return Math.max(Math.min(MIN_JUMP_DAMAGE, firstHit), firstHit * (float) Math.pow(JUMP_DAMAGE_FACTOR, jump));
    }

    /**
     * The closest hostile creature (any creature when {@code anyCreature}) within jump range of {@code from}, in the
     * spell radius and in sight.
     */
    @Nullable
    private static LivingEntity nextTarget(ServerLevel level, SpellContext context, LivingEntity caster, LivingEntity from,
                                           Set<UUID> struck, boolean safeToPlayers, boolean anyCreature) {
        Vec3 origin = CombatSpellHelper.center(from);
        AABB area = new AABB(origin, origin).inflate(JUMP_RANGE);
        LivingEntity best = null;
        double bestDistance = JUMP_RANGE * JUMP_RANGE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (struck.contains(candidate.getUUID()) || !CombatSpellHelper.isAutoTarget(caster, candidate, safeToPlayers, anyCreature)
                    || !context.isInRadius(candidate)) {
                continue;
            }
            Vec3 center = CombatSpellHelper.center(candidate);
            double distance = center.distanceToSqr(origin);
            if (distance <= bestDistance && CombatSpellHelper.canSee(level, origin, center, from)) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }
}
