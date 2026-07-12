package com.moratan251.psitweaks.common.compat;

import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

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

    public static void registerClient(IEventBus modEventBus) {
        if (isMekanismLoaded()) {
            MekanismIntegration.registerClient(modEventBus);
        }
    }

    public static void addCreativeTabContents(CreativeModeTab.Output output) {
        if (isMekanismLoaded()) {
            MekanismIntegration.addCreativeTabContents(output);
        }
    }
}
