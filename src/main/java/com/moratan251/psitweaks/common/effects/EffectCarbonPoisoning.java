package com.moratan251.psitweaks.common.effects;

import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class EffectCarbonPoisoning extends MobEffect {

    private static final int DAMAGE_INTERVAL_TICKS = 20;
    private static final float DAMAGE_PER_TICK = 2.0F;
    private static final float MINIMUM_HEALTH = 1.0F;

    private static final int SLOWNESS_AMPLIFIER = 3;
    private static final int NAUSEA_AMPLIFIER = 1;
    private static final int BLINDNESS_AMPLIFIER = 0;

    public EffectCarbonPoisoning() {
        super(MobEffectCategory.HARMFUL, 0x424242);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        MobEffectInstance carbonPoisoning = entity.getEffect(PsitweaksEffects.CARBON_POISONING);
        if (carbonPoisoning == null) {
            return;
        }

        int duration = carbonPoisoning.getDuration();
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, SLOWNESS_AMPLIFIER));
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, NAUSEA_AMPLIFIER));
        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, BLINDNESS_AMPLIFIER));
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return true;
        }

        float damage = Math.min(DAMAGE_PER_TICK, entity.getHealth() - MINIMUM_HEALTH);
        if (damage > 0.0F) {
            entity.hurt(createDamageSource(entity), damage);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % DAMAGE_INTERVAL_TICKS == 0;
    }

    private static DamageSource createDamageSource(LivingEntity entity) {
        Holder<DamageType> damageType = entity.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(PsitweaksDamageTypes.CARBON_POISONING);
        return new DamageSource(damageType);
    }
}
