package com.moratan251.psitweaks.common.compat;

import com.blakebr0.mysticalagradditions.lib.ModCropTiers;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTextures;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class MysticalAgradditionsIntegration {
    private static final String MOD_ID = "psitweaks";

    private static final Crop PSYCHEONIC_METAL = new Crop(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "psycheonic_metal"),
            ModCropTiers.SIX,
            CropType.RESOURCE,
            CropTextures.INGOT_CROP_TEXTURES,
            0x7ED8E6,
            LazyIngredient.item("psitweaks:psycheonic_metal_ingot")
    ).setCruxBlock(() -> PsitweaksBlocks.PSYCHEONIC_METAL_CRUX.get())
            .setDisplayName(Component.translatable("crop." + MOD_ID + ".psycheonic_metal"));

    private MysticalAgradditionsIntegration() {
    }

    static void onRegisterCrops(ICropRegistry registry) {
        registry.register(PSYCHEONIC_METAL);
    }
}
