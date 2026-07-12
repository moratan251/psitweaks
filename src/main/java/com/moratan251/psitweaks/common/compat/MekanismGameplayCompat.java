package com.moratan251.psitweaks.common.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MekanismGameplayCompat {
    private MekanismGameplayCompat() {
    }

    public static SoundEvent laserSound() {
        return MekanismCompat.isMekanismLoaded()
                ? MekanismGameplayIntegration.laserSound()
                : SoundEvents.BEACON_ACTIVATE;
    }

    public static ResourceKey<DamageType> laserDamageType() {
        return MekanismCompat.isMekanismLoaded()
                ? MekanismGameplayIntegration.laserDamageType()
                : DamageTypes.MAGIC;
    }

    public static void supplyLaserEnergy(BlockEntity blockEntity, long energy) {
        if (MekanismCompat.isMekanismLoaded()) {
            MekanismGameplayIntegration.supplyLaserEnergy(blockEntity, energy);
        }
    }
}
