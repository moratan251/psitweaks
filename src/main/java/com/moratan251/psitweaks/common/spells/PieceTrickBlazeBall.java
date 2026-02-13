package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.EntityBlazeBall;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
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

public class PieceTrickBlazeBall extends PieceTrick {

    private static final double MIN_POWER = 0.1;
    private static final float BASE_DAMAGE = 2.0F;
    private static final float DAMAGE_PER_POWER = 2.0F;
    private static final float BASE_SPEED = 1.0F;
    private static final float SPEED_PER_POWER = 0.04F;
    private static final double POTENCY_BASE = 50.0;
    private static final double POTENCY_PER_POWER = 50.0;
    private static final double COST_BASE = 150.0;
    private static final double COST_PER_POWER = 150.0;

    private SpellParam<Number> power;

    public PieceTrickBlazeBall(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.power", true).max(0.1).mul(100).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel("psi.spellparam.power", true).max(0.1).mul(200).add(200).floor());
    }

    @Override
    public void initParams() {
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
        double powerVal = getParamValue(context, power).doubleValue();
        if (powerVal <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        powerVal = Math.max(MIN_POWER, powerVal);

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        float baseDamage = BASE_DAMAGE + (float) (powerVal * DAMAGE_PER_POWER);
        double perSpellDamageMul = PsitweaksConfig.COMMON.blazeBallDamageMultiplier.get();
        double globalDamageMul = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        float finalDamage = (float) (baseDamage * perSpellDamageMul * globalDamageMul);
        float speed = BASE_SPEED + (float) (powerVal * SPEED_PER_POWER);

        EntityBlazeBall projectile = new EntityBlazeBall(PsitweaksEntities.BLAZE_BALL.get(), level);
        projectile.setOwner(context.caster);
        Vec3 look = context.caster.getLookAngle();
        projectile.setPos(
                context.caster.getX() + look.x * 0.5,
                context.caster.getEyeY() - 0.1,
                context.caster.getZ() + look.z * 0.5
        );
        projectile.shootFromRotation(context.caster, context.caster.getXRot(), context.caster.getYRot(), 0.0F, speed, 0.0F);
        projectile.setDamage(finalDamage);

        level.addFreshEntity(projectile);
        level.playSound(
                null,
                context.caster.getX(),
                context.caster.getY(),
                context.caster.getZ(),
                SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.random.nextFloat() * 0.4F + 0.8F)
        );

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMALL_FLAME,
                    context.caster.getX(),
                    context.caster.getEyeY() - 0.1,
                    context.caster.getZ(),
                    10,
                    0.15,
                    0.15,
                    0.15,
                    0.02
            );
        }

        return null;
    }
}
