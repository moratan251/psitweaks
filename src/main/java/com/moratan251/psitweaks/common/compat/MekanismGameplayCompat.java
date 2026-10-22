package com.moratan251.psitweaks.common.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

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

    /** {@code caster} is awarded the hidden advancement if this heat lights a Fusion Reactor. */
    public static void supplyLaserEnergy(BlockEntity blockEntity, long energy, @Nullable ServerPlayer caster) {
        if (MekanismCompat.isMekanismLoaded()) {
            if (caster != null && MekanismCompat.isGeneratorsLoaded()) {
                MekanismGeneratorsGameplayIntegration.watchFusionIgnition(blockEntity, caster);
            }
            MekanismGameplayIntegration.supplyLaserEnergy(blockEntity, energy);
        }
    }
}
