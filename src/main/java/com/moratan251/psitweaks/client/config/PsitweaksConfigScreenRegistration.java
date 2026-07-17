package com.moratan251.psitweaks.client.config;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class PsitweaksConfigScreenRegistration {
    private PsitweaksConfigScreenRegistration() {
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(PsitweaksConfigScreen::new));
    }
}
