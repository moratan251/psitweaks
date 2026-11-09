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

/** 作動式: アイスサークルの円。凍結属性のダメージを与え、凍えさせる。 */
public class EntityIceCircle extends SpellGramCircle {

    private static final int FREEZE_TICKS_PER_HIT = 140;

    public EntityIceCircle(EntityType<? extends EntityIceCircle> type, Level level) {
        super(type, level);
    }

    public EntityIceCircle(Level level) {
        this(PsitweaksEntities.ICE_CIRCLE.get(), level);
    }

    @Override
    protected ResourceKey<DamageType> damageType() {
        return PsitweaksDamageTypes.ICE_CIRCLE;
    }

    @Override
    protected ParticleOptions ringParticle() {
        return ParticleTypes.SNOWFLAKE;
    }

    @Override
    protected ParticleOptions innerParticle() {
        return ParticleTypes.ITEM_SNOWBALL;
    }

    @Override
    protected void onAreaDamageApplied(LivingEntity target, float damage, DamageSource source) {
        // 当てるたびに足すが、凍り切る量（getTicksRequiredToFreeze）に1回分を足した所で止める。上限がないと、円が消えた後も
        // 当てた回数だけ長く凍ったままになっていた。
        int cap = target.getTicksRequiredToFreeze() + FREEZE_TICKS_PER_HIT;
        target.setTicksFrozen(Math.min(cap, target.getTicksFrozen() + FREEZE_TICKS_PER_HIT));
    }
}
