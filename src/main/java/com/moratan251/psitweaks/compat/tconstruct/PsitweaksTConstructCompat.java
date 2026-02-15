package com.moratan251.psitweaks.compat.tconstruct;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class PsitweaksTConstructCompat {
    private static final CastingAssistEventHandler CASTING_ASSIST_EVENT_HANDLER = new CastingAssistEventHandler();
    private static boolean initialized = false;

    private PsitweaksTConstructCompat() {
    }

    public static void register(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        initialized = true;

        PsitweaksTConstructFluids.register(modEventBus);
        PsitweaksTConstructModifiers.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(CASTING_ASSIST_EVENT_HANDLER);
    }
}
