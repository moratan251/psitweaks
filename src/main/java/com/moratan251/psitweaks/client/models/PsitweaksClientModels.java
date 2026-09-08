package com.moratan251.psitweaks.client.models;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;

public final class PsitweaksClientModels {
    private static final String[] CAD_MODEL_PATHS = {
            "cad_alloy_psion",
            "cad_chaotic_psimetal",
            "cad_flashmetal",
            "cad_heavy_psimetal_alpha",
            "cad_heavy_psimetal_beta",
            "cad_psycheonicmetal",
            "cad_inline_1",
            "cad_inline_2",
            "cad_inline_3"
    };

    private PsitweaksClientModels() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PsitweaksClientModels::registerAdditional);
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        for (String path : CAD_MODEL_PATHS) {
            event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("psi", "item/" + path)));
        }
    }
}
