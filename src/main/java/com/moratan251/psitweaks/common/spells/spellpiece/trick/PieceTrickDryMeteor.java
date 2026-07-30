package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.EntityDryIceProjectile;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
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
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickDryMeteor extends PieceTrick {

    private static final int POTENCY = 275;
    private static final int COST = 550;
    private static final float BASE_DAMAGE = 14.0F;
    private static final float VELOCITY = 1.8F;

    public PieceTrickDryMeteor(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        double perSpellDamageMultiplier = PsitweaksConfig.COMMON.dryMeteorDamageMultiplier.get();
        double globalDamageMultiplier = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        double spellDamageFactor = context.caster.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR);
        float finalDamage = (float) (BASE_DAMAGE
                * perSpellDamageMultiplier
                * globalDamageMultiplier
                * spellDamageFactor);

        EntityDryIceProjectile projectile = new EntityDryIceProjectile(level, context.caster);
        Vec3 look = context.caster.getLookAngle();
        projectile.setPos(context.caster.getX(), context.caster.getEyeY() - 0.1, context.caster.getZ());
        projectile.shoot(look.x, look.y, look.z, VELOCITY, 0.0F);
        projectile.setDamage(finalDamage);
        projectile.setSafeToPlayers(SpellSafetyUtils.hasSafeToPlayers(context));
        level.addFreshEntity(projectile);

        level.playSound(null, context.caster.getX(), context.caster.getY(), context.caster.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.7F, 0.85F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    context.caster.getX(), context.caster.getEyeY(), context.caster.getZ(),
                    8, 0.15, 0.15, 0.15, 0.02);
        }

        return null;
    }
}
