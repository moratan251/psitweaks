package com.moratan251.psitweaks.common.effects;

import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EffectCarbonPoisoning extends MobEffect {

    private static final int DAMAGE_INTERVAL_TICKS = 20;
    private static final float DAMAGE_PER_TICK = 2.0F;
    private static final float MINIMUM_HEALTH = 1.0F;

    public EffectCarbonPoisoning() {
        super(MobEffectCategory.HARMFUL, 0x424242);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        float damage = Math.min(DAMAGE_PER_TICK, entity.getHealth() - MINIMUM_HEALTH);
        if (damage > 0.0F) {
            entity.hurt(createDamageSource(entity), damage);
            entity.invulnerableTime = 0;
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % DAMAGE_INTERVAL_TICKS == 0;
    }

    private static DamageSource createDamageSource(LivingEntity entity) {
        Holder<DamageType> damageType = entity.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(PsitweaksDamageTypes.CARBON_POISONING);
        return new DamageSource(damageType);
    }
}
