package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class PsiTweaksMekanismRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public PsiTweaksMekanismRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> recipes = new LinkedHashMap<>();

        addChemicalSupportRecipes(recipes);
        addPsiAlternativeRecipes(recipes);
        addEnrichingRecipes(recipes);
        addOreProcessingRecipes(recipes);
        addMetallurgicInfusingRecipes(recipes);

        CompletableFuture<?>[] futures = recipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks Mekanism recipes";
    }

    private static void addChemicalSupportRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "chemical_conversion/psigem", chemicalConversion(
                itemInput(item("enriched_psigem")), chemical("infuse_psigem"), 80));
        recipe(recipes, "chemical_conversion/ebony", chemicalConversion(
                itemInput(item("enriched_ebony")), chemical("infuse_ebony"), 80));
        recipe(recipes, "chemical_conversion/ivory", chemicalConversion(
                itemInput(item("enriched_ivory")), chemical("infuse_ivory"), 80));
        recipe(recipes, "chemical_conversion/chaotic_factor", chemicalConversion(
                itemInput(item("chaotic_factor")), chemical("infuse_chaotic_factor"), 80));
        recipe(recipes, "chemical_conversion/psionic_echo", chemicalConversion(
                itemInput(item("enriched_echo")), chemical("infuse_psionic_echo"), 80));
        recipe(recipes, "chemical_conversion/hypostasis", chemicalConversion(
                itemInput(item("enriched_hypostasis")), chemical("infuse_hypostasis"), 80));

        recipe(recipes, "oxidizing/psionic_echo", itemToChemical(
                "mekanism:oxidizing",
                itemInput(item("psionic_echo")),
                chemical("gas_psionic_echo"), 1_000));
        recipe(recipes, "reaction/oxygen_echo", reaction(
                fluidTagInput("c:oxygen", 200),
                chemicalInput(chemical("gas_psionic_echo"), 100),
                itemInput("mekanism:substrate", 4),
                chemicalOutput(chemical("gas_peo_fuel"), 300),
                itemOutput(item("echo_pellet"), 1),
                100));
        recipe(recipes, "washing/clean_antinite_slurry", washing(
                fluidTagInput("minecraft:water", 5),
                chemicalInput(chemical("dirty_antinite"), 1),
                chemical("clean_antinite"), 1));
        recipe(recipes, "dissolution/dirty_antinite_slurry_from_ore", dissolution(
                chemicalInput("mekanism:sulfuric_acid", 1),
                tagInput("c:ores/antinite"),
                chemical("dirty_antinite"), 1_000));
        recipe(recipes, "dissolution/dirty_antinite_slurry_from_raw_block", dissolution(
                chemicalInput("mekanism:sulfuric_acid", 2),
                tagInput("c:storage_blocks/raw_antinite"),
                chemical("dirty_antinite"), 6_000));
        recipe(recipes, "dissolution/dirty_antinite_slurry_from_raw_ore", dissolution(
                chemicalInput("mekanism:sulfuric_acid", 1),
                tagInput("c:raw_materials/antinite", 3),
                chemical("dirty_antinite"), 2_000));
        recipe(recipes, "crystallizing/antinite_crystal_from_slurry", crystallizing(
                chemicalInput(chemical("clean_antinite"), 200),
                item("crystal_antinite"), 1));
    }

    private static void addPsiAlternativeRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "metallurgic_infusing/psidust", metallurgicInfusing(
                chemicalInput(chemical("infuse_psigem"), 1),
                itemInput("minecraft:redstone"),
                "psi:psidust", 1));
        recipe(recipes, "metallurgic_infusing/psigem", metallurgicInfusing(
                chemicalInput(chemical("infuse_psigem"), 1),
                itemInput("minecraft:diamond"),
                "psi:psigem", 1));
        recipe(recipes, "metallurgic_infusing/psimetal", metallurgicInfusing(
                chemicalInput(chemical("infuse_psigem"), 1),
                itemInput("minecraft:gold_ingot"),
                "psi:psimetal", 1));
        recipe(recipes, "metallurgic_infusing/ebony_substance", metallurgicInfusing(
                chemicalInput(chemical("infuse_ebony"), 10),
                tagInput("minecraft:coals"),
                "psi:ebony_substance", 1));
        recipe(recipes, "metallurgic_infusing/ivory_substance", metallurgicInfusing(
                chemicalInput(chemical("infuse_ivory"), 10),
                itemInput("minecraft:quartz"),
                "psi:ivory_substance", 1));
        recipe(recipes, "metallurgic_infusing/ebony_psimetal", metallurgicInfusing(
                chemicalInput(chemical("infuse_ebony"), 320),
                itemInput("psi:psimetal"),
                "psi:ebony_psimetal", 1));
        recipe(recipes, "metallurgic_infusing/ivory_psimetal", metallurgicInfusing(
                chemicalInput(chemical("infuse_ivory"), 320),
                itemInput("psi:psimetal"),
                "psi:ivory_psimetal", 1));

        recipe(recipes, "combining/echo_shard", combining(
                itemInput("minecraft:amethyst_shard"),
                itemInput("minecraft:sculk", 8),
                "minecraft:echo_shard", 1));
        recipe(recipes, "reaction/amethyst", reaction(
                fluidTagInput("minecraft:water", 100),
                chemicalInput(chemical("gas_psionic_echo"), 50),
                itemInput("minecraft:amethyst_shard"),
                itemOutput("minecraft:amethyst_shard", 2),
                800));
    }

    private static void addEnrichingRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "enriching/enriched_psigem", itemToItem(
                "mekanism:enriching", itemInput("psi:psigem"), item("enriched_psigem"), 1));
        recipe(recipes, "enriching/enriched_ebony", itemToItem(
                "mekanism:enriching", itemInput("psi:ebony_substance"), item("enriched_ebony"), 1));
        recipe(recipes, "enriching/enriched_ivory", itemToItem(
                "mekanism:enriching", itemInput("psi:ivory_substance"), item("enriched_ivory"), 1));
        recipe(recipes, "enriching/enriched_echo", itemToItem(
                "mekanism:enriching", itemInput(item("psionic_echo")), item("enriched_echo"), 1));
        recipe(recipes, "enriching/enriched_hypostasis", itemToItem(
                "mekanism:enriching", itemInput(item("hypostasis_gem")), item("enriched_hypostasis"), 1));
        recipe(recipes, "enriching/flashmetal", itemToItem(
                "mekanism:enriching", itemInput(item("unrefined_flashmetal")), item("flashmetal"), 1));
        recipe(recipes, "enriching/echo_sheet", itemToItem(
                "mekanism:enriching", itemInput(item("echo_pellet"), 3), item("echo_sheet"), 1));
    }

    private static void addOreProcessingRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "crushing/antinite_dust_from_ingot", itemToItem(
                "mekanism:crushing", tagInput("c:ingots/antinite"), item("antinite_dust"), 1));
        recipe(recipes, "crushing/dirty_dust_antinite_from_clump", itemToItem(
                "mekanism:crushing", tagInput("mekanism:clumps/antinite"), item("dirty_dust_antinite"), 1));

        recipe(recipes, "enriching/antinite_dust_from_dirty_dust", itemToItem(
                "mekanism:enriching", tagInput("mekanism:dirty_dusts/antinite"), item("antinite_dust"), 1));
        recipe(recipes, "enriching/antinite_dust_from_ore", itemToItem(
                "mekanism:enriching", tagInput("c:ores/antinite"), item("antinite_dust"), 2));
        recipe(recipes, "enriching/antinite_dust_from_raw_block", itemToItem(
                "mekanism:enriching", tagInput("c:storage_blocks/raw_antinite"), item("antinite_dust"), 12));
        recipe(recipes, "enriching/antinite_dust", itemToItem(
                "mekanism:enriching", tagInput("c:raw_materials/antinite", 3), item("antinite_dust"), 4));

        recipe(recipes, "injecting/antinite_shard_from_crystal", itemChemicalToItem(
                "mekanism:injecting",
                chemicalInput("mekanism:hydrogen_chloride", 1),
                tagInput("mekanism:crystals/antinite"),
                item("shard_antinite"), 1, true));
        recipe(recipes, "injecting/antinite_shard_from_ore", itemChemicalToItem(
                "mekanism:injecting",
                chemicalInput("mekanism:hydrogen_chloride", 1),
                tagInput("c:ores/antinite"),
                item("shard_antinite"), 4, true));
        recipe(recipes, "injecting/antinite_shard_from_raw_block", itemChemicalToItem(
                "mekanism:injecting",
                chemicalInput("mekanism:hydrogen_chloride", 2),
                tagInput("c:storage_blocks/raw_antinite"),
                item("shard_antinite"), 24, true));
        recipe(recipes, "injecting/antinite_shard_from_raw_ore", itemChemicalToItem(
                "mekanism:injecting",
                chemicalInput("mekanism:hydrogen_chloride", 1),
                tagInput("c:raw_materials/antinite", 3),
                item("shard_antinite"), 8, true));

        recipe(recipes, "purifying/antinite_clump_from_ore", itemChemicalToItem(
                "mekanism:purifying",
                chemicalInput("mekanism:oxygen", 1),
                tagInput("c:ores/antinite"),
                item("clump_antinite"), 3, true));
        recipe(recipes, "purifying/antinite_clump_from_raw_block", itemChemicalToItem(
                "mekanism:purifying",
                chemicalInput("mekanism:oxygen", 2),
                tagInput("c:storage_blocks/raw_antinite"),
                item("clump_antinite"), 18, true));
        recipe(recipes, "purifying/antinite_clump_from_raw_ore", itemChemicalToItem(
                "mekanism:purifying",
                chemicalInput("mekanism:oxygen", 1),
                tagInput("c:raw_materials/antinite"),
                item("clump_antinite"), 2, true));
        recipe(recipes, "purifying/antinite_clump_from_shard", itemChemicalToItem(
                "mekanism:purifying",
                chemicalInput("mekanism:oxygen", 1),
                tagInput("mekanism:shards/antinite"),
                item("clump_antinite"), 1, true));
    }

    private static void addMetallurgicInfusingRecipes(Map<ResourceLocation, JsonObject> recipes) {
        recipe(recipes, "metallurgic_infusing/alloy_psion", metallurgicInfusing(
                chemicalInput(chemical("infuse_psigem"), 40),
                itemInput("mekanism:alloy_atomic"),
                item("alloy_psion"), 1));
        recipe(recipes, "metallurgic_infusing/alloy_psionic_echo", metallurgicInfusing(
                chemicalInput(chemical("infuse_psionic_echo"), 40),
                itemInput(item("alloy_psion")),
                item("alloy_psionic_echo"), 1));
        recipe(recipes, "metallurgic_infusing/alloy_hypostasis", metallurgicInfusing(
                chemicalInput(chemical("infuse_hypostasis"), 40),
                itemInput(item("alloy_psionic_echo")),
                item("alloy_hypostasis"), 1));
        recipe(recipes, "metallurgic_infusing/psionic_echo", metallurgicInfusing(
                chemicalInput(chemical("infuse_psionic_echo"), 1),
                itemInput("minecraft:echo_shard"),
                item("psionic_echo"), 1));
        recipe(recipes, "metallurgic_infusing/psionic_factor", metallurgicInfusing(
                chemicalInput(chemical("infuse_psigem"), 80),
                itemInput("minecraft:ender_pearl"),
                item("psionic_factor"), 1));
        recipe(recipes, "metallurgic_infusing/psionic_factor_ebony", metallurgicInfusing(
                chemicalInput(chemical("infuse_ebony"), 640),
                itemInput(item("psionic_factor")),
                item("psionic_factor_ebony"), 1));
        recipe(recipes, "metallurgic_infusing/psionic_factor_ivory", metallurgicInfusing(
                chemicalInput(chemical("infuse_ivory"), 640),
                itemInput(item("psionic_factor")),
                item("psionic_factor_ivory"), 1));
        recipe(recipes, "metallurgic_infusing/chaotic_factor_0", metallurgicInfusing(
                chemicalInput(chemical("infuse_ivory"), 640),
                itemInput(item("psionic_factor_ebony")),
                item("chaotic_factor"), 1));
        recipe(recipes, "metallurgic_infusing/chaotic_factor_1", metallurgicInfusing(
                chemicalInput(chemical("infuse_ebony"), 640),
                itemInput(item("psionic_factor_ivory")),
                item("chaotic_factor"), 1));
        recipe(recipes, "metallurgic_infusing/chaotic_psimetal", metallurgicInfusing(
                chemicalInput(chemical("infuse_chaotic_factor"), 40),
                itemInput("psi:psimetal"),
                item("chaotic_psimetal"), 1));
        recipe(recipes, "metallurgic_infusing/heavy_psimetal_scrap", metallurgicInfusing(
                chemicalInput(chemical("infuse_psionic_echo"), 10),
                itemInput("minecraft:netherite_scrap"),
                item("heavy_psimetal_scrap"), 1));
        recipe(recipes, "metallurgic_infusing/psycheonic_metal_nugget", metallurgicInfusing(
                chemicalInput(chemical("infuse_hypostasis"), 10),
                itemInput(item("heavy_psimetal")),
                item("psycheonic_metal_nugget"), 1));
    }

    private static JsonObject itemToItem(String type, JsonObject input, String output, int count) {
        JsonObject root = new JsonObject();

        root.addProperty("type", type);
        root.add("input", input);
        root.add("output", itemOutput(output, count));

        return root;
    }

    private static JsonObject itemChemicalToItem(String type, JsonObject chemicalInput, JsonObject itemInput,
                                                 String output, int count, boolean perTickUsage) {
        JsonObject root = new JsonObject();

        root.addProperty("type", type);
        root.add("chemical_input", chemicalInput);
        root.add("item_input", itemInput);
        root.add("output", itemOutput(output, count));
        root.addProperty("per_tick_usage", perTickUsage);

        return root;
    }

    private static JsonObject metallurgicInfusing(JsonObject chemicalInput, JsonObject itemInput, String output, int count) {
        return itemChemicalToItem("mekanism:metallurgic_infusing", chemicalInput, itemInput, output, count, false);
    }

    private static JsonObject chemicalConversion(JsonObject input, String outputChemical, long amount) {
        return itemToChemical("mekanism:chemical_conversion", input, outputChemical, amount);
    }

    private static JsonObject itemToChemical(String type, JsonObject input, String outputChemical, long amount) {
        JsonObject root = new JsonObject();

        root.addProperty("type", type);
        root.add("input", input);
        root.add("output", chemicalOutput(outputChemical, amount));

        return root;
    }

    private static JsonObject crystallizing(JsonObject input, String output, int count) {
        JsonObject root = new JsonObject();

        root.addProperty("type", "mekanism:crystallizing");
        root.add("input", input);
        root.add("output", itemOutput(output, count));

        return root;
    }

    private static JsonObject combining(JsonObject mainInput, JsonObject extraInput, String output, int count) {
        JsonObject root = new JsonObject();

        root.addProperty("type", "mekanism:combining");
        root.add("main_input", mainInput);
        root.add("extra_input", extraInput);
        root.add("output", itemOutput(output, count));

        return root;
    }

    private static JsonObject dissolution(JsonObject chemicalInput, JsonObject itemInput, String outputChemical, long amount) {
        JsonObject root = new JsonObject();

        root.addProperty("type", "mekanism:dissolution");
        root.add("chemical_input", chemicalInput);
        root.add("item_input", itemInput);
        root.add("output", chemicalOutput(outputChemical, amount));
        root.addProperty("per_tick_usage", true);

        return root;
    }

    private static JsonObject washing(JsonObject fluidInput, JsonObject chemicalInput, String outputChemical, long amount) {
        JsonObject root = new JsonObject();

        root.addProperty("type", "mekanism:washing");
        root.add("fluid_input", fluidInput);
        root.add("chemical_input", chemicalInput);
        root.add("output", chemicalOutput(outputChemical, amount));

        return root;
    }

    private static JsonObject reaction(JsonObject fluidInput, JsonObject chemicalInput, JsonObject itemInput,
                                       JsonObject chemicalOutput, JsonObject itemOutput, int duration) {
        JsonObject root = new JsonObject();

        root.addProperty("type", "mekanism:reaction");
        root.add("chemical_input", chemicalInput);
        root.add("chemical_output", chemicalOutput);
        root.addProperty("duration", duration);
        root.add("fluid_input", fluidInput);
        root.add("item_input", itemInput);
        root.add("item_output", itemOutput);

        return root;
    }

    private static JsonObject reaction(JsonObject fluidInput, JsonObject chemicalInput, JsonObject itemInput,
                                       JsonObject itemOutput, int duration) {
        JsonObject root = new JsonObject();

        root.addProperty("type", "mekanism:reaction");
        root.add("chemical_input", chemicalInput);
        root.addProperty("duration", duration);
        root.add("fluid_input", fluidInput);
        root.add("item_input", itemInput);
        root.add("item_output", itemOutput);

        return root;
    }

    private static JsonObject itemInput(String item) {
        return itemInput(item, 1);
    }

    private static JsonObject itemInput(String item, int count) {
        JsonObject input = new JsonObject();
        input.addProperty("count", count);
        input.addProperty("item", item);
        return input;
    }

    private static JsonObject tagInput(String tag) {
        return tagInput(tag, 1);
    }

    private static JsonObject tagInput(String tag, int count) {
        JsonObject input = new JsonObject();
        input.addProperty("count", count);
        input.addProperty("tag", tag);
        return input;
    }

    private static JsonObject chemicalInput(String chemical, long amount) {
        JsonObject input = new JsonObject();
        input.addProperty("amount", amount);
        input.addProperty("chemical", chemical);
        return input;
    }

    private static JsonObject chemicalOutput(String chemical, long amount) {
        JsonObject output = new JsonObject();
        output.addProperty("amount", amount);
        output.addProperty("id", chemical);
        return output;
    }

    private static JsonObject fluidTagInput(String tag, int amount) {
        JsonObject input = new JsonObject();
        input.addProperty("amount", amount);
        input.addProperty("tag", tag);
        return input;
    }

    private static JsonObject itemOutput(String item, int count) {
        JsonObject output = new JsonObject();
        output.addProperty("count", count);
        output.addProperty("id", item);
        return output;
    }

    private static void recipe(Map<ResourceLocation, JsonObject> recipes, String id, JsonObject recipe) {
        recipes.put(Psitweaks.location(id), recipe);
    }

    private static String item(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }

    private static String chemical(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }
}
