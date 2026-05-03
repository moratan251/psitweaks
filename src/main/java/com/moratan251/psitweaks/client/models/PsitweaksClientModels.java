package com.moratan251.psitweaks.client.models;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import vazkii.psi.client.model.ModelCAD;
import vazkii.psi.common.item.base.ModItems;

@EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getModels().computeIfPresent(
                ModelResourceLocation.inventory(ModItems.cad.getId()),
                (key, oldModel) -> new ModelCAD()
        );
    }

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        for (String path : CAD_MODEL_PATHS) {
            event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("psi", "item/" + path)));
        }
    }
}
