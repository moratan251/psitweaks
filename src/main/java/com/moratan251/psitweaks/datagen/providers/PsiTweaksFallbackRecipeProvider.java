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

public final class PsiTweaksFallbackRecipeProvider implements DataProvider {
    private static final String MEKANISM = "mekanism";

    private final PackOutput.PathProvider pathProvider;

    public PsiTweaksFallbackRecipeProvider(PackOutput output) {
        pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> recipes = new LinkedHashMap<>();

        addCookingPair(recipes, "enriched_psigem", "psi:psigem", item("enriched_psigem"), 0.3D);
        addCookingPair(recipes, "enriched_ebony", "psi:ebony_substance", item("enriched_ebony"), 0.3D);
        addCookingPair(recipes, "enriched_ivory", "psi:ivory_substance", item("enriched_ivory"), 0.3D);
        addCookingPair(recipes, "enriched_echo", item("psionic_echo"), item("enriched_echo"), 0.6D);
        addCookingPair(recipes, "enriched_hypostasis", item("hypostasis_gem"), item("enriched_hypostasis"), 0.8D);
        addCookingPair(recipes, "flashmetal", item("unrefined_flashmetal"), item("flashmetal"), 1.0D);

        add(recipes, "echo_sheet", shapeless(
                List.of(
                        ingredient(item("echo_pellet")),
                        ingredient(item("echo_pellet")),
                        ingredient(item("echo_pellet"))
                ),
                item("echo_sheet"), 1));
        add(recipes, "echo_pellet", shapeless(
                List.of(
                        ingredient("minecraft:paper"),
                        ingredient("minecraft:paper"),
                        ingredient("minecraft:paper"),
                        ingredient(item("psionic_echo"))
                ),
                item("echo_pellet"), 3));
        add(recipes, "alloy_psion", shapeless(
                List.of(
                        ingredient("minecraft:copper_block"),
                        ingredient("minecraft:redstone"),
                        ingredient("minecraft:obsidian"),
                        ingredient(item("enriched_psigem")),
                        ingredient(item("enriched_psigem")),
                        ingredient(item("enriched_psigem")),
                        ingredient(item("enriched_psigem")),
                        ingredient(item("enriched_psigem")),
                        ingredient(item("enriched_psigem"))
                ),
                item("alloy_psion"), 8));
        add(recipes, "alloy_psionic_echo", shapeless(
                List.of(
                        ingredient(item("alloy_psion")),
                        ingredient(item("alloy_psion")),
                        ingredient(item("enriched_echo"))
                ),
                item("alloy_psionic_echo"), 2));
        add(recipes, "alloy_hypostasis", shapeless(
                List.of(
                        ingredient(item("alloy_psionic_echo")),
                        ingredient(item("alloy_psionic_echo")),
                        ingredient(item("enriched_hypostasis"))
                ),
                item("alloy_hypostasis"), 2));
        add(recipes, "psionic_factor", shapeless(
                List.of(ingredient("minecraft:ender_pearl"), ingredient(item("enriched_psigem"))),
                item("psionic_factor"), 1));
        add(recipes, "psionic_factor_ebony", surrounding(
                item("psionic_factor"), item("enriched_ebony"), item("psionic_factor_ebony")));
        add(recipes, "psionic_factor_ivory", surrounding(
                item("psionic_factor"), item("enriched_ivory"), item("psionic_factor_ivory")));
        add(recipes, "chaotic_factor", surrounding(
                item("psionic_factor_ebony"), item("enriched_ivory"), item("chaotic_factor")));
        add(recipes, "chaotic_factor_from_ivory", surrounding(
                item("psionic_factor_ivory"), item("enriched_ebony"), item("chaotic_factor")));
        add(recipes, "chaotic_psimetal", shapeless(
                List.of(
                        ingredient("psi:psimetal"),
                        ingredient("psi:psimetal"),
                        ingredient(item("chaotic_factor"))
                ),
                item("chaotic_psimetal"), 1));
        add(recipes, "heavy_psimetal_scrap", shapeless(
                List.of(
                        ingredient(item("enriched_echo")),
                        ingredient("minecraft:netherite_scrap"),
                        ingredient("minecraft:netherite_scrap"),
                        ingredient("minecraft:netherite_scrap"),
                        ingredient("minecraft:netherite_scrap"),
                        ingredient("minecraft:netherite_scrap"),
                        ingredient("minecraft:netherite_scrap"),
                        ingredient("minecraft:netherite_scrap"),
                        ingredient("minecraft:netherite_scrap")
                ),
                item("heavy_psimetal_scrap"), 8));
        add(recipes, "psycheonic_metal_nugget", shapelessCounted(
                List.of(
                        counted(item("heavy_psimetal"), 8),
                        counted(item("enriched_hypostasis"), 1)
                ),
                item("psycheonic_metal_nugget"), 8));

        addPhilosophersStoneFallbacks(recipes);
        addProgramFallbacks(recipes);

        add(recipes, "unrefined_flashmetal", shaped(
                List.of("GGG", "AAA", "GGG"),
                Map.of(
                        'A', ingredient(item("chaotic_psimetal")),
                        'G', ingredient("minecraft:glowstone")
                ),
                item("unrefined_flashmetal"), 1));
        add(recipes, "psionic_control_circuit", shaped(
                List.of("ROR", "AGA", "RDR"),
                Map.of(
                        'A', ingredient(item("alloy_psion")),
                        'D', ingredient("minecraft:diamond"),
                        'G', ingredient("minecraft:gold_ingot"),
                        'O', ingredient("minecraft:obsidian"),
                        'R', ingredient("minecraft:redstone")
                ),
                item("psionic_control_circuit"), 1));
        add(recipes, "cad_assembly_flashmetal", shaped(
                List.of("MMM", "FFF", " CF"),
                Map.of(
                        'C', ingredient(item("psionic_control_circuit")),
                        'F', ingredient(item("flashmetal")),
                        'M', ingredient("minecraft:paper")
                ),
                item("cad_assembly_flashmetal"), 1));
        add(recipes, "cad_assembly_heavy_psimetal_beta", shaped(
                List.of("SCS", "PIP", "SCS"),
                Map.of(
                        'C', ingredient(item("echo_control_circuit")),
                        'I', ingredient(item("incomplete_heavy_psimetal_assembly")),
                        'P', ingredient("minecraft:heart_of_the_sea"),
                        'S', ingredient(item("echo_sheet"))
                ),
                item("cad_assembly_heavy_psimetal_beta"), 1));
        add(recipes, "cad_assembly_psycheonic_metal_from_alpha", shaped(
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredient("minecraft:heavy_core"),
                        'C', ingredient(item("hypostasis_control_circuit")),
                        'H', ingredient(item("cad_assembly_heavy_psimetal_alpha")),
                        'P', ingredient(item("psycheonic_metal_ingot"))
                ),
                item("cad_assembly_psycheonic_metal"), 1));
        add(recipes, "cad_assembly_psycheonic_metal_from_beta", shaped(
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredient("minecraft:heavy_core"),
                        'C', ingredient(item("hypostasis_control_circuit")),
                        'H', ingredient(item("cad_assembly_heavy_psimetal_beta")),
                        'P', ingredient(item("psycheonic_metal_ingot"))
                ),
                item("cad_assembly_psycheonic_metal"), 1));

        add(recipes, "interference_range_extender", shaped(
                List.of("ABA", "BCB", "ABA"),
                Map.of(
                        'A', ingredient(item("flashmetal")),
                        'B', ingredient(item("psionic_control_circuit")),
                        'C', ingredient("minecraft:ender_eye")
                ),
                item("interference_range_extender"), 1));
        add(recipes, "third_eye_device", shaped(
                List.of("ABA", "CDC", "ABA"),
                Map.of(
                        'A', ingredient(item("psycheonic_metal_ingot")),
                        'B', ingredient("minecraft:heavy_core"),
                        'C', ingredient("minecraft:nether_star"),
                        'D', ingredient(item("interference_range_extender"))
                ),
                item("third_eye_device"), 1));

        CompletableFuture<?>[] futures = recipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks recipes without Mekanism";
    }

    private static void addCookingPair(Map<ResourceLocation, JsonObject> recipes, String id, String input,
            String result, double experience) {
        add(recipes, id + "_smelting", cooking(
                "minecraft:smelting", ingredient(input), result, experience, 200));
        add(recipes, id + "_blasting", cooking(
                "minecraft:blasting", ingredient(input), result, experience, 100));
    }

    private static JsonObject surrounding(String center, String outer, String result) {
        return shaped(
                List.of("OOO", "OCO", "OOO"),
                Map.of(
                        'C', ingredient(center),
                        'O', ingredient(outer)
                ),
                result, 1);
    }

    private static JsonObject shaped(List<String> pattern, Map<Character, JsonObject> keys, String result, int count) {
        JsonObject root = new JsonObject();
        JsonArray patternJson = new JsonArray();
        JsonObject keyJson = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        pattern.forEach(patternJson::add);
        keys.forEach((key, value) -> keyJson.add(String.valueOf(key), value));
        root.add("pattern", patternJson);
        root.add("key", keyJson);
        root.add("result", result(result, count));
        return root;
    }

    private static JsonObject shapeless(List<JsonObject> ingredients, String result, int count) {
        JsonObject root = new JsonObject();
        JsonArray ingredientsJson = new JsonArray();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        ingredients.forEach(ingredientsJson::add);
        root.add("ingredients", ingredientsJson);
        root.add("result", result(result, count));
        return root;
    }

    private static JsonObject shapelessCounted(List<CountedIngredient> ingredients, String result, int count) {
        JsonObject root = new JsonObject();
        JsonArray ingredientsJson = new JsonArray();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        ingredients.forEach(entry -> {
            for (int i = 0; i < entry.count(); i++) {
                ingredientsJson.add(ingredient(entry.item()));
            }
        });
        root.add("ingredients", ingredientsJson);
        root.add("result", result(result, count));
        return root;
    }

    private static void addPhilosophersStoneFallbacks(Map<ResourceLocation, JsonObject> recipes) {
        add(recipes, "philosophers_stone/philosophers_stone_iron_to_gold", shapelessCounted(
                List.of(
                        counted(item("philosophers_stone"), 1),
                        counted("minecraft:iron_ingot", 8)
                ),
                "minecraft:gold_ingot", 2));
        add(recipes, "philosophers_stone/philosophers_stone_gold_to_iron", shapelessCounted(
                List.of(
                        counted(item("philosophers_stone"), 1),
                        counted("minecraft:gold_ingot", 2)
                ),
                "minecraft:iron_ingot", 8));
    }

    private static void addProgramFallbacks(Map<ResourceLocation, JsonObject> recipes) {
        addProgram(recipes, "program_cocytus", List.of(
                counted(item("psycheonic_metal"), 2),
                counted("minecraft:blue_ice", 2),
                counted("minecraft:nether_star", 3),
                counted("minecraft:sculk_shrieker", 1)));
        addProgram(recipes, "program_phonon_maser", List.of(
                counted("minecraft:amethyst_shard", 2),
                counted(item("flashmetal"), 4),
                counted("minecraft:note_block", 2)));
        addProgram(recipes, "program_time_accelerate", List.of(
                counted("minecraft:redstone_block", 2),
                counted("minecraft:powered_rail", 2),
                counted("minecraft:clock", 4)));
        addProgram(recipes, "program_flight", List.of(
                counted("minecraft:phantom_membrane", 2),
                counted("minecraft:nether_wart", 2),
                counted("minecraft:feather", 2),
                counted(item("chaotic_factor"), 2)));
        addProgram(recipes, "program_meteor_line", List.of(
                counted(item("psycheonic_metal"), 4),
                counted("minecraft:nether_star", 2),
                counted("minecraft:dragon_head", 2)));
        addProgram(recipes, "program_supreme_infusion", List.of(
                counted("minecraft:amethyst_block", 2),
                counted(item("flashmetal"), 2),
                counted(item("alloy_psion"), 3),
                counted("minecraft:netherite_ingot", 1)));
        addProgram(recipes, "program_molecular_divider", List.of(
                counted(item("echo_control_circuit"), 4),
                counted("minecraft:quartz_block", 2),
                counted(item("heavy_psimetal"), 2)));
        addProgram(recipes, "program_guillotine", List.of(
                counted("minecraft:wither_skeleton_skull", 1),
                counted("minecraft:anvil", 3),
                counted("psi:psimetal_sword", 1),
                counted("minecraft:bone", 2),
                counted("minecraft:rotten_flesh", 1)));
        addProgram(recipes, "program_active_air_mine", List.of(
                counted("minecraft:tnt", 2),
                counted("psi:psidust", 4),
                counted("psi:psigem", 2)));
        addProgram(recipes, "program_die_flex", List.of(
                counted(item("psionic_control_circuit"), 3),
                counted(item("flashmetal"), 3),
                counted(item("chaotic_factor"), 2)));
        addProgramUpgrade(recipes, "program_jump_flex", List.of(
                counted(item("program_die_flex"), 1),
                counted(item("echo_control_circuit"), 3),
                counted(item("heavy_psimetal"), 3),
                counted(item("antinite_ingot"), 2)));
        addProgramUpgrade(recipes, "program_switch_flex", List.of(
                counted(item("program_jump_flex"), 1),
                counted(item("hypostasis_control_circuit"), 3),
                counted(item("psycheonic_metal_ingot"), 3),
                counted("minecraft:nether_star", 2)));
        addProgram(recipes, "program_material_mutation", List.of(
                counted(item("philosophers_stone"), 1),
                counted("minecraft:sculk_catalyst", 1),
                counted(item("antinite_ingot"), 2),
                counted(item("chaotic_factor"), 2),
                counted(item("psionic_echo"), 2)));
        addProgram(recipes, "program_mass_block_break", List.of(
                counted("minecraft:tnt", 4),
                counted("psi:psigem", 2),
                counted(item("chaotic_psimetal"), 2)));
    }

    private static void addProgram(Map<ResourceLocation, JsonObject> recipes, String program,
            List<CountedIngredient> ingredients) {
        List<CountedIngredient> withBlankProgram = new java.util.ArrayList<>();
        withBlankProgram.add(counted(item("program_blank"), 1));
        withBlankProgram.addAll(ingredients);
        add(recipes, program, shapelessCounted(withBlankProgram, item(program), 1));
    }

    private static void addProgramUpgrade(Map<ResourceLocation, JsonObject> recipes, String program,
            List<CountedIngredient> ingredients) {
        add(recipes, program, shapelessCounted(ingredients, item(program), 1));
    }

    private static CountedIngredient counted(String item, int count) {
        return new CountedIngredient(item, count);
    }

    private record CountedIngredient(String item, int count) {
    }

    private static JsonObject cooking(String type, JsonObject ingredient, String result, double experience,
            int cookingTime) {
        JsonObject root = new JsonObject();
        root.addProperty("type", type);
        root.addProperty("category", "misc");
        root.add("ingredient", ingredient);
        root.add("result", result(result, 1));
        root.addProperty("experience", experience);
        root.addProperty("cookingtime", cookingTime);
        return root;
    }

    private static JsonObject ingredient(String item) {
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

    private static void add(Map<ResourceLocation, JsonObject> recipes, String id, JsonObject recipe) {
        recipes.put(
                Psitweaks.location("fallback/" + id),
                RecipeConditionHelper.excludeMod(recipe, MEKANISM)
        );
    }

    private static String item(String path) {
        return Psitweaks.MOD_ID + ":" + path;
    }
}
