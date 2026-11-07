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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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

/**
 * Discharges lightning from the spell's focal point in the direction. The bolt strikes the nearest hostile creature that
 * way within {@link #RANGE} blocks, then branches twice: from that first target to up to {@link #FIRST_BRANCHES}
 * hostile creatures within {@link #BRANCH_RANGE} blocks, and from each of those to up to {@link #SECOND_BRANCHES} more.
 * No creature is struck twice. The branches deal a share of the first hit ({@link #branchDamage}) and land a moment
 * after it, one stage at a time; targets in water or rain take extra damage. With nothing to strike, the bolt
 * discharges into the air and the spell goes on. With a non-zero Indiscriminate input it picks and branches to any
 * creature instead (see {@link AttackRules#isIndiscriminateTarget}).
 */
public class PieceTrickChainLightning extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 9.0F;
    private static final float DAMAGE_PER_POWER = 9.0F;
    /** How far the bolt reaches from the focal point: the first target, or the end of a discharge into the air. */
    public static final double RANGE = 24.0;
    /** How far a branch reaches from the creature it leaves. */
    public static final double BRANCH_RANGE = 8.0;
    /** How many creatures the first target branches to. */
    public static final int FIRST_BRANCHES = 3;
    /** How many creatures each of the first branches' targets branches to. */
    public static final int SECOND_BRANCHES = 2;
    /** The damage of each stage, as a share of the first hit: the first target, the first branches, the second. */
    private static final float[] STAGE_DAMAGE_FACTORS = {1.0F, 0.4F, 0.2F};
    /** How far from the direction the first target may stand: 30 degrees. */
    private static final double FRONT_COS = Math.cos(Math.toRadians(30.0));
    /**
     * The number of jumps is no longer chosen; the stats are those the old Number input gave at four jumps.
     */
    private static final double POTENCY_BASE = 180.0;
    private static final double POTENCY_PER_POWER = 40.0;
    private static final double COST_BASE = 470.0;
    private static final double COST_PER_POWER = 120.0;

    private SpellParam<Vector3> direction;
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
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, false, false));
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
        Vector3 directionVal = getParamValue(context, direction);
        if (directionVal == null || directionVal.isZero()) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        AttackRules.Options options = AttackRules.Options.from(context);
        boolean anyCreature = AttackRules.isIndiscriminate(getParamValue(context, indiscriminate));
        LivingEntity caster = context.caster;
        Entity focus = context.focalPoint != null ? context.focalPoint : caster;
        Vec3 origin = focus.getEyePosition();
        Vec3 aim = directionVal.toVec3D().normalize();
        // The bolt leaves the caster's hand when the caster is the focal point, else the focal point itself.
        Vec3 start = focus == caster ? CombatSpellHelper.castPoint(caster) : origin;
        int color = CombatSpellHelper.spellColor(caster);

        LivingEntity first = firstTarget(level, context, caster, focus, origin, aim, options, anyCreature);
        if (first == null) {
            Vec3 end = dischargeEnd(level, focus, origin, aim);
            CombatEffects.lightningArcs(level, List.of(start, end), List.of(-1, 0), color, level.getRandom().nextLong());
            boltSounds(level, end);
            return null;
        }

        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        float damage = AttackRules.scale(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.chainLightningDamageMultiplier.get(), caster);
        DamageSource source = AttackRules.source(level, PsitweaksDamageTypes.CHAIN_LIGHTNING, caster, caster);

        // The arcs as a tree: point 0 is where the bolt starts, and each other point is reached from its parent.
        List<Vec3> points = new ArrayList<>();
        List<Integer> parents = new ArrayList<>();
        points.add(start);
        parents.add(-1);
        points.add(CombatSpellHelper.center(first));
        parents.add(0);
        Set<UUID> struck = new HashSet<>();
        struck.add(first.getUUID());
        // The first target is hit at once; the branches land one stage at a time, in step with the arcs.
        EntityChainLightning.strike(level, caster, options, first, source, branchDamage(damage, 0), 0);

        EntityChainLightning branches = new EntityChainLightning(level, points.get(1));
        branches.configure(caster, options, damage, color);
        List<LivingEntity> firstBranches = nextTargets(level, context, caster, first, FIRST_BRANCHES, struck, options,
                anyCreature);
        for (LivingEntity target : firstBranches) {
            points.add(CombatSpellHelper.center(target));
            parents.add(1);
            branches.schedule(target, 1, branchDamage(damage, 1));
        }
        for (int i = 0; i < firstBranches.size(); i++) {
            // The first branches' targets are points 2, 3 and 4.
            int parent = 2 + i;
            for (LivingEntity target : nextTargets(level, context, caster, firstBranches.get(i), SECOND_BRANCHES, struck,
                    options, anyCreature)) {
                points.add(CombatSpellHelper.center(target));
                parents.add(parent);
                branches.schedule(target, 2, branchDamage(damage, 2));
            }
        }
        if (!firstBranches.isEmpty()) {
            level.addFreshEntity(branches);
        }

        CombatEffects.lightningArcs(level, points, parents, color, level.getRandom().nextLong());
        boltSounds(level, points.get(1));
        return null;
    }

    /**
     * The damage of a creature struck at {@code stage}: 0 for the first target, 1 for the first branches and 2 for the
     * second, which deal 40% and 20% of the first hit.
     */
    public static float branchDamage(float firstHit, int stage) {
        return firstHit * STAGE_DAMAGE_FACTORS[stage];
    }

    private static void boltSounds(ServerLevel level, Vec3 at) {
        CombatEffects.sound(level, at, SoundEvents.TRIDENT_THUNDER, 0.7F, 1.6F);
        CombatEffects.sound(level, at, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.9F, 1.5F);
    }

    /** Where a bolt with nothing to strike ends: the first solid block in the direction, or the end of its reach. */
    private static Vec3 dischargeEnd(ServerLevel level, Entity focus, Vec3 origin, Vec3 aim) {
        Vec3 reach = origin.add(aim.scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(origin, reach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                focus));
        return hit.getType() == HitResult.Type.MISS ? reach : hit.getLocation();
    }

    /**
     * The first target: the closest creature the bolt may strike ({@link AttackRules#isAutoTarget}) in the direction from
     * the focal point, within {@link #RANGE} blocks, the spell radius and in sight. "In the direction" is within 30
     * degrees of it, or on the line itself for a creature too big or too close for its centre to be in that cone.
     * Creatures hidden by another player's Trick: Parade are passed over. Null when there is none.
     */
    @Nullable
    private static LivingEntity firstTarget(ServerLevel level, SpellContext context, LivingEntity caster, Entity focus,
                                            Vec3 origin, Vec3 aim, AttackRules.Options options, boolean anyCreature) {
        Vec3 reach = origin.add(aim.scale(RANGE));
        LivingEntity best = null;
        double bestDistance = RANGE * RANGE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(origin, origin).inflate(RANGE))) {
            if (candidate == focus || !context.isInRadius(candidate)
                    || !AttackRules.isAutoTarget(caster, candidate, options, anyCreature)
                    || Parades.decoyFacing(candidate, caster) != null) {
                continue;
            }
            Vec3 center = CombatSpellHelper.center(candidate);
            Vec3 offset = center.subtract(origin);
            double distance = offset.lengthSqr();
            if (distance > bestDistance) {
                continue;
            }
            boolean inFront = offset.dot(aim) >= FRONT_COS * Math.sqrt(distance)
                    || candidate.getBoundingBox().clip(origin, reach).isPresent();
            if (inFront && (CombatSpellHelper.canSee(level, origin, center, focus)
                    || CombatSpellHelper.canSee(level, origin, candidate.getEyePosition(), focus))) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    /**
     * Up to {@code count} creatures the bolt may branch to from {@code from}, closest first: hostile creatures (any
     * creature when {@code anyCreature}) within {@link #BRANCH_RANGE} blocks of it, in the spell radius, in sight and
     * not struck yet. The ones picked are added to {@code struck}.
     */
    private static List<LivingEntity> nextTargets(ServerLevel level, SpellContext context, LivingEntity caster,
                                                  LivingEntity from, int count, Set<UUID> struck,
                                                  AttackRules.Options options, boolean anyCreature) {
        Vec3 origin = CombatSpellHelper.center(from);
        List<LivingEntity> found = new ArrayList<>();
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(origin, origin).inflate(BRANCH_RANGE))) {
            if (struck.contains(candidate.getUUID()) || !AttackRules.isAutoTarget(caster, candidate, options, anyCreature)
                    || !context.isInRadius(candidate)) {
                continue;
            }
            Vec3 center = CombatSpellHelper.center(candidate);
            if (center.distanceToSqr(origin) <= BRANCH_RANGE * BRANCH_RANGE
                    && CombatSpellHelper.canSee(level, origin, center, from)) {
                found.add(candidate);
            }
        }
        found.sort(Comparator.comparingDouble(candidate -> CombatSpellHelper.center(candidate).distanceToSqr(origin)));
        List<LivingEntity> picked = new ArrayList<>(found.subList(0, Math.min(count, found.size())));
        for (LivingEntity target : picked) {
            struck.add(target.getUUID());
        }
        return picked;
    }
}
