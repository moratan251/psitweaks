package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.datagen.builders.MaterialMutationRecipeBuilder;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class MaterialMutationRecipeProvider implements DataProvider {

    private static final int DEFAULT_MACHINE_TIME = 200;

    private final PackOutput.PathProvider pathProvider;

    public MaterialMutationRecipeProvider(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipes/material_mutation");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Map<ResourceLocation, JsonObject> generatedRecipes = new LinkedHashMap<>();
        addRecipes((recipeId, json) -> {
            JsonObject previous = generatedRecipes.put(recipeId, json);
            if (previous != null) {
                throw new IllegalStateException("Duplicate material mutation recipe id: " + recipeId);
            }
        });

        List<CompletableFuture<?>> futures = new ArrayList<>();
        generatedRecipes.forEach((recipeId, json) ->
                futures.add(DataProvider.saveStable(cachedOutput, json, pathProvider.json(recipeId))));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void addRecipes(BiConsumer<ResourceLocation, JsonObject> consumer) {
        addItemMutation(consumer, "netherite_scrap", Blocks.GOLD_BLOCK, Items.NETHERITE_SCRAP, 4);
        addItemMutation(consumer, "americium_pellet", PsitweaksBlocks.PLUTONIUM_BLOCK.get(), PsitweaksItems.PELLET_AMERICIUM.get(), 1);
        addItemMutation(consumer, "neptunium_pellet", PsitweaksBlocks.POLONIUM_BLOCK.get(), PsitweaksItems.PELLET_NEPTUNIUM.get(), 1);
        addItemMutation(consumer, "hypostasis_gem", PsitweaksBlocks.ANTINITE_BLOCK.get(), PsitweaksItems.HYPOSTASIS_GEM.get(), 1);
        addBlockMutation(consumer, "sculk_sensor", Blocks.NOTE_BLOCK, Blocks.SCULK_SENSOR, 1);
        addBlockMutation(consumer, "crying_obsidian", Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, 1);
        addItemMutation(consumer, "ender_pearl", Blocks.CRYING_OBSIDIAN, Items.ENDER_PEARL, 1);
        addBlockMutation(consumer, "amethyst_block", Blocks.DIAMOND_BLOCK, Blocks.AMETHYST_BLOCK, 1);
        addBlockMutation(consumer, "emerald_block", Blocks.AMETHYST_BLOCK, Blocks.EMERALD_BLOCK, 1);
        addBlockMutation(consumer, "diamond_block", Blocks.EMERALD_BLOCK, Blocks.DIAMOND_BLOCK, 1);
        addItemMutation(consumer, "end_rod", Blocks.TORCH, Items.END_ROD, 1);
        addBlockMutation(consumer, "advanced_solar_generator", Blocks.DAYLIGHT_DETECTOR, GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getBlock(), 1);
        addBlockMutation(consumer, "wind_generator", Blocks.LOOM, GeneratorsBlocks.WIND_GENERATOR.getBlock(), 1);
        addBlockMutation(consumer, "heat_generator", Blocks.CAMPFIRE, GeneratorsBlocks.HEAT_GENERATOR.getBlock(), 1);
        addBlockMutation(consumer, "bio_generator", Blocks.COMPOSTER, GeneratorsBlocks.BIO_GENERATOR.getBlock(), 1);
        addBlockMutation(consumer, "mycelium", Blocks.GRASS_BLOCK, Blocks.MYCELIUM, 1);
        addBlockMutation(consumer, "diamond_block_1", Blocks.COAL_BLOCK, Blocks.DIAMOND_BLOCK, 1);
        addBlockMutation(consumer, "gold_block", Blocks.GLOWSTONE, Blocks.GOLD_BLOCK, 1);
        addBlockMutation(consumer, "sponge", Blocks.DRIED_KELP_BLOCK, Blocks.SPONGE, 1);
        addBlockMutation(consumer, "honey_block", Blocks.SLIME_BLOCK, Blocks.HONEY_BLOCK, 1);
        addBlockMutation(consumer, "slime_block", Blocks.HONEY_BLOCK, Blocks.SLIME_BLOCK, 1);
    }

    private static void addItemMutation(BiConsumer<ResourceLocation, JsonObject> consumer,
                                        String idPath,
                                        Block inputBlock,
                                        ItemLike outputItem,
                                        int outputCount) {
        MaterialMutationRecipeBuilder.trick(inputBlock)
                .outputItem(outputItem, outputCount)
                .save(consumer, Psitweaks.location("trick/" + idPath));

        MaterialMutationRecipeBuilder.machine(requireItemForm(inputBlock, idPath), outputItem, DEFAULT_MACHINE_TIME)
                .outputCount(outputCount)
                .save(consumer, Psitweaks.location("machine/" + idPath));
    }

    private static void addBlockMutation(BiConsumer<ResourceLocation, JsonObject> consumer,
                                         String idPath,
                                         Block inputBlock,
                                         Block outputBlock,
                                         int outputCount) {
        MaterialMutationRecipeBuilder.trick(inputBlock)
                .outputBlock(outputBlock, outputCount)
                .save(consumer, Psitweaks.location("trick/" + idPath));

        MaterialMutationRecipeBuilder.machine(requireItemForm(inputBlock, idPath), requireItemForm(outputBlock, idPath), DEFAULT_MACHINE_TIME)
                .outputCount(outputCount)
                .save(consumer, Psitweaks.location("machine/" + idPath));
    }

    private static ItemLike requireItemForm(Block block, String idPath) {
        Item item = block.asItem();
        if (item == Items.AIR) {
            throw new IllegalStateException("Material mutation machine recipe '" + idPath + "' requires a block with an item form");
        }
        return item;
    }

    @Override
    public String getName() {
        return "PsiTweaks Material Mutation Recipes";
    }
}
