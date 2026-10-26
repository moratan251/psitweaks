package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.combat.EntityChainLightning;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
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
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Strikes the nearest hostile creature in front of the caster with lightning that then jumps to the nearest hostile
 * creature it can see, up to {@link #MAX_CHAINS} times. Each jump deals half the damage of the one before (but at least
 * 1) and lands a moment after the previous one; targets in water or rain take extra damage. With a non-zero
 * Indiscriminate input it picks and jumps to any creature instead (see {@link CombatSpellHelper#isIndiscriminateTarget}).
 */
public class PieceTrickChainLightning extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 9.0F;
    private static final float DAMAGE_PER_POWER = 9.0F;
    private static final float JUMP_DAMAGE_FACTOR = 0.5F;
    /** The jumps never fall below this, unless the first hit itself is weaker. */
    private static final float MIN_JUMP_DAMAGE = 1.0F;
    private static final double JUMP_RANGE = 6.0;
    public static final int MAX_CHAINS = 10;
    /** How far from where the caster looks the first target may stand: 30 degrees. */
    private static final double FRONT_COS = Math.cos(Math.toRadians(30.0));
    /**
     * The number of jumps is no longer chosen; the stats are those the old Number input gave at four jumps.
     */
    private static final double POTENCY_BASE = 180.0;
    private static final double POTENCY_PER_POWER = 40.0;
    private static final double COST_BASE = 470.0;
    private static final double COST_PER_POWER = 120.0;

    private SpellParam<Number> power;
    private SpellParam<Number> indiscriminate;

    public PieceTrickChainLightning(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        // Spells saved with the old Target and Number inputs still load: Psi only reads the inputs a piece has now.
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(indiscriminate = new ParamNumber(PsitweaksSpellParams.INDISCRIMINATE, SpellParam.PURPLE, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + powerVal * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE + powerVal * COST_PER_POWER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        AttackRules.Options options = AttackRules.Options.from(context);
        boolean anyCreature = AttackRules.isIndiscriminate(getParamValue(context, indiscriminate));
        LivingEntity caster = context.caster;
        LivingEntity first = firstTarget(level, context, caster, options, anyCreature);
        if (AttackRules.skipFallenTarget(context, first)) {
            return null;
        }
        AttackRules.verifyTarget(context, first);

        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        float damage = AttackRules.scale(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.chainLightningDamageMultiplier.get(), caster);
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.CHAIN_LIGHTNING, caster, caster);

        int color = CombatSpellHelper.spellColor(caster);
        List<Vec3> points = new ArrayList<>();
        points.add(CombatSpellHelper.castPoint(caster));
        Set<UUID> struck = new HashSet<>();
        // The first target is hit at once; the jumps land one by one, in step with the arcs.
        EntityChainLightning jumps = null;
        LivingEntity current = first;
        for (int jump = 0; current != null; jump++) {
            points.add(CombatSpellHelper.center(current));
            struck.add(current.getUUID());
            float dealt = jumpDamage(damage, jump);
            if (jump == 0) {
                if (AttackRules.canHarm(caster, current, options)) {
                    EntityChainLightning.strike(level, caster, options, current, source, dealt, jump);
                }
            } else {
                if (jumps == null) {
                    jumps = new EntityChainLightning(level, points.get(1));
                    jumps.configure(caster, options, damage, color);
                }
                jumps.schedule(current, jump, dealt);
            }
            if (jump >= MAX_CHAINS) {
                break;
            }
            current = nextTarget(level, context, caster, current, struck, options, anyCreature);
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
     * The first target: the closest creature the bolt may jump to ({@link #nextTarget}'s rules) in front of the caster,
     * within the spell radius and in sight. "In front" is within 30 degrees of where the caster looks, or under the
     * crosshair for a creature too big or too close for its centre to be in that cone. Creatures hidden by another
     * player's Trick: Parade are passed over. When nothing standing is in front but a creature that has just fallen is,
     * that one is returned, so the caller can warn instead of failing; null when there is nothing.
     */
    @Nullable
    private static LivingEntity firstTarget(ServerLevel level, SpellContext context, LivingEntity caster,
                                            AttackRules.Options options, boolean anyCreature) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        double range = SpellContext.MAX_DISTANCE;
        Vec3 reach = eye.add(look.scale(range));
        LivingEntity best = null;
        LivingEntity fallen = null;
        double bestDistance = range * range;
        double fallenDistance = range * range;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, eye).inflate(range))) {
            if (candidate == caster || !context.isInRadius(candidate)) {
                continue;
            }
            Vec3 center = CombatSpellHelper.center(candidate);
            Vec3 offset = center.subtract(eye);
            double distance = offset.lengthSqr();
            boolean fell = AttackRules.hasFallen(candidate);
            boolean wanted = fell ? distance <= fallenDistance
                    : distance <= bestDistance && AttackRules.isAutoTarget(caster, candidate, options, anyCreature)
                    && Parades.decoyFacing(candidate, caster) == null;
            boolean inFront = offset.dot(look) >= FRONT_COS * Math.sqrt(distance)
                    || candidate.getBoundingBox().clip(eye, reach).isPresent();
            if (!wanted || !inFront || !CombatSpellHelper.canSee(level, eye, center, caster)
                    && !CombatSpellHelper.canSee(level, eye, candidate.getEyePosition(), caster)) {
                continue;
            }
            if (fell) {
                fallen = candidate;
                fallenDistance = distance;
            } else {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best != null ? best : fallen;
    }

    /**
     * The closest hostile creature (any creature when {@code anyCreature}) within jump range of {@code from}, in the
     * spell radius and in sight.
     */
    @Nullable
    private static LivingEntity nextTarget(ServerLevel level, SpellContext context, LivingEntity caster, LivingEntity from,
                                           Set<UUID> struck, AttackRules.Options options, boolean anyCreature) {
        Vec3 origin = CombatSpellHelper.center(from);
        AABB area = new AABB(origin, origin).inflate(JUMP_RANGE);
        LivingEntity best = null;
        double bestDistance = JUMP_RANGE * JUMP_RANGE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (struck.contains(candidate.getUUID()) || !AttackRules.isAutoTarget(caster, candidate, options, anyCreature)
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
