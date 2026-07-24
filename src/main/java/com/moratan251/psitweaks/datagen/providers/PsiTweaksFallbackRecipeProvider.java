package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import vazkii.psi.common.item.base.ModItems;

public final class PsiTweaksFallbackRecipeProvider implements DataProvider {
    private static final String MEKANISM = "mekanism";

    private final PackOutput.PathProvider pathProvider;

    public PsiTweaksFallbackRecipeProvider(PackOutput output) {
        pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, JsonObject> recipes = new LinkedHashMap<>();

        addCookingPair(recipes, "enriched_psigem", ModItems.psigem.get(), PsitweaksItems.ENRICHED_PSIGEM, 0.3D);
        addCookingPair(recipes, "enriched_ebony", ModItems.ebonySubstance.get(), PsitweaksItems.ENRICHED_EBONY, 0.3D);
        addCookingPair(recipes, "enriched_ivory", ModItems.ivorySubstance.get(), PsitweaksItems.ENRICHED_IVORY, 0.3D);
        addCookingPair(recipes, "enriched_echo", PsitweaksItems.PSIONIC_ECHO, PsitweaksItems.ENRICHED_ECHO, 0.6D);
        addCookingPair(recipes, "enriched_hypostasis", PsitweaksItems.HYPOSTASIS_GEM, PsitweaksItems.ENRICHED_HYPOSTASIS, 0.8D);
        addCookingPair(recipes, "flashmetal", PsitweaksItems.UNREFINED_FLASHMETAL, PsitweaksItems.FLASHMETAL, 1.0D);

        add(recipes, "echo_sheet", shapeless(
                List.of(
                        ingredient(PsitweaksItems.ECHO_PELLET),
                        ingredient(PsitweaksItems.ECHO_PELLET),
                        ingredient(PsitweaksItems.ECHO_PELLET)
                ),
                PsitweaksItems.ECHO_SHEET, 1));
        add(recipes, "echo_pellet", shapeless(
                List.of(
                        ingredient(Items.PAPER),
                        ingredient(Items.PAPER),
                        ingredient(Items.PAPER),
                        ingredient(PsitweaksItems.PSIONIC_ECHO)
                ),
                PsitweaksItems.ECHO_PELLET, 3));
        add(recipes, "alloy_psion", shapeless(
                List.of(
                        ingredient(Items.COPPER_BLOCK),
                        ingredient(Items.REDSTONE),
                        ingredient(Items.OBSIDIAN),
                        ingredient(PsitweaksItems.ENRICHED_PSIGEM),
                        ingredient(PsitweaksItems.ENRICHED_PSIGEM),
                        ingredient(PsitweaksItems.ENRICHED_PSIGEM),
                        ingredient(PsitweaksItems.ENRICHED_PSIGEM),
                        ingredient(PsitweaksItems.ENRICHED_PSIGEM),
                        ingredient(PsitweaksItems.ENRICHED_PSIGEM)
                ),
                PsitweaksItems.ALLOY_PSION, 8));
        add(recipes, "alloy_psionic_echo", shapeless(
                List.of(
                        ingredient(PsitweaksItems.ALLOY_PSION),
                        ingredient(PsitweaksItems.ALLOY_PSION),
                        ingredient(PsitweaksItems.ENRICHED_ECHO)
                ),
                PsitweaksItems.ALLOY_PSIONIC_ECHO, 2));
        add(recipes, "alloy_hypostasis", shapeless(
                List.of(
                        ingredient(PsitweaksItems.ALLOY_PSIONIC_ECHO),
                        ingredient(PsitweaksItems.ALLOY_PSIONIC_ECHO),
                        ingredient(PsitweaksItems.ENRICHED_HYPOSTASIS)
                ),
                PsitweaksItems.ALLOY_HYPOSTASIS, 2));
        add(recipes, "psionic_factor", shapeless(
                List.of(ingredient(Items.ENDER_PEARL), ingredient(PsitweaksItems.ENRICHED_PSIGEM)),
                PsitweaksItems.PSIONIC_FACTOR, 1));
        add(recipes, "psionic_factor_ebony", surrounding(
                PsitweaksItems.PSIONIC_FACTOR, PsitweaksItems.ENRICHED_EBONY, PsitweaksItems.PSIONIC_FACTOR_EBONY));
        add(recipes, "psionic_factor_ivory", surrounding(
                PsitweaksItems.PSIONIC_FACTOR, PsitweaksItems.ENRICHED_IVORY, PsitweaksItems.PSIONIC_FACTOR_IVORY));
        add(recipes, "chaotic_factor", surrounding(
                PsitweaksItems.PSIONIC_FACTOR_EBONY, PsitweaksItems.ENRICHED_IVORY, PsitweaksItems.CHAOTIC_FACTOR));
        add(recipes, "chaotic_factor_from_ivory", surrounding(
                PsitweaksItems.PSIONIC_FACTOR_IVORY, PsitweaksItems.ENRICHED_EBONY, PsitweaksItems.CHAOTIC_FACTOR));
        add(recipes, "chaotic_psimetal", shapeless(
                List.of(
                        ingredient(ModItems.psimetal.get()),
                        ingredient(ModItems.psimetal.get()),
                        ingredient(PsitweaksItems.CHAOTIC_FACTOR)
                ),
                PsitweaksItems.CHAOTIC_PSIMETAL, 1));
        add(recipes, "heavy_psimetal_scrap", shapeless(
                List.of(
                        ingredient(PsitweaksItems.ENRICHED_ECHO),
                        ingredient(Items.NETHERITE_SCRAP),
                        ingredient(Items.NETHERITE_SCRAP),
                        ingredient(Items.NETHERITE_SCRAP),
                        ingredient(Items.NETHERITE_SCRAP),
                        ingredient(Items.NETHERITE_SCRAP),
                        ingredient(Items.NETHERITE_SCRAP),
                        ingredient(Items.NETHERITE_SCRAP),
                        ingredient(Items.NETHERITE_SCRAP)
                ),
                PsitweaksItems.HEAVY_PSIMETAL_SCRAP, 8));
        add(recipes, "psycheonic_metal_nugget", shapelessCounted(
                List.of(
                        counted(PsitweaksItems.HEAVY_PSIMETAL, 8),
                        counted(PsitweaksItems.ENRICHED_HYPOSTASIS, 1)
                ),
                PsitweaksItems.PSYCHEONIC_METAL_NUGGET, 8));

        addPhilosophersStoneFallbacks(recipes);
        addProgramFallbacks(recipes);

        add(recipes, "unrefined_flashmetal", shaped(
                List.of("GGG", "AAA", "GGG"),
                Map.of(
                        'A', ingredient(PsitweaksItems.CHAOTIC_PSIMETAL),
                        'G', ingredient(Items.GLOWSTONE)
                ),
                PsitweaksItems.UNREFINED_FLASHMETAL, 1));
        add(recipes, "psionic_control_circuit", shaped(
                List.of("ROR", "AGA", "RDR"),
                Map.of(
                        'A', ingredient(PsitweaksItems.ALLOY_PSION),
                        'D', ingredient(Items.DIAMOND),
                        'G', ingredient(Items.GOLD_INGOT),
                        'O', ingredient(Items.OBSIDIAN),
                        'R', ingredient(Items.REDSTONE)
                ),
                PsitweaksItems.PSIONIC_CONTROL_CIRCUIT, 1));
        add(recipes, "cad_assembly_flashmetal", shaped(
                List.of("MMM", "FFF", " CF"),
                Map.of(
                        'C', ingredient(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'F', ingredient(PsitweaksItems.FLASHMETAL),
                        'M', ingredient(Items.PAPER)
                ),
                PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL, 1));
        add(recipes, "cad_assembly_heavy_psimetal_beta", shaped(
                List.of("SCS", "PIP", "SCS"),
                Map.of(
                        'C', ingredient(PsitweaksItems.ECHO_CONTROL_CIRCUIT),
                        'I', ingredient(PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY),
                        'P', ingredient(Items.HEART_OF_THE_SEA),
                        'S', ingredient(PsitweaksItems.ECHO_SHEET)
                ),
                PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA, 1));
        add(recipes, "cad_assembly_psycheonic_metal_from_alpha", shaped(
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredient(Items.HEAVY_CORE),
                        'C', ingredient(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT),
                        'H', ingredient(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA),
                        'P', ingredient(PsitweaksItems.PSYCHEONIC_METAL_INGOT)
                ),
                PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL, 1));
        add(recipes, "cad_assembly_psycheonic_metal_from_beta", shaped(
                List.of("PCP", "AHA", "PCP"),
                Map.of(
                        'A', ingredient(Items.HEAVY_CORE),
                        'C', ingredient(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT),
                        'H', ingredient(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA),
                        'P', ingredient(PsitweaksItems.PSYCHEONIC_METAL_INGOT)
                ),
                PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL, 1));

        add(recipes, "interference_range_extender", shaped(
                List.of("ABA", "BCB", "ABA"),
                Map.of(
                        'A', ingredient(PsitweaksItems.FLASHMETAL),
                        'B', ingredient(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT),
                        'C', ingredient(Items.ENDER_EYE)
                ),
                PsitweaksItems.INTERFERENCE_RANGE_EXTENDER, 1));
        add(recipes, "third_eye_device", shaped(
                List.of("ABA", "CDC", "ABA"),
                Map.of(
                        'A', ingredient(PsitweaksItems.PSYCHEONIC_METAL_INGOT),
                        'B', ingredient(Items.HEAVY_CORE),
                        'C', ingredient(Items.NETHER_STAR),
                        'D', ingredient(PsitweaksItems.INTERFERENCE_RANGE_EXTENDER)
                ),
                PsitweaksItems.THIRD_EYE_DEVICE, 1));

        CompletableFuture<?>[] futures = recipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "PsiTweaks recipes without Mekanism";
    }

    private static void addCookingPair(Map<ResourceLocation, JsonObject> recipes, String id, ItemLike input,
            ItemLike result, double experience) {
        add(recipes, id + "_smelting", cooking(
                "minecraft:smelting", ingredient(input), result, experience, 200));
        add(recipes, id + "_blasting", cooking(
                "minecraft:blasting", ingredient(input), result, experience, 100));
    }

    private static JsonObject surrounding(ItemLike center, ItemLike outer, ItemLike result) {
        return shaped(
                List.of("OOO", "OCO", "OOO"),
                Map.of(
                        'C', ingredient(center),
                        'O', ingredient(outer)
                ),
                result, 1);
    }

    private static JsonObject shaped(List<String> pattern, Map<Character, JsonObject> keys, ItemLike result, int count) {
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

    private static JsonObject shapeless(List<JsonObject> ingredients, ItemLike result, int count) {
        JsonObject root = new JsonObject();
        JsonArray ingredientsJson = new JsonArray();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        ingredients.forEach(ingredientsJson::add);
        root.add("ingredients", ingredientsJson);
        root.add("result", result(result, count));
        return root;
    }

    private static JsonObject shapelessCounted(List<CountedIngredient> ingredients, ItemLike result, int count) {
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
                        counted(PsitweaksItems.PHILOSOPHERS_STONE, 1),
                        counted(Items.IRON_INGOT, 8)
                ),
                Items.GOLD_INGOT, 2));
        add(recipes, "philosophers_stone/philosophers_stone_gold_to_iron", shapelessCounted(
                List.of(
                        counted(PsitweaksItems.PHILOSOPHERS_STONE, 1),
                        counted(Items.GOLD_INGOT, 2)
                ),
                Items.IRON_INGOT, 8));
    }

    private static void addProgramFallbacks(Map<ResourceLocation, JsonObject> recipes) {
        addProgram(recipes, PsitweaksItems.PROGRAM_COCYTUS, List.of(
                counted(PsitweaksItems.PSYCHEONIC_METAL_INGOT, 2),
                counted(Items.BLUE_ICE, 2),
                counted(Items.NETHER_STAR, 3),
                counted(Items.SCULK_SHRIEKER, 1)));
        addProgram(recipes, PsitweaksItems.PROGRAM_PHONON_MASER, List.of(
                counted(Items.AMETHYST_SHARD, 2),
                counted(PsitweaksItems.FLASHMETAL, 4),
                counted(Items.NOTE_BLOCK, 2)));
        addProgram(recipes, PsitweaksItems.PROGRAM_TIME_ACCELERATE, List.of(
                counted(Items.REDSTONE_BLOCK, 2),
                counted(Items.POWERED_RAIL, 2),
                counted(Items.CLOCK, 4)));
        addProgram(recipes, PsitweaksItems.PROGRAM_FLIGHT, List.of(
                counted(Items.PHANTOM_MEMBRANE, 2),
                counted(Items.NETHER_WART, 2),
                counted(Items.FEATHER, 2),
                counted(PsitweaksItems.CHAOTIC_FACTOR, 2)));
        addProgram(recipes, PsitweaksItems.PROGRAM_METEOR_LINE, List.of(
                counted(PsitweaksItems.PSYCHEONIC_METAL_INGOT, 4),
                counted(Items.NETHER_STAR, 2),
                counted(Items.DRAGON_HEAD, 2)));
        addProgram(recipes, PsitweaksItems.PROGRAM_SUPREME_INFUSION, List.of(
                counted(Items.AMETHYST_BLOCK, 2),
                counted(PsitweaksItems.FLASHMETAL, 2),
                counted(PsitweaksItems.ALLOY_PSION, 3),
                counted(Items.NETHERITE_INGOT, 1)));
        addProgram(recipes, PsitweaksItems.PROGRAM_MOLECULAR_DIVIDER, List.of(
                counted(PsitweaksItems.ECHO_CONTROL_CIRCUIT, 4),
                counted(Items.QUARTZ_BLOCK, 2),
                counted(PsitweaksItems.HEAVY_PSIMETAL, 2)));
        addProgram(recipes, PsitweaksItems.PROGRAM_GUILLOTINE, List.of(
                counted(Items.WITHER_SKELETON_SKULL, 1),
                counted(Items.ANVIL, 3),
                counted(ModItems.psimetalSword.get(), 1),
                counted(Items.BONE, 2),
                counted(Items.ROTTEN_FLESH, 1)));
        addProgram(recipes, PsitweaksItems.PROGRAM_ACTIVE_AIR_MINE, List.of(
                counted(Items.TNT, 2),
                counted(ModItems.psidust.get(), 4),
                counted(ModItems.psigem.get(), 2)));
        addProgram(recipes, PsitweaksItems.PROGRAM_DIE_FLEX, List.of(
                counted(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT, 3),
                counted(PsitweaksItems.FLASHMETAL, 3),
                counted(PsitweaksItems.CHAOTIC_FACTOR, 2)));
        addProgramUpgrade(recipes, PsitweaksItems.PROGRAM_JUMP_FLEX, List.of(
                counted(PsitweaksItems.PROGRAM_DIE_FLEX, 1),
                counted(PsitweaksItems.ECHO_CONTROL_CIRCUIT, 3),
                counted(PsitweaksItems.HEAVY_PSIMETAL, 3),
                counted(PsitweaksItems.ANTINITE_INGOT, 2)));
        addProgramUpgrade(recipes, PsitweaksItems.PROGRAM_SWITCH_FLEX, List.of(
                counted(PsitweaksItems.PROGRAM_JUMP_FLEX, 1),
                counted(PsitweaksItems.HYPOSTASIS_CONTROL_CIRCUIT, 3),
                counted(PsitweaksItems.PSYCHEONIC_METAL_INGOT, 3),
                counted(Items.NETHER_STAR, 2)));
        addProgram(recipes, PsitweaksItems.PROGRAM_MATERIAL_MUTATION, List.of(
                counted(PsitweaksItems.PHILOSOPHERS_STONE, 1),
                counted(Items.SCULK_CATALYST, 1),
                counted(PsitweaksItems.ANTINITE_INGOT, 2),
                counted(PsitweaksItems.CHAOTIC_FACTOR, 2),
                counted(PsitweaksItems.PSIONIC_ECHO, 2)));
        addProgram(recipes, PsitweaksItems.PROGRAM_MASS_BLOCK_BREAK, List.of(
                counted(Items.TNT, 4),
                counted(ModItems.psigem.get(), 2),
                counted(PsitweaksItems.CHAOTIC_PSIMETAL, 2)));
    }

    private static void addProgram(Map<ResourceLocation, JsonObject> recipes, ItemLike program,
            List<CountedIngredient> ingredients) {
        List<CountedIngredient> withBlankProgram = new java.util.ArrayList<>();
        withBlankProgram.add(counted(PsitweaksItems.PROGRAM_BLANK, 1));
        withBlankProgram.addAll(ingredients);
        add(recipes, itemPath(program), shapelessCounted(withBlankProgram, program, 1));
    }

    private static void addProgramUpgrade(Map<ResourceLocation, JsonObject> recipes, ItemLike program,
            List<CountedIngredient> ingredients) {
        add(recipes, itemPath(program), shapelessCounted(ingredients, program, 1));
    }

    private static CountedIngredient counted(ItemLike item, int count) {
        return new CountedIngredient(item, count);
    }

    private record CountedIngredient(ItemLike item, int count) {
    }

    private static JsonObject cooking(String type, JsonObject ingredient, ItemLike result, double experience,
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

    private static JsonObject ingredient(ItemLike item) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", itemId(item));
        return ingredient;
    }

    private static JsonObject result(ItemLike item, int count) {
        JsonObject result = new JsonObject();
        result.addProperty("count", count);
        result.addProperty("id", itemId(item));
        return result;
    }

    private static void add(Map<ResourceLocation, JsonObject> recipes, String id, JsonObject recipe) {
        recipes.put(
                Psitweaks.location("fallback/" + id),
                RecipeConditionHelper.excludeMod(recipe, MEKANISM)
        );
    }

    private static String itemId(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private static String itemPath(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }
}
