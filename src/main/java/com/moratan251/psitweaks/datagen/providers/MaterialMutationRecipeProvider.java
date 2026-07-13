package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class MaterialMutationRecipeProvider implements DataProvider {
    private static final int DEFAULT_MACHINE_TIME = 200;

    private final PackOutput.PathProvider pathProvider;

    public MaterialMutationRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes/material_mutation");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> recipes = new LinkedHashMap<>();

        addItemMutation(recipes, "netherite_scrap", "minecraft:gold_block", "minecraft:netherite_scrap", 1);
        addItemMutation(recipes, "americium_pellet", "psitweaks:plutonium_block", "psitweaks:pellet_americium", 1);
        addItemMutation(recipes, "neptunium_pellet", "psitweaks:polonium_block", "psitweaks:pellet_neptunium", 1);
        addItemMutation(recipes, "hypostasis_gem", "psitweaks:antinite_block", "psitweaks:hypostasis_gem", 1);
        addItemMutation(recipes, "jade", "minecraft:emerald_block", "psitweaks:jade", 9);
        addBlockMutation(recipes, "sculk_sensor", "minecraft:note_block", "minecraft:sculk_sensor", 1);
        addBlockMutation(recipes, "crying_obsidian", "minecraft:obsidian", "minecraft:crying_obsidian", 1);
        addItemMutation(recipes, "ender_pearl", "minecraft:crying_obsidian", "minecraft:ender_pearl", 1);
        addItemMutation(recipes, "end_rod", "minecraft:torch", "minecraft:end_rod", 1);
        addBlockMutation(recipes, "advanced_solar_generator", "minecraft:daylight_detector", "mekanismgenerators:advanced_solar_generator", 1);
        addBlockMutation(recipes, "wind_generator", "minecraft:loom", "mekanismgenerators:wind_generator", 1);
        addBlockMutation(recipes, "heat_generator", "minecraft:campfire", "mekanismgenerators:heat_generator", 1);
        addBlockMutation(recipes, "bio_generator", "minecraft:composter", "mekanismgenerators:bio_generator", 1);
        addBlockMutation(recipes, "mycelium", "minecraft:grass_block", "minecraft:mycelium", 1);
        addItemMutation(recipes, "gold_nugget", "minecraft:glowstone", "minecraft:gold_nugget", 4);
        addItemMutation(recipes, "prismarine_shard", "minecraft:bricks", "minecraft:prismarine_shard", 9);
        addItemMutation(recipes, "prismarine_crystals", "minecraft:glass", "minecraft:prismarine_crystals", 4);
        addBlockMutation(recipes, "sponge", "minecraft:dried_kelp_block", "minecraft:sponge", 1);
        addBlockMutation(recipes, "honey_block", "minecraft:slime_block", "minecraft:honey_block", 1);
        addBlockMutation(recipes, "slime_block", "minecraft:honey_block", "minecraft:slime_block", 1);

        CompletableFuture<?>[] futures = recipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks Material Mutation Recipes";
    }

    private static void addItemMutation(Map<ResourceLocation, JsonObject> recipes, String id, String inputBlock, String outputItem, int outputCount) {
        recipes.put(Psitweaks.location("trick/" + id), trickRecipe(inputBlock, outputItem, outputCount, false));
        recipes.put(Psitweaks.location("machine/" + id), machineRecipe(inputBlock, outputItem, outputCount));
    }

    private static void addBlockMutation(Map<ResourceLocation, JsonObject> recipes, String id, String inputBlock, String outputBlock, int outputCount) {
        recipes.put(Psitweaks.location("trick/" + id), trickRecipe(inputBlock, outputBlock, outputCount, true));
        recipes.put(Psitweaks.location("machine/" + id), machineRecipe(inputBlock, outputBlock, outputCount));
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
}
