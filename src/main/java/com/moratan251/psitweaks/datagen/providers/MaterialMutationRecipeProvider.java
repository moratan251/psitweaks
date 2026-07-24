package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class MaterialMutationRecipeProvider implements DataProvider {
    private static final int DEFAULT_MACHINE_TIME = 200;

    private final PackOutput.PathProvider pathProvider;

    public MaterialMutationRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes/material_mutation");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> recipes = new LinkedHashMap<>();

        addItemMutation(recipes, "netherite_scrap", Blocks.GOLD_BLOCK, Items.NETHERITE_SCRAP, 1);
        addItemMutation(recipes, "americium_pellet", PsitweaksBlocks.PLUTONIUM_BLOCK.get(),
                PsitweaksItems.PELLET_AMERICIUM, 1);
        addItemMutation(recipes, "neptunium_pellet", PsitweaksBlocks.POLONIUM_BLOCK.get(),
                PsitweaksItems.PELLET_NEPTUNIUM, 1);
        addItemMutation(recipes, "hypostasis_gem", PsitweaksBlocks.ANTINITE_BLOCK.get(),
                PsitweaksItems.HYPOSTASIS_GEM, 1);
        addItemMutation(recipes, "jade", Blocks.EMERALD_BLOCK, PsitweaksItems.JADE, 9);
        addBlockMutation(recipes, "sculk_sensor", Blocks.NOTE_BLOCK, Blocks.SCULK_SENSOR, 1);
        addBlockMutation(recipes, "crying_obsidian", Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, 1);
        addItemMutation(recipes, "ender_pearl", Blocks.CRYING_OBSIDIAN, Items.ENDER_PEARL, 1);
        addItemMutation(recipes, "end_rod", Blocks.TORCH, Items.END_ROD, 1);
        addExternalBlockMutation(recipes, "advanced_solar_generator", Blocks.DAYLIGHT_DETECTOR,
                mekanismGenerators("advanced_solar_generator"), 1);
        addExternalBlockMutation(recipes, "wind_generator", Blocks.LOOM,
                mekanismGenerators("wind_generator"), 1);
        addExternalBlockMutation(recipes, "heat_generator", Blocks.CAMPFIRE,
                mekanismGenerators("heat_generator"), 1);
        addExternalBlockMutation(recipes, "bio_generator", Blocks.COMPOSTER,
                mekanismGenerators("bio_generator"), 1);
        addBlockMutation(recipes, "mycelium", Blocks.GRASS_BLOCK, Blocks.MYCELIUM, 1);
        addItemMutation(recipes, "gold_nugget", Blocks.GLOWSTONE, Items.GOLD_NUGGET, 4);
        addItemMutation(recipes, "prismarine_shard", Blocks.BRICKS, Items.PRISMARINE_SHARD, 4);
        addItemMutation(recipes, "prismarine_crystals", Blocks.GLASS, Items.PRISMARINE_CRYSTALS, 4);
        addBlockMutation(recipes, "sponge", Blocks.DRIED_KELP_BLOCK, Blocks.SPONGE, 1);
        addBlockMutation(recipes, "honey_block", Blocks.SLIME_BLOCK, Blocks.HONEY_BLOCK, 1);
        addBlockMutation(recipes, "slime_block", Blocks.HONEY_BLOCK, Blocks.SLIME_BLOCK, 1);

        CompletableFuture<?>[] futures = recipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks Material Mutation Recipes";
    }

    private static void addItemMutation(Map<ResourceLocation, JsonObject> recipes, String id, Block inputBlock,
                                        ItemLike outputItem, int outputCount) {
        recipes.put(Psitweaks.location("trick/" + id),
                trickRecipe(blockId(inputBlock), itemId(outputItem), outputCount, false));
        recipes.put(Psitweaks.location("machine/" + id),
                machineRecipe(itemId(inputBlock), itemId(outputItem), outputCount));
    }

    private static void addBlockMutation(Map<ResourceLocation, JsonObject> recipes, String id, Block inputBlock,
                                         Block outputBlock, int outputCount) {
        recipes.put(Psitweaks.location("trick/" + id),
                trickRecipe(blockId(inputBlock), blockId(outputBlock), outputCount, true));
        recipes.put(Psitweaks.location("machine/" + id),
                machineRecipe(itemId(inputBlock), itemId(outputBlock), outputCount));
    }

    private static void addExternalBlockMutation(Map<ResourceLocation, JsonObject> recipes, String id,
                                                 Block inputBlock, ResourceLocation outputBlock, int outputCount) {
        String outputId = outputBlock.toString();
        recipes.put(Psitweaks.location("trick/" + id),
                trickRecipe(blockId(inputBlock), outputId, outputCount, true));
        recipes.put(Psitweaks.location("machine/" + id),
                machineRecipe(itemId(inputBlock), outputId, outputCount));
    }

    private static JsonObject trickRecipe(String inputBlock, String output, int outputCount, boolean outputIsBlock) {
        JsonObject root = new JsonObject();
        JsonArray mutations = new JsonArray();
        JsonObject mutation = new JsonObject();
        JsonObject input = new JsonObject();
        JsonObject outputObject = new JsonObject();

        input.addProperty("block", inputBlock);
        mutation.add("input", input);
        outputObject.addProperty(outputIsBlock ? "block" : "item", output);
        outputObject.addProperty("count", outputCount);
        mutation.add("output", outputObject);
        mutations.add(mutation);
        root.add("mutations", mutations);

        return root;
    }

    private static JsonObject machineRecipe(String inputBlockItem, String outputItem, int outputCount) {
        JsonObject root = new JsonObject();
        JsonObject input = new JsonObject();
        JsonObject output = new JsonObject();

        input.addProperty("item", inputBlockItem);
        root.add("input", input);
        output.addProperty("item", outputItem);
        output.addProperty("count", outputCount);
        root.add("output", output);
        root.addProperty("time", DEFAULT_MACHINE_TIME);

        return root;
    }

    private static String itemId(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private static String blockId(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static ResourceLocation mekanismGenerators(String path) {
        return ResourceLocation.fromNamespaceAndPath("mekanismgenerators", path);
    }
}
