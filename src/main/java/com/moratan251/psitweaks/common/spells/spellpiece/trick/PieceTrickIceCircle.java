package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityIceCircle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import vazkii.psi.api.spell.Spell;

/** 作動式: アイスサークル。 */
public class PieceTrickIceCircle extends PieceTrickSpellGramCircleBase<EntityIceCircle> {

    public PieceTrickIceCircle(Spell spell) {
        super(spell, EntityIceCircle.class);
    }

    @Override
    protected EntityIceCircle createCircle(ServerLevel level) {
        return new EntityIceCircle(level);
    }

    @Override
    protected double damageMultiplier() {
        return PsitweaksConfig.COMMON.iceCircleDamageMultiplier.get();
    }

    @Override
    protected SoundEvent placeSound() {
        return SoundEvents.GLASS_BREAK;
    }

    @Override
    protected float placePitch() {
        return 1.2F;
    }

    @Override
    protected ParticleOptions placeParticle() {
        return ParticleTypes.SNOWFLAKE;
    }
}
