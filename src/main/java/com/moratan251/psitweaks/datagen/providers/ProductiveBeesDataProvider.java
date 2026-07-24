package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class ProductiveBeesDataProvider implements DataProvider {
    private static final List<GeneratedBee> BEES = List.of(
            bee("psi_psidust", "Psidust Bee", "サイダストのミツバチ",
                    "#DADCFB", "#AF98FF", "psi:psidust_block"),
            bee("psi_psimetal", "Psimetal Bee", "サイメタルのミツバチ",
                    "#C0C2FF", "#8C92D8", "psi:psimetal_block"),
            bee("psi_psigem", "Psigem Bee", "サイジェムのミツバチ",
                    "#6855B4", "#E0E8FE", "psi:psigem_block"),
            bee("psi_ebony_psimetal", "Ebony Psimetal Bee", "エボニーサイメタルのミツバチ",
                    "#202020", "#5A5050", "psi:ebony_psimetal_block"),
            bee("psi_ivory_psimetal", "Ivory Psimetal Bee", "アイボリーサイメタルのミツバチ",
                    "#F6F6E9", "#C2C1A2", "psi:ivory_psimetal_block"),
            bee("psitweaks_chaotic_psimetal", "Chaotic Psimetal Bee", "カオティックサイメタルのミツバチ",
                    "#7F7F7F", "#0F0A0A", "psitweaks:chaotic_psimetal_block"),
            bee("psitweaks_flashmetal", "Flashmetal Bee", "フラッシュメタルのミツバチ",
                    "#FFE86D", "#7570BF", "psitweaks:flashmetal_block"),
            bee("psitweaks_heavy_psimetal", "Heavy Psimetal Bee", "ヘビーサイメタルのミツバチ",
                    "#294C73", "#5F89B7", "psitweaks:heavy_psimetal_block"),
            bee("psitweaks_antinite", "Antinite Bee", "アンティナイトのミツバチ",
                    "#CCBF61", "#81772D", "psitweaks:antinite_block"),
            bee("psitweaks_hypostasis_gem", "Hypostasis Gem Bee", "ヒュポスタシスジェムのミツバチ",
                    "#FFB7F8", "#AE475C", "psitweaks:hypostasis_gem_block"),
            bee("psitweaks_psycheonic_metal", "Psycheonic Metal Bee", "プシオニックメタルのミツバチ",
                    "#7ED8E6", "#2D7681", "psitweaks:psycheonic_metal_block"));

    private static final List<BeeBreedingRecipe> BEE_BREEDING_RECIPES = List.of(
            breeding("psi_psimetal",
                    "productivebees:gold", "psitweaks:psi_psidust", "psitweaks:psi_psimetal"),
            breeding("psi_psigem",
                    "psitweaks:psi_psimetal", "productivebees:diamond", "psitweaks:psi_psigem"),
            breeding("psi_ebony_psimetal",
                    "psitweaks:psi_psimetal", "productivebees:coal", "psitweaks:psi_ebony_psimetal"),
            breeding("psi_ivory_psimetal",
                    "psitweaks:psi_psimetal", "productivebees:crystalline", "psitweaks:psi_ivory_psimetal"),
            breeding("psitweaks_chaotic_psimetal",
                    "psitweaks:psi_ebony_psimetal", "psitweaks:psi_ivory_psimetal",
                    "psitweaks:psitweaks_chaotic_psimetal"),
            breeding("psitweaks_flashmetal",
                    "psitweaks:psitweaks_chaotic_psimetal", "productivebees:refined_glowstone",
                    "psitweaks:psitweaks_flashmetal", "mekanism"),
            breeding("psitweaks_heavy_psimetal",
                    "psitweaks:psitweaks_flashmetal", "productivebees:netherite",
                    "psitweaks:psitweaks_heavy_psimetal"),
            breeding("psitweaks_antinite",
                    "productivebees:ender", "psitweaks:psitweaks_flashmetal",
                    "psitweaks:psitweaks_antinite"),
            breeding("psitweaks_psycheonic_metal",
                    "psitweaks:psitweaks_hypostasis_gem", "psitweaks:psitweaks_heavy_psimetal",
                    "psitweaks:psitweaks_psycheonic_metal"));

    private static final List<BeeConversionRecipe> BEE_CONVERSION_RECIPES = List.of(
            conversion("psi_psidust",
                    "productivebees:redstone", "psitweaks:psi_psidust", "psi:psidust"),
            conversion("psitweaks_hypostasis_gem",
                    "psitweaks:psitweaks_antinite", "psitweaks:psitweaks_hypostasis_gem",
                    "psitweaks:hypostasis_gem"));

    private final PackOutput.PathProvider beePathProvider;
    private final PackOutput.PathProvider recipePathProvider;

    public ProductiveBeesDataProvider(PackOutput output) {
        this.beePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "productivebees");
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (GeneratedBee bee : BEES) {
            futures.add(DataProvider.saveStable(
                    output,
                    definition(bee),
                    beePathProvider.json(Psitweaks.location(bee.id()))));
        }
        for (BeeBreedingRecipe recipe : BEE_BREEDING_RECIPES) {
            futures.add(DataProvider.saveStable(
                    output,
                    breedingRecipe(recipe),
                    recipePathProvider.json(Psitweaks.location(
                            "productivebees/bee_breeding/" + recipe.id()))));
        }
        for (BeeConversionRecipe recipe : BEE_CONVERSION_RECIPES) {
            futures.add(DataProvider.saveStable(
                    output,
                    conversionRecipe(recipe),
                    recipePathProvider.json(Psitweaks.location(
                            "productivebees/bee_conversion/" + recipe.id()))));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks Productive Bees definitions";
    }

    static List<GeneratedBee> bees() {
        return BEES;
    }

    private static JsonObject definition(GeneratedBee bee) {
        JsonObject root = new JsonObject();
        root.addProperty("primaryColor", bee.primaryColor());
        root.addProperty("secondaryColor", bee.secondaryColor());
        root.addProperty("createComb", true);
        root.addProperty("flowerTag", bee.flowerTag());
        return root;
    }

    private static GeneratedBee bee(String id, String enUs, String jaJp,
                                    String primaryColor, String secondaryColor, String flowerBlock) {
        return new GeneratedBee(id, enUs, jaJp, primaryColor, secondaryColor, flowerBlock);
    }

    private static JsonObject breedingRecipe(BeeBreedingRecipe recipe) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "productivebees:bee_breeding");
        root.addProperty("parent1", recipe.parent1());
        root.addProperty("parent2", recipe.parent2());
        root.addProperty("offspring", recipe.offspring());
        root.add("neoforge:conditions", modLoadedConditions(recipe.requiredMods()));
        return root;
    }

    private static JsonObject conversionRecipe(BeeConversionRecipe recipe) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "productivebees:bee_conversion");
        root.addProperty("source", recipe.source());
        root.addProperty("result", recipe.result());
        JsonObject item = new JsonObject();
        item.addProperty("item", recipe.item());
        root.add("item", item);
        root.add("neoforge:conditions", modLoadedConditions(recipe.requiredMods()));
        return root;
    }

    private static JsonArray modLoadedConditions(List<String> additionalMods) {
        JsonArray conditions = new JsonArray();
        conditions.add(modLoadedCondition("productivebees"));
        additionalMods.stream()
                .map(ProductiveBeesDataProvider::modLoadedCondition)
                .forEach(conditions::add);
        return conditions;
    }

    private static JsonObject modLoadedCondition(String modId) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", modId);
        return condition;
    }

    private static BeeBreedingRecipe breeding(String id, String parent1, String parent2, String offspring,
                                               String... requiredMods) {
        return new BeeBreedingRecipe(id, parent1, parent2, offspring, List.of(requiredMods));
    }

    private static BeeConversionRecipe conversion(String id, String source, String result, String item,
                                                  String... requiredMods) {
        return new BeeConversionRecipe(id, source, result, item, List.of(requiredMods));
    }

    record GeneratedBee(String id, String enUs, String jaJp,
                        String primaryColor, String secondaryColor, String flowerBlock) {
        String flowerTag() {
            return "psitweaks:productivebees/flowers/" + id;
        }
    }

    private record BeeBreedingRecipe(String id, String parent1, String parent2, String offspring,
                                     List<String> requiredMods) {
    }

    private record BeeConversionRecipe(String id, String source, String result, String item,
                                       List<String> requiredMods) {
    }
}
