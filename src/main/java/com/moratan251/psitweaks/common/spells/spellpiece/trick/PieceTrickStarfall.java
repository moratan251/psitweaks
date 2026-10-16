package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.combat.EntityStarfallMeteor;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
 * Calls Count meteors down around the position. After a one-second warning they land one after another, the first
 * at the centre and the rest within four blocks, each exploding without breaking blocks and setting things alight.
 */
public class PieceTrickStarfall extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 5.0F;
    private static final float DAMAGE_PER_POWER = 4.0F;
    public static final int MAX_METEORS = 12;
    private static final double SPREAD = 4.0;
    private static final int WARNING_TICKS = 20;
    private static final int INTERVAL_TICKS = 5;
    private static final double SKY_HEIGHT = 24.0;
    private static final double SKY_OFFSET = 12.0;
    private static final int MAX_CLIMB = 4;
    private static final int MAX_DROP = 12;
    private static final double POTENCY_BASE = 150.0;
    private static final double POTENCY_PER_POWER = 40.0;
    private static final double POTENCY_PER_METEOR = 30.0;
    private static final double COST_BASE = 400.0;
    private static final double COST_PER_METEOR = 100.0;
    private static final double COST_PER_METEOR_POWER = 80.0;

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;
    private SpellParam<Number> count;

    public PieceTrickStarfall(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).mul(POTENCY_PER_POWER)
                .add(new StatLabel("psitweaks.spellparam.count", true).min(MAX_METEORS).mul(POTENCY_PER_METEOR))
                .add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(COST_PER_METEOR_POWER).add(COST_PER_METEOR).parenthesize()
                .mul(new StatLabel("psitweaks.spellparam.count", true).min(MAX_METEORS)).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(count = new ParamNumber("psitweaks.spellparam.count", SpellParam.GREEN, false, true));
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
        float damage = CombatSpellHelper.scaleDamage(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.starfallDamageMultiplier.get(), context.caster);
        boolean safeToPlayers = SpellSafetyUtils.hasSafeToPlayers(context);
        int color = CombatSpellHelper.spellColor(context.caster);

        Vec3 center = positionVal.toVec3D();
        Vec3 look = context.caster.getLookAngle();
        Vec3 behind = new Vec3(-look.x, 0.0, -look.z);
        behind = behind.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : behind.normalize();
        RandomSource random = level.getRandom();
        for (int i = 0; i < meteors; i++) {
            Vec3 landing = center;
            if (i > 0) {
                double angle = random.nextDouble() * Mth.TWO_PI;
                double distance = Math.sqrt(random.nextDouble()) * SPREAD;
                landing = center.add(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
            }
            Vec3 impact = new Vec3(landing.x, landingY(level, landing.x, center.y, landing.z), landing.z);
            Vec3 start = impact.add(behind.scale(SKY_OFFSET)).add((random.nextDouble() - 0.5) * 3.0, SKY_HEIGHT,
                    (random.nextDouble() - 0.5) * 3.0);
            EntityStarfallMeteor meteor = new EntityStarfallMeteor(level, start, impact, WARNING_TICKS + i * INTERVAL_TICKS);
            meteor.configure(context.caster, safeToPlayers, damage, color);
            level.addFreshEntity(meteor);
        }
        CombatEffects.sound(level, center, SoundEvents.BEACON_POWER_SELECT, 1.0F, 0.6F);
        return null;
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
