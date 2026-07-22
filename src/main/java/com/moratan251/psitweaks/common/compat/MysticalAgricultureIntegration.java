package com.moratan251.psitweaks.common.compat;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTextures;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

@MysticalAgriculturePlugin
public final class MysticalAgricultureIntegration implements IMysticalAgriculturePlugin {
    private static final String MOD_ID = "psitweaks";

    private static final Crop PSIDUST = crop(
            "psidust", CropTier.THREE, CropTextures.DUST_CROP_TEXTURES, 0xDADCFB, "psi:psidust");
    private static final Crop PSIMETAL = crop(
            "psimetal", CropTier.FOUR, CropTextures.INGOT_CROP_TEXTURES, 0xC0C2FF, "psi:psimetal");
    private static final Crop EBONY_PSIMETAL = crop(
            "ebony_psimetal", CropTier.FOUR, CropTextures.INGOT_CROP_TEXTURES, 0x202020, "psi:ebony_psimetal");
    private static final Crop IVORY_PSIMETAL = crop(
            "ivory_psimetal", CropTier.FOUR, CropTextures.INGOT_CROP_TEXTURES, 0xF6F6E9, "psi:ivory_psimetal");
    private static final Crop PSIGEM = crop(
            "psigem", CropTier.FIVE, CropTextures.GEM_CROP_TEXTURES, 0x6855B4, "psi:psigem");
    private static final Crop CHAOTIC_PSIMETAL = crop(
            "chaotic_psimetal", CropTier.FIVE, CropTextures.INGOT_CROP_TEXTURES, 0x7F7F7F, "psitweaks:chaotic_psimetal");
    private static final Crop FLASHMETAL = crop(
            "flashmetal", CropTier.FIVE, CropTextures.INGOT_CROP_TEXTURES, 0xFFE86D, "psitweaks:flashmetal");
    private static final Crop HEAVY_PSIMETAL = crop(
            "heavy_psimetal", CropTier.FIVE, CropTextures.INGOT_CROP_TEXTURES, 0x294C73, "psitweaks:heavy_psimetal");
    private static final Crop ANTINITE = crop(
            "antinite", CropTier.FIVE, CropTextures.INGOT_CROP_TEXTURES, 0xCCBF61, "psitweaks:antinite_ingot");

    @Override
    public void onRegisterCrops(ICropRegistry registry) {
        registry.register(PSIDUST);
        registry.register(PSIMETAL);
        registry.register(EBONY_PSIMETAL);
        registry.register(IVORY_PSIMETAL);
        registry.register(PSIGEM);
        registry.register(CHAOTIC_PSIMETAL);
        registry.register(FLASHMETAL);
        registry.register(HEAVY_PSIMETAL);
        registry.register(ANTINITE);
        if (ModList.get().isLoaded("mysticalagradditions")) {
            MysticalAgradditionsIntegration.onRegisterCrops(registry);
        }
    }

    private static Crop crop(String name, CropTier tier, CropTextures textures, int color, String material) {
        return new Crop(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, name),
                tier,
                CropType.RESOURCE,
                textures,
                color,
                LazyIngredient.item(material)
        ).setDisplayName(Component.translatable("crop." + MOD_ID + "." + name));
    }
}
