package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.EntityDryIceProjectile;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
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
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Fires a dry ice bullet from Position along Direction. Both are optional: without Position the bullet leaves from the
 * caster's eyes, and without Direction it flies the way the caster looks.
 */
public class PieceTrickDryMeteor extends PieceTrick {

    private static final int POTENCY = 275;
    private static final int COST = 550;
    private static final float BASE_DAMAGE = 14.0F;
    private static final float VELOCITY = 1.8F;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> direction;

    public PieceTrickDryMeteor(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, true, false));
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vec3 from = CombatSpellHelper.optionalPosition(context, getParamValue(context, position));
        Vec3 aim = CombatSpellHelper.optionalDirection(getParamValue(context, direction));
        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        float finalDamage = AttackRules.scale(BASE_DAMAGE, PsitweaksConfig.COMMON.dryMeteorDamageMultiplier.get(), context.caster);

        EntityDryIceProjectile projectile = new EntityDryIceProjectile(level, context.caster);
        Vec3 look = aim != null ? aim : context.caster.getLookAngle();
        if (from != null) {
            CombatSpellHelper.placeCentered(projectile, from);
        } else {
            projectile.setPos(context.caster.getX(), context.caster.getEyeY() - 0.1, context.caster.getZ());
        }
        projectile.shoot(look.x, look.y, look.z, VELOCITY, 0.0F);
        projectile.setDamage(finalDamage);
        projectile.setSafeToPlayers(SpellSafetyUtils.hasSafeToPlayers(context));
        level.addFreshEntity(projectile);

        Vec3 soundAt = from != null ? from : context.caster.position();
        level.playSound(null, soundAt.x, soundAt.y, soundAt.z,
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.7F, 0.85F);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 puff = from != null ? from : context.caster.getEyePosition();
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, puff.x, puff.y, puff.z,
                    8, 0.15, 0.15, 0.15, 0.02);
        }

        return null;
    }
}
