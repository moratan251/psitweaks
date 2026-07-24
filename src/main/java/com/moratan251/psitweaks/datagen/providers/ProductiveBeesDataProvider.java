package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.item.base.ModItems;

public final class ProductiveBeesDataProvider implements DataProvider {
    private static final List<GeneratedBee> BEES = List.of(
            bee("psi_psidust", "Psidust Bee", "サイダストのミツバチ",
                    "#DADCFB", "#AF98FF", ModBlocks.psidustBlock.get()),
            bee("psi_psimetal", "Psimetal Bee", "サイメタルのミツバチ",
                    "#C0C2FF", "#8C92D8", ModBlocks.psimetalBlock.get()),
            bee("psi_psigem", "Psigem Bee", "サイジェムのミツバチ",
                    "#6855B4", "#E0E8FE", ModBlocks.psigemBlock.get()),
            bee("psi_ebony_psimetal", "Ebony Psimetal Bee", "エボニーサイメタルのミツバチ",
                    "#202020", "#5A5050", ModBlocks.psimetalEbony.get()),
            bee("psi_ivory_psimetal", "Ivory Psimetal Bee", "アイボリーサイメタルのミツバチ",
                    "#F6F6E9", "#C2C1A2", ModBlocks.psimetalIvory.get()),
            bee("psitweaks_chaotic_psimetal", "Chaotic Psimetal Bee", "カオティックサイメタルのミツバチ",
                    "#7F7F7F", "#0F0A0A", PsitweaksBlocks.CHAOTIC_PSIMETAL_BLOCK.get()),
            bee("psitweaks_flashmetal", "Flashmetal Bee", "フラッシュメタルのミツバチ",
                    "#FFE86D", "#7570BF", PsitweaksBlocks.FLASHMETAL_BLOCK.get()),
            bee("psitweaks_heavy_psimetal", "Heavy Psimetal Bee", "ヘビーサイメタルのミツバチ",
                    "#294C73", "#5F89B7", PsitweaksBlocks.HEAVY_PSIMETAL_BLOCK.get()),
            bee("psitweaks_antinite", "Antinite Bee", "アンティナイトのミツバチ",
                    "#CCBF61", "#81772D", PsitweaksBlocks.ANTINITE_BLOCK.get()),
            bee("psitweaks_hypostasis_gem", "Hypostasis Gem Bee", "ヒュポスタシスジェムのミツバチ",
                    "#FFB7F8", "#AE475C", PsitweaksBlocks.HYPOSTASIS_GEM_BLOCK.get()),
            beeWithoutSelfBreeding("psitweaks_psycheonic_metal",
                    "Psycheonic Metal Bee", "プシオニックメタルのミツバチ",
                    "#7ED8E6", "#2D7681", PsitweaksBlocks.PSYCHEONIC_METAL_BLOCK.get()));

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
                    "psitweaks:psitweaks_antinite"));

    private static final List<BeeConversionRecipe> BEE_CONVERSION_RECIPES = List.of(
            conversion("psi_psidust",
                    "productivebees:redstone", "psitweaks:psi_psidust", ModItems.psidust.get()),
            conversion("psitweaks_hypostasis_gem",
                    "psitweaks:psitweaks_antinite", "psitweaks:psitweaks_hypostasis_gem",
                    PsitweaksItems.HYPOSTASIS_GEM),
            conversion("psitweaks_psycheonic_metal",
                    "psitweaks:psitweaks_heavy_psimetal", "psitweaks:psitweaks_psycheonic_metal",
                    PsitweaksBlocks.PSYCHEONIC_METAL_BLOCK));

    private static final List<CentrifugeRecipe> CENTRIFUGE_RECIPES = List.of(
            centrifuge("psi_psidust", ModItems.psidust.get(), 1, 2, 0.5D),
            centrifuge("psi_psimetal", ModItems.psimetal.get(), 1, 1, 0.4D),
            centrifuge("psi_psigem", ModItems.psigem.get(), 1, 1, 0.2D),
            centrifuge("psi_ebony_psimetal", ModItems.ebonyPsimetal.get(), 1, 1, 0.35D),
            centrifuge("psi_ivory_psimetal", ModItems.ivoryPsimetal.get(), 1, 1, 0.35D),
            centrifuge("psitweaks_chaotic_psimetal", PsitweaksItems.CHAOTIC_PSIMETAL, 1, 1, 0.2D),
            centrifuge("psitweaks_flashmetal", PsitweaksItems.FLASHMETAL_NUGGET, 4, 6, 0.5D),
            centrifuge("psitweaks_heavy_psimetal", PsitweaksItems.HEAVY_PSIMETAL_NUGGET, 3, 4, 0.3D),
            centrifuge("psitweaks_antinite", PsitweaksItems.ANTINITE_NUGGET, 2, 3, 0.2D),
            centrifuge("psitweaks_hypostasis_gem", PsitweaksItems.HYPOSTASIS_GEM, 1, 1, 0.03D),
            centrifuge("psitweaks_psycheonic_metal", PsitweaksItems.PSYCHEONIC_METAL_NUGGET, 1, 1, 0.1D));

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
        for (GeneratedBee bee : BEES) {
            futures.add(DataProvider.saveStable(
                    output,
                    advancedBeehiveRecipe(bee),
                    recipePathProvider.json(Psitweaks.location(
                            "productivebees/bee_produce/" + bee.id()))));
        }
        for (CentrifugeRecipe recipe : CENTRIFUGE_RECIPES) {
            futures.add(DataProvider.saveStable(
                    output,
                    centrifugeRecipe(recipe),
                    recipePathProvider.json(Psitweaks.location(
                            "productivebees/centrifuge/" + recipe.beeId()))));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "PsiTweaks Productive Bees data";
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
        if (!bee.selfBreed()) {
            root.addProperty("selfbreed", false);
        }
        return root;
    }

    private static GeneratedBee bee(String id, String enUs, String jaJp,
                                    String primaryColor, String secondaryColor, Block flowerBlock) {
        return new GeneratedBee(id, enUs, jaJp, primaryColor, secondaryColor, flowerBlock, true);
    }

    private static GeneratedBee beeWithoutSelfBreeding(String id, String enUs, String jaJp,
                                                       String primaryColor, String secondaryColor,
                                                       Block flowerBlock) {
        return new GeneratedBee(id, enUs, jaJp, primaryColor, secondaryColor, flowerBlock, false);
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
        item.addProperty("item", itemId(recipe.item()));
        root.add("item", item);
        root.add("neoforge:conditions", modLoadedConditions(recipe.requiredMods()));
        return root;
    }

    private static JsonObject advancedBeehiveRecipe(GeneratedBee bee) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "productivebees:advanced_beehive");
        root.addProperty("ingredient", bee.resourceId());

        JsonArray results = new JsonArray();
        JsonObject honeycombResult = new JsonObject();
        honeycombResult.add("item", configurableHoneycomb(bee.resourceId()));
        results.add(honeycombResult);

        JsonObject pollenResult = new JsonObject();
        JsonObject pollen = new JsonObject();
        pollen.addProperty("tag", "c:pollens");
        pollenResult.add("item", pollen);
        pollenResult.addProperty("chance", 0.05D);
        results.add(pollenResult);

        root.add("results", results);
        root.add("neoforge:conditions", modLoadedConditions(List.of()));
        return root;
    }

    private static JsonObject centrifugeRecipe(CentrifugeRecipe recipe) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "productivebees:centrifuge");
        root.add("ingredient", configurableHoneycomb(beeResourceId(recipe.beeId())));

        JsonArray outputs = new JsonArray();
        JsonObject materialOutput = new JsonObject();
        JsonObject material = new JsonObject();
        material.addProperty("item", itemId(recipe.outputItem()));
        materialOutput.add("item", material);
        if (recipe.min() != 1 || recipe.max() != 1) {
            materialOutput.addProperty("min", recipe.min());
            materialOutput.addProperty("max", recipe.max());
        }
        materialOutput.addProperty("chance", recipe.chance());
        outputs.add(materialOutput);

        JsonObject waxOutput = new JsonObject();
        JsonObject wax = new JsonObject();
        wax.addProperty("item", "productivebees:wax");
        waxOutput.add("item", wax);
        outputs.add(waxOutput);

        root.add("outputs", outputs);
        root.add("neoforge:conditions", modLoadedConditions(List.of()));
        return root;
    }

    private static JsonObject configurableHoneycomb(String beeType) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("type", "productivebees:component");
        JsonObject components = new JsonObject();
        components.addProperty("productivebees:bee_type", beeType);
        ingredient.add("components", components);
        ingredient.addProperty("items", "productivebees:configurable_honeycomb");
        return ingredient;
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

    private static BeeConversionRecipe conversion(String id, String source, String result, ItemLike item,
                                                   String... requiredMods) {
        return new BeeConversionRecipe(id, source, result, item, List.of(requiredMods));
    }

    private static CentrifugeRecipe centrifuge(String beeId, ItemLike outputItem,
                                               int min, int max, double chance) {
        return new CentrifugeRecipe(beeId, outputItem, min, max, chance);
    }

    private static String itemId(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private static String beeResourceId(String beeId) {
        return "psitweaks:" + beeId;
    }

    record GeneratedBee(String id, String enUs, String jaJp,
                        String primaryColor, String secondaryColor, Block flowerBlock,
                        boolean selfBreed) {
        String flowerTag() {
            return "psitweaks:productivebees/flowers/" + id;
        }

        String resourceId() {
            return beeResourceId(id);
        }
    }

    private record BeeBreedingRecipe(String id, String parent1, String parent2, String offspring,
                                     List<String> requiredMods) {
    }

    private record BeeConversionRecipe(String id, String source, String result, ItemLike item,
                                       List<String> requiredMods) {
    }

    private record CentrifugeRecipe(String beeId, ItemLike outputItem, int min, int max, double chance) {
    }
}
