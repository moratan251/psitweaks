package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.combat.EntityPseudoMeteor;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
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
 * Calls Count meteors down around the position. After a half-second warning they land one after another, each
 * exploding without breaking blocks and setting things alight. They go after the creatures near the position that an
 * auto-aimed attack may pick (hostile ones, or any with Indiscriminate), nearest first and in turn, and follow them
 * until they start to fall; how far they look grows with the number of meteors ({@link #aimRange}). With none there,
 * the first lands at the position and the rest anywhere within {@link #SPREAD} blocks of it.
 */
public class PieceTrickPseudoMeteor extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 25.0F;
    private static final float DAMAGE_PER_POWER = 20.0F;
    public static final int MAX_METEORS = 16;
    private static final double SPREAD = 4.0;
    private static final double MIN_AIM_RANGE = 6.0;
    private static final double AIM_RANGE_PER_METEOR = 0.4;
    private static final int WARNING_TICKS = 10;
    private static final int INTERVAL_TICKS = 5;
    private static final double SKY_HEIGHT = 24.0;
    private static final double SKY_OFFSET = 12.0;
    private static final int MAX_CLIMB = 4;
    private static final int MAX_DROP = 12;
    private static final double POTENCY_BASE = 760.0;
    private static final double POTENCY_PER_POWER = 40.0;
    private static final double POTENCY_PER_METEOR = 30.0;
    private static final double COST_BASE = 400.0;
    private static final double COST_PER_METEOR = 100.0;
    private static final double COST_PER_METEOR_POWER = 80.0;

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;
    private SpellParam<Number> count;
    private SpellParam<Number> indiscriminate;

    public PieceTrickPseudoMeteor(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).mul(POTENCY_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER, true).min(MAX_METEORS).mul(POTENCY_PER_METEOR))
                .add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(COST_PER_METEOR_POWER).add(COST_PER_METEOR).parenthesize()
                .mul(new StatLabel(SpellParam.GENERIC_NAME_NUMBER, true).min(MAX_METEORS)).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(count = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.GREEN, false, true));
        addParam(indiscriminate = new ParamNumber(PsitweaksSpellParams.INDISCRIMINATE, SpellParam.PURPLE, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        int meteors = Math.min(MAX_METEORS, CombatTrickStats.positiveInteger(getParamEvaluation(count), x, y));
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + powerVal * POTENCY_PER_POWER
                + meteors * POTENCY_PER_METEOR));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE
                + meteors * (COST_PER_METEOR + powerVal * COST_PER_METEOR_POWER)));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        int meteors = Mth.clamp(getParamValue(context, count).intValue(), 1, MAX_METEORS);
        float damage = AttackRules.scale(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.pseudoMeteorDamageMultiplier.get(), context.caster);
        AttackRules.Options options = AttackRules.Options.from(context);
        boolean anyCreature = AttackRules.isIndiscriminate(getParamValue(context, indiscriminate));
        int color = CombatSpellHelper.spellColor(context.caster);

        Vec3 center = positionVal.toVec3D();
        List<LivingEntity> prey = prey(level, context, center, aimRange(meteors), options, anyCreature);
        Vec3 look = context.caster.getLookAngle();
        Vec3 behind = new Vec3(-look.x, 0.0, -look.z);
        behind = behind.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : behind.normalize();
        RandomSource random = level.getRandom();
        for (int i = 0; i < meteors; i++) {
            LivingEntity chased = prey.isEmpty() ? null : prey.get(i % prey.size());
            Vec3 impact;
            if (chased != null) {
                impact = chased.position();
            } else {
                Vec3 landing = center;
                if (i > 0) {
                    double angle = random.nextDouble() * Mth.TWO_PI;
                    double distance = Math.sqrt(random.nextDouble()) * SPREAD;
                    landing = center.add(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
                }
                impact = new Vec3(landing.x, landingY(level, landing.x, center.y, landing.z), landing.z);
            }
            Vec3 start = impact.add(behind.scale(SKY_OFFSET)).add((random.nextDouble() - 0.5) * 3.0, SKY_HEIGHT,
                    (random.nextDouble() - 0.5) * 3.0);
            EntityPseudoMeteor meteor = new EntityPseudoMeteor(level, start, impact, WARNING_TICKS + i * INTERVAL_TICKS);
            meteor.configure(context.caster, options, damage, color);
            if (chased != null) {
                meteor.chase(chased);
            }
            level.addFreshEntity(meteor);
        }
        CombatEffects.sound(level, center, SoundEvents.BEACON_POWER_SELECT, 1.0F, 0.6F);
        return null;
    }

    /**
     * How far from the position the meteors look for creatures: 6 blocks for one or two meteors, 0.4 more for each
     * meteor beyond two, so 10 blocks for twelve and 11.6 for sixteen.
     */
    public static double aimRange(int meteors) {
        return MIN_AIM_RANGE + Math.max(0, meteors - 2) * AIM_RANGE_PER_METEOR;
    }

    /**
     * The creatures the meteors go after, nearest to the centre first: within {@code range} blocks of it and the spell
     * radius, the ones an auto-aimed attack may pick ({@link AttackRules#isAutoTarget}), and not hiding behind a Trick:
     * Parade lookalike from the caster.
     */
    private static List<LivingEntity> prey(ServerLevel level, SpellContext context, Vec3 center, double range,
                                           AttackRules.Options options, boolean anyCreature) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(range), candidate ->
                        CombatSpellHelper.center(candidate).distanceToSqr(center) <= range * range
                                && context.isInRadius(candidate)
                                && AttackRules.isAutoTarget(context.caster, candidate, options, anyCreature)
                                && Parades.decoyFacing(candidate, context.caster) == null)
                .stream()
                .sorted(Comparator.comparingDouble(candidate -> CombatSpellHelper.center(candidate).distanceToSqr(center)))
                .toList();
    }

    /**
     * The ground in the landing column closest to the cast height: it climbs out of a slope that rises above that
     * height (at most four blocks) and otherwise searches down, so a roof over the position does not catch the meteor.
     */
    private static double landingY(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(x, y + 0.5, z).mutable();
        for (int i = 0; i < MAX_CLIMB && !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty(); i++) {
            pos.move(Direction.UP);
        }
        Double groundY = CombatSpellHelper.findGroundY(level, x, pos.getY() + 0.5, z, MAX_DROP);
        return groundY != null ? groundY : y;
    }
}
