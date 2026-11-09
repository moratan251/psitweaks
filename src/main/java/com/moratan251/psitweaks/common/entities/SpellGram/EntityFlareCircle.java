package com.moratan251.psitweaks.common.entities.SpellGram;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** 作動式: ファイアサークルの円。火属性のダメージを与え、2 秒燃やす。 */
public class EntityFlareCircle extends SpellGramCircle {

    public EntityFlareCircle(EntityType<? extends EntityFlareCircle> type, Level level) {
        super(type, level);
    }

    public EntityFlareCircle(Level level) {
        this(PsitweaksEntities.FLARE_CIRCLE.get(), level);
    }

    @Override
    protected ResourceKey<DamageType> damageType() {
        return PsitweaksDamageTypes.FLARE_CIRCLE;
    }

    @Override
    protected ParticleOptions ringParticle() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected ParticleOptions innerParticle() {
        return ParticleTypes.SMALL_FLAME;
    }

    @Override
    protected void onAreaDamageApplied(LivingEntity target, float damage, DamageSource source) {
        target.igniteForSeconds(2);
    }
}
