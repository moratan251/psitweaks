package com.moratan251.psitweaks.common.compat;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class MekanismCompat {
    private static final String MEKANISM_MOD_ID = "mekanism";
    private static final String GENERATORS_MOD_ID = "mekanismgenerators";

    private MekanismCompat() {
    }

    public static boolean isMekanismLoaded() {
        return ModList.get().isLoaded(MEKANISM_MOD_ID);
    }

    public static boolean isGeneratorsLoaded() {
        return isMekanismLoaded() && ModList.get().isLoaded(GENERATORS_MOD_ID);
    }

    public static void register(IEventBus modEventBus) {
        if (isMekanismLoaded()) {
            MekanismIntegration.register(modEventBus);
        }
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        if (isMekanismLoaded()) {
            MekanismIntegration.commonSetup(event);
        }
    }

    public static void enqueueIMC() {
        if (isMekanismLoaded()) {
            MekanismIntegration.enqueueIMC();
        }
    }

    public static void registerClient(IEventBus modEventBus) {
        if (isMekanismLoaded()) {
            MekanismIntegration.registerClient(modEventBus);
        }
    }

    public static void registerNetworkMessages(int firstMessageId) {
        if (isMekanismLoaded()) {
            MekanismIntegration.registerNetworkMessages(firstMessageId);
        }
    }

    public static void addCreativeTabContents(CreativeModeTab.Output output) {
        if (isMekanismLoaded()) {
            MekanismIntegration.addCreativeTabContents(output);
        }
    }

    public static SoundEvent laserSound() {
        return isMekanismLoaded() ? MekanismIntegration.laserSound() : SoundEvents.BEACON_ACTIVATE;
    }

    public static net.minecraft.resources.ResourceKey<DamageType> laserDamageType() {
        return isMekanismLoaded() ? MekanismIntegration.laserDamageType() : DamageTypes.MAGIC;
    }

    public static void supplyLaserEnergy(BlockEntity blockEntity, long energy) {
        if (isMekanismLoaded()) {
            MekanismIntegration.supplyLaserEnergy(blockEntity, energy);
        }
    }
}
