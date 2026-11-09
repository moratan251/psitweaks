package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityFlareCircle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import vazkii.psi.api.spell.Spell;

/** 作動式: ファイアサークル。 */
public class PieceTrickFlareCircle extends PieceTrickSpellGramCircleBase<EntityFlareCircle> {

    public PieceTrickFlareCircle(Spell spell) {
        super(spell, EntityFlareCircle.class);
    }

    @Override
    protected EntityFlareCircle createCircle(ServerLevel level) {
        return new EntityFlareCircle(level);
    }

    @Override
    protected double damageMultiplier() {
        return PsitweaksConfig.COMMON.flareCircleDamageMultiplier.get();
    }

    @Override
    protected SoundEvent placeSound() {
        return SoundEvents.BLAZE_SHOOT;
    }

    @Override
    protected float placePitch() {
        return 0.9F;
    }

    @Override
    protected ParticleOptions placeParticle() {
        return ParticleTypes.FLAME;
    }
}
