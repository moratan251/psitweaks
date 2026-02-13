package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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

import java.util.List;

public class PieceTrickActiveAirMine extends PieceTrick {

    private static final double MIN_POWER = 0.1;
    private static final double MIN_RADIUS = 2.0;
    private static final double MAX_RADIUS = 6.0;
    private static final double RADIUS_PER_POWER = 0.5;
    private static final float BASE_DAMAGE = 4.5F;
    private static final float DAMAGE_PER_POWER = 8.0F;
    private static final double POTENCY_BASE = 200;
    private static final double POTENCY_PER_POWER = 50;
    private static final double COST_BASE = 100;
    private static final double COST_PER_POWER = 400;

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;

    public PieceTrickActiveAirMine(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY,
                new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST,
                new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = getParamEvaluation(power);
        if (powerVal == null || powerVal <= 0.0) {
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
        if (powerVal <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        double clampedPower = Math.max(MIN_POWER, powerVal);
        double radius = calculateRadius(clampedPower);
        float damage = calculateDamage(clampedPower);

        double perSpellDamageMultiplier = PsitweaksConfig.COMMON.activeAirMineDamageMultiplier.get();
        double globalDamageMultiplier = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        float finalDamage = (float) (damage * perSpellDamageMultiplier * globalDamageMultiplier);

        Vec3 center = new Vec3(positionVal.x, positionVal.y, positionVal.z);
        double radiusSq = radius * radius;
        AABB searchArea = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                target -> target.isAlive()
                        && target != context.caster
                        && target.getBoundingBox().distanceToSqr(center) <= radiusSq
        );

        for (LivingEntity target : targets) {
            target.hurt(context.caster.damageSources().sonicBoom(context.caster), finalDamage);
        }

        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
        level.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS,
                0.7F,
                1.0F
        );

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 28, radius * 0.35, radius * 0.2, radius * 0.35, 0.03);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 20, radius * 0.3, radius * 0.25, radius * 0.3, 0.05);
        }

        return null;
    }

    private static double calculateRadius(double powerVal) {
        return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, MIN_RADIUS + powerVal * RADIUS_PER_POWER));
    }

    private static float calculateDamage(double powerVal) {
        return BASE_DAMAGE + (float) (powerVal * DAMAGE_PER_POWER);
    }
}
