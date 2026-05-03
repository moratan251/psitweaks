package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class PsiTweaksRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public PsiTweaksRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> recipes = new LinkedHashMap<>();

        recipe(recipes, "psionic_control_circuit", shaped(
                List.of("AUA"),
                Map.of(
                        'A', ingredientItem(item("alloy_psion")),
                        'U', ingredientItem("mekanism:ultimate_control_circuit")
                ),
                item("psionic_control_circuit"), 1));
        recipe(recipes, "echo_control_circuit", shaped(
                List.of("SSS", "ACA", "SSS"),
                Map.of(
                        'A', ingredientItem(item("alloy_psionic_echo")),
                        'C', ingredientItem(item("psionic_control_circuit")),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("echo_control_circuit"), 1));
        recipe(recipes, "hypostasis_control_circuit", shaped(
                List.of("SSS", "ACA", "SSS"),
                Map.of(
                        'A', ingredientItem(item("alloy_hypostasis")),
                        'C', ingredientItem(item("echo_control_circuit")),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("hypostasis_control_circuit"), 1));
        recipe(recipes, "magatama", shaped(
                List.of("JJJ", "JHJ", "JJJ"),
                Map.of(
                        'H', ingredientItem(item("hypostasis_gem")),
                        'J', ingredientItem(item("jade"))
                ),
                item("magatama"), 1));
        recipe(recipes, "psionic_echo", trickCrafting(
                ingredientItem("minecraft:echo_shard"),
                item("psionic_echo"),
                "psitweaks:trick_supreme_infusion",
                "psi:cad_assembly_psimetal"));
        recipe(recipes, "incomplete_heavy_psimetal_assembly", shaped(
                List.of("III", "  I"),
                Map.of('I', ingredientItem(item("heavy_psimetal"))),
                item("incomplete_heavy_psimetal_assembly"), 1));
        recipe(recipes, "cad_assembly_alloy_psion", shaped(
                List.of("AAA", "  A"),
                Map.of('A', ingredientItem(item("alloy_psion"))),
                item("cad_assembly_alloy_psion"), 1));
        recipe(recipes, "cad_assembly_chaotic_psimetal", shaped("equipment",
                List.of("AAA", " BA"),
                Map.of(
                        'A', ingredientItem(item("chaotic_psimetal")),
                        'B', ingredientItem(item("psionic_control_circuit"))
                ),
                item("cad_assembly_chaotic_psimetal"), 1));
        recipe(recipes, "cad_assembly_flashmetal", shaped("equipment",
                List.of("HHH", "FFF", " CF"),
                Map.of(
                        'C', ingredientItem(item("psionic_control_circuit")),
                        'F', ingredientItem(item("flashmetal")),
                        'H', ingredientItem("mekanism:hdpe_sheet")
                ),
                item("cad_assembly_flashmetal"), 1));
        recipe(recipes, "cad_assembly_heavy_psimetal_alpha", shaped("equipment",
                List.of("SNS", "CIC", "SCS"),
                Map.of(
                        'C', ingredientItem(item("echo_control_circuit")),
                        'I', ingredientItem(item("incomplete_heavy_psimetal_assembly")),
                        'N', ingredientItem("minecraft:nether_star"),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("cad_assembly_heavy_psimetal_alpha"), 1));
        recipe(recipes, "cad_assembly_heavy_psimetal_beta", shaped("equipment",
                List.of("SCS", "PIP", "SCS"),
                Map.of(
                        'C', ingredientItem(item("echo_control_circuit")),
                        'I', ingredientItem(item("incomplete_heavy_psimetal_assembly")),
                        'P', ingredientItem("mekanism:pellet_plutonium"),
                        'S', ingredientItem(item("echo_sheet"))
                ),
                item("cad_assembly_heavy_psimetal_beta"), 1));
        recipe(recipes, "cad_assembly_psycheonic_metal_from_alpha", shaped("equipment",
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredientItem(item("pellet_americium")),
                        'C', ingredientItem(item("hypostasis_control_circuit")),
                        'H', ingredientItem(item("cad_assembly_heavy_psimetal_alpha")),
                        'P', ingredientItem(item("psycheonic_metal_ingot"))
                ),
                item("cad_assembly_psycheonic_metal"), 1));
        recipe(recipes, "cad_assembly_psycheonic_metal_from_beta", shaped("equipment",
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredientItem(item("pellet_americium")),
                        'C', ingredientItem(item("hypostasis_control_circuit")),
                        'H', ingredientItem(item("cad_assembly_heavy_psimetal_beta")),
                        'P', ingredientItem(item("psycheonic_metal_ingot"))
                ),
                item("cad_assembly_psycheonic_metal"), 1));
        recipe(recipes, "unrefined_flashmetal", shaped(
                List.of("BBB", "AAA", "BBB"),
                Map.of(
                        'A', ingredientItem(item("chaotic_psimetal")),
                        'B', ingredientItem("mekanism:ingot_refined_glowstone")
                ),
                item("unrefined_flashmetal"), 1));
        recipe(recipes, "heavy_psimetal", shapeless(
                List.of(
                        ingredientItem(item("heavy_psimetal_scrap")),
                        ingredientItem(item("heavy_psimetal_scrap")),
                        ingredientItem(item("heavy_psimetal_scrap")),
                        ingredientItem(item("heavy_psimetal_scrap")),
                        ingredientItem(item("flashmetal")),
                        ingredientItem(item("flashmetal")),
                        ingredientItem(item("flashmetal")),
                        ingredientItem(item("flashmetal"))
                ),
                item("heavy_psimetal"), 1));
        recipe(recipes, "psycheonic_metal_ingot", shaped(
                List.of("NNN", "NNN", "NNN"),
                Map.of('N', ingredientItem(item("psycheonic_metal_nugget"))),
                item("psycheonic_metal_ingot"), 1));
        recipe(recipes, "psycheonic_metal_nugget_from_ingot", shapeless(
                List.of(ingredientItem(item("psycheonic_metal_ingot"))),
                item("psycheonic_metal_nugget"), 9));
        recipe(recipes, "amethyst_shard_from_block", shapeless(
                List.of(ingredientItem("minecraft:amethyst_block")),
                "minecraft:amethyst_shard", 4));
        recipe(recipes, "antinite_ingot_smelting", cooking(
                "minecraft:smelting",
                ingredientItem(item("raw_antinite")),
                item("antinite_ingot"), 1.2D, 200));
        recipe(recipes, "antinite_ingot_blasting", cooking(
                "minecraft:blasting",
                ingredientItem(item("raw_antinite")),
                item("antinite_ingot"), 1.2D, 100));
        recipe(recipes, "antinite_ingot_from_dust_smelting", cooking(
                "minecraft:smelting",
                ingredientItem(item("antinite_dust")),
                item("antinite_ingot"), 0.3D, 200));
        recipe(recipes, "antinite_ingot_from_dust_blasting", cooking(
                "minecraft:blasting",
                ingredientItem(item("antinite_dust")),
                item("antinite_ingot"), 0.3D, 100));
        recipe(recipes, "cad_disassembler", shaped(
                List.of("PPP", "PAP", "PPP"),
                Map.of(
                        'P', ingredientItem("psi:psimetal"),
                        'A', ingredientItem("minecraft:anvil")
                ),
                block("cad_disassembler"), 1));
        recipe(recipes, "inline_caster", shaped("equipment",
                List.of(" G ", "PBP", " P "),
                Map.of(
                        'B', ingredientItem("psi:cad_assembly_psimetal"),
                        'G', ingredientItem("psi:psigem"),
                        'P', ingredientItem("psi:psimetal")
                ),
                item("inline_caster"), 1));
        recipe(recipes, "secondary_caster", shaped("equipment",
                List.of(" F ", "CBC", " T "),
                Map.of(
                        'B', ingredientItem("psi:cad_assembly_psimetal"),
                        'C', ingredientItem(item("psionic_control_circuit")),
                        'F', ingredientItem(item("flashmetal")),
                        'T', ingredientItem("psi:cad_socket_signaling")
                ),
                item("secondary_caster"), 1));
        recipe(recipes, "parallel_caster", shaped("equipment",
                List.of("FHF", "CBC", "FTF"),
                Map.of(
                        'B', ingredientItem("psi:cad_assembly_psimetal"),
                        'C', ingredientItem(item("echo_control_circuit")),
                        'F', ingredientItem(item("flashmetal")),
                        'H', ingredientItem(item("heavy_psimetal")),
                        'T', ingredientItem("psi:cad_socket_transmissive")
                ),
                item("parallel_caster"), 1));
        recipe(recipes, "antinite_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(item("antinite_ingot"))),
                block("antinite_block"), 1));
        recipe(recipes, "antinite_ingot_from_block", shapeless(
                List.of(ingredientItem(block("antinite_block"))),
                item("antinite_ingot"), 9));
        recipe(recipes, "chaotic_psimetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(item("chaotic_psimetal"))),
                block("chaotic_psimetal_block"), 1));
        recipe(recipes, "chaotic_psimetal_from_block", shapeless(
                List.of(ingredientItem(block("chaotic_psimetal_block"))),
                item("chaotic_psimetal"), 9));
        recipe(recipes, "flashmetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(item("flashmetal"))),
                block("flashmetal_block"), 1));
        recipe(recipes, "flashmetal_from_block", shapeless(
                List.of(ingredientItem(block("flashmetal_block"))),
                item("flashmetal"), 9));
        recipe(recipes, "heavy_psimetal_block", shaped("building",
                List.of("III", "III", "III"),
                Map.of('I', ingredientItem(item("heavy_psimetal"))),
                block("heavy_psimetal_block"), 1));
        recipe(recipes, "heavy_psimetal_from_block", shapeless(
                List.of(ingredientItem(block("heavy_psimetal_block"))),
                item("heavy_psimetal"), 9));
        recipe(recipes, "plutonium_block", shaped("building",
                List.of("PPP", "PPP", "PPP"),
                Map.of('P', ingredientItem("mekanism:pellet_plutonium")),
                block("plutonium_block"), 1));
        recipe(recipes, "plutonium_pellet_from_block", shapeless(
                List.of(ingredientItem(block("plutonium_block"))),
                "mekanism:pellet_plutonium", 9));
        recipe(recipes, "polonium_block", shaped("building",
                List.of("PPP", "PPP", "PPP"),
                Map.of('P', ingredientItem("mekanism:pellet_polonium")),
                block("polonium_block"), 1));
        recipe(recipes, "polonium_pellet_from_block", shapeless(
                List.of(ingredientItem(block("polonium_block"))),
                "mekanism:pellet_polonium", 9));
        recipe(recipes, "raw_antinite_block", shaped("building",
                List.of("RRR", "RRR", "RRR"),
                Map.of('R', ingredientItem(item("raw_antinite"))),
                block("raw_antinite_block"), 1));
        recipe(recipes, "raw_antinite_from_block", shapeless(
                List.of(ingredientItem(block("raw_antinite_block"))),
                item("raw_antinite"), 9));
        recipe(recipes, "spellmachinery_casing", shaped("building",
                List.of("AHA", "HPH", "AHA"),
                Map.of(
                        'A', ingredientItem(item("alloy_hypostasis")),
                        'H', ingredientItem(item("magatama")),
                        'P', ingredientItem(item("psycheonic_metal_ingot"))
                ),
                block("spellmachinery_casing"), 1));

        CompletableFuture<?>[] futures = recipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks recipes";
    }

    private static void recipe(Map<ResourceLocation, JsonObject> recipes, String id, JsonObject recipe) {
        recipes.put(Psitweaks.location(id), recipe);
    }

    private static JsonObject shaped(List<String> pattern, Map<Character, JsonObject> keys, String result, int count) {
        return shaped("misc", pattern, keys, result, count);
    }

    private static JsonObject shaped(String category, List<String> pattern, Map<Character, JsonObject> keys, String result, int count) {
        JsonObject root = new JsonObject();
        JsonArray patternJson = new JsonArray();
        JsonObject keyJson = new JsonObject();

        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", category);
        for (String row : pattern) {
            patternJson.add(row);
        }
        root.add("pattern", patternJson);
        keys.forEach((key, ingredient) -> keyJson.add(String.valueOf(key), ingredient));
        root.add("key", keyJson);
        root.add("result", result(result, count));

        return root;
    }

    private static JsonObject shapeless(List<JsonObject> ingredients, String result, int count) {
        return shapeless("misc", ingredients, result, count);
    }

    private static JsonObject shapeless(String category, List<JsonObject> ingredients, String result, int count) {
        JsonObject root = new JsonObject();
        JsonArray ingredientsJson = new JsonArray();

        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", category);
        ingredients.forEach(ingredientsJson::add);
        root.add("ingredients", ingredientsJson);
        root.add("result", result(result, count));

        return root;
    }

    private static JsonObject cooking(String type, JsonObject ingredient, String result, double experience, int cookingTime) {
        JsonObject root = new JsonObject();

        root.addProperty("type", type);
        root.addProperty("category", "misc");
        root.add("ingredient", ingredient);
        root.add("result", result(result, 1));
        root.addProperty("experience", experience);
        root.addProperty("cookingtime", cookingTime);

        return root;
    }

    private static JsonObject trickCrafting(JsonObject input, String output, String piece, String cadAssembly) {
        JsonObject root = new JsonObject();

        root.addProperty("type", "psi:trick_crafting");
        root.add("cad", result(cadAssembly, 1));
        root.add("input", input);
        root.add("output", result(output, 1));
        root.addProperty("piece", piece);

        return root;
    }

    private static JsonObject ingredientItem(String item) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        return ingredient;
    }

    private static JsonObject result(String item, int count) {
        JsonObject result = new JsonObject();
        result.addProperty("count", count);
        result.addProperty("id", item);
        return result;
    }

    private static String item(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }

    private static String block(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }
}
