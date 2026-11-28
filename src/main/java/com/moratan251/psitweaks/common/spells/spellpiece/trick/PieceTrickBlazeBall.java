package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.EntityBlazeBall;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
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
 * Fires a ball of flame from Position along Direction. Both are optional: without Position the ball leaves from in
 * front of the caster's eyes, and without Direction it flies the way the caster looks.
 */
public class PieceTrickBlazeBall extends PieceTrick {

    private static final double MIN_POWER = 0.1;
    private static final float BASE_DAMAGE = 2.0F;
    private static final float DAMAGE_PER_POWER = 2.0F;
    private static final float BASE_SPEED = 1.0F;
    private static final float SPEED_PER_POWER = 0.04F;
    private static final double POTENCY_BASE = 45.0;
    private static final double POTENCY_PER_POWER = 45.0;
    private static final double COST_BASE = 150.0;
    private static final double COST_PER_POWER = 150.0;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> direction;
    private SpellParam<Number> power;

    public PieceTrickBlazeBall(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel("psi.spellparam.power", true).max(MIN_POWER).mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, true, false));
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, true, false));
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
        Vec3 from = CombatSpellHelper.optionalPosition(context, getParamValue(context, position));
        Vec3 aim = CombatSpellHelper.optionalDirection(getParamValue(context, direction));

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        float baseDamage = BASE_DAMAGE + (float) (powerVal * DAMAGE_PER_POWER);
        float finalDamage = AttackRules.scale(baseDamage, PsitweaksConfig.COMMON.blazeBallDamageMultiplier.get(), context.caster);
        float speed = BASE_SPEED + (float) (powerVal * SPEED_PER_POWER);

        EntityBlazeBall projectile = new EntityBlazeBall(PsitweaksEntities.BLAZE_BALL.get(), level);
        projectile.setOwner(context.caster);
        Vec3 flight = aim != null ? aim : context.caster.getLookAngle();
        if (from != null) {
            CombatSpellHelper.placeCentered(projectile, from);
        } else {
            projectile.setPos(
                    context.caster.getX() + flight.x * 0.5,
                    context.caster.getEyeY() - 0.1,
                    context.caster.getZ() + flight.z * 0.5
            );
        }
        if (from == null && aim == null) {
            // Thrown from the caster's hand, it also carries the caster's own motion.
            projectile.shootFromRotation(context.caster, context.caster.getXRot(), context.caster.getYRot(), 0.0F, speed, 0.0F);
        } else {
            projectile.shoot(flight.x, flight.y, flight.z, speed, 0.0F);
        }
        projectile.setDamage(finalDamage);
        projectile.setSafeToPlayers(SpellSafetyUtils.hasSafeToPlayers(context));

        level.addFreshEntity(projectile);
        Vec3 soundAt = from != null ? from : context.caster.position();
        level.playSound(
                null,
                soundAt.x,
                soundAt.y,
                soundAt.z,
                SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.random.nextFloat() * 0.4F + 0.8F)
        );

        if (level instanceof ServerLevel serverLevel) {
            Vec3 flash = from != null ? from
                    : new Vec3(context.caster.getX(), context.caster.getEyeY() - 0.1, context.caster.getZ());
            serverLevel.sendParticles(
                    ParticleTypes.SMALL_FLAME,
                    flash.x,
                    flash.y,
                    flash.z,
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
