package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class MysticalAgricultureModelProvider implements DataProvider {
    private static final String MOD_ID = "mysticalagriculture";
    private static final List<CropModel> CROPS = List.of(
            new CropModel("psidust", "block/flower_dust", "item/essence_dust"),
            new CropModel("psimetal", "block/flower_ingot", "item/essence_ingot"),
            new CropModel("ebony_psimetal", "block/flower_ingot", "item/essence_ingot"),
            new CropModel("ivory_psimetal", "block/flower_ingot", "item/essence_ingot"),
            new CropModel("psigem", "block/flower_rock", "item/essence_gem"),
            new CropModel("chaotic_psimetal", "block/flower_ingot", "item/essence_ingot"),
            new CropModel("flashmetal", "block/flower_ingot", "item/essence_ingot"),
            new CropModel("heavy_psimetal", "block/flower_ingot", "item/essence_ingot"),
            new CropModel("antinite", "block/flower_ingot", "item/essence_ingot")
    );

    private final List<CropModel> crops;
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider blockModelPathProvider;
    private final PackOutput.PathProvider itemModelPathProvider;

    public MysticalAgricultureModelProvider(PackOutput output, boolean includeMysticalAgradditionsCrop) {
        this.crops = new ArrayList<>(CROPS);
        if (includeMysticalAgradditionsCrop) {
            this.crops.add(this.crops.size() - 1,
                    new CropModel("psycheonic_metal", "block/flower_ingot", "item/essence_ingot"));
        }
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.blockModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.itemModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (CropModel crop : crops) {
            ResourceLocation blockId = id(crop.name() + "_crop");
            futures.add(DataProvider.saveStable(output, blockState(crop.name()), blockStatePathProvider.json(blockId)));
            futures.add(DataProvider.saveStable(output, blockModel(crop.flowerTexture()), blockModelPathProvider.json(blockId)));
            futures.add(DataProvider.saveStable(output,
                    itemModel(crop.essenceTexture()), itemModelPathProvider.json(id(crop.name() + "_essence"))));
            futures.add(DataProvider.saveStable(output,
                    itemModel("item/mystical_seeds"), itemModelPathProvider.json(id(crop.name() + "_seeds"))));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks Mystical Agriculture models";
    }

    private static JsonObject blockState(String name) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (int age = 0; age <= 6; age++) {
            variants.add("age=" + age, modelVariant("mysticalagriculture:block/mystical_resource_crop_" + age));
        }
        variants.add("age=7", modelVariant("mysticalagriculture:block/" + name + "_crop"));
        root.add("variants", variants);
        return root;
    }

    private static JsonObject modelVariant(String model) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", model);
        return variant;
    }

    private static JsonObject blockModel(String flowerTexture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "mysticalagriculture:block/mystical_resource_crop_7");
        JsonObject textures = new JsonObject();
        textures.addProperty("flower", "mysticalagriculture:" + flowerTexture);
        root.add("textures", textures);
        return root;
    }

    private static JsonObject itemModel(String texture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "mysticalagriculture:" + texture);
        root.add("textures", textures);
        return root;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private record CropModel(String name, String flowerTexture, String essenceTexture) {
    }
}
