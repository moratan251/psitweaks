package com.moratan251.psitweaks.client.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class PsitweaksConfigScreenRegistration {
    private PsitweaksConfigScreenRegistration() {
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, parent) -> new ConfigurationScreen(container, parent));
    }
}
