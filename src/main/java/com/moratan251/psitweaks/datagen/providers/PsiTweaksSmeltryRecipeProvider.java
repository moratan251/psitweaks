package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class PsiTweaksSmeltryRecipeProvider extends RecipeProvider {
    private static final int INGOT_AMOUNT = 90;
    private static final int BLOCK_AMOUNT = INGOT_AMOUNT * 9;

    public PsiTweaksSmeltryRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        addRecipes(consumer);
    }

    public static void addRecipes(Consumer<FinishedRecipe> consumer) {
        alloy(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/alloys/molten_chaotic_psimetal"),
                List.of(
                        new FluidInput("forge:molten_ebony_psimetal", INGOT_AMOUNT),
                        new FluidInput("forge:molten_ivory_psimetal", INGOT_AMOUNT),
                        new FluidInput("forge:molten_psigem", 50)

                ),
                "forge:molten_chaotic_psimetal",
                180,
                1000,
                List.of("tconstruct", "psi"),
                List.of(
                        "forge:molten_ebony_psimetal",
                        "forge:molten_ivory_psimetal",
                        "forge:molten_chaotic_psimetal",
                        "forge:molten_psigem"
                ));

        alloy(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/alloys/molten_flashmetal"),
                List.of(
                        new FluidInput("forge:molten_refined_glowstone", 540),
                        new FluidInput("forge:molten_chaotic_psimetal", 270)
                ),
                "forge:molten_flashmetal",
                INGOT_AMOUNT,
                900,
                List.of("tconstruct"),
                List.of(
                        "forge:molten_refined_glowstone",
                        "forge:molten_chaotic_psimetal",
                        "forge:molten_flashmetal"
                ));

        alloy(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/alloys/molten_heavy_psimetal"),
                List.of(
                        new FluidInput("forge:molten_flashmetal", 360),
                        new FluidInput("forge:molten_debris", 360),
                        new FluidInput("forge:molten_psionic_echo", 50)
                ),
                "forge:molten_heavy_psimetal",
                INGOT_AMOUNT,
                1300,
                List.of("tconstruct"),
                List.of(
                        "forge:molten_flashmetal",
                        "forge:molten_debris",
                        "forge:molten_psionic_echo",
                        "forge:molten_heavy_psimetal"
                ));

        meltingWithItem(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/melting/material/psigem"),
                "psi:psigem",
                "forge:molten_psigem",
                100,
                1000,
                75,
                List.of("tconstruct", "psi"),
                List.of("forge:molten_psigem"));

        meltingWithItem(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/melting/material/psionic_echo"),
                "psitweaks:psionic_echo",
                "forge:molten_psionic_echo",
                100,
                1000,
                75,
                List.of("tconstruct"),
                List.of("forge:molten_psionic_echo"));

        meltingWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/melting/metal/chaotic_psimetal/block"),
                "forge:storage_blocks/chaotic_psimetal",
                "forge:molten_chaotic_psimetal",
                BLOCK_AMOUNT,
                1000,
                75 * 3,
                List.of("tconstruct"),
                List.of("forge:molten_chaotic_psimetal"));

        meltingWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/melting/metal/flashmetal/block"),
                "forge:storage_blocks/flashmetal",
                "forge:molten_flashmetal",
                BLOCK_AMOUNT,
                800,
                35 * 3,
                List.of("tconstruct"),
                List.of("forge:molten_flashmetal"));

        meltingWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/melting/metal/heavy_psimetal/block"),
                "forge:storage_blocks/heavy_psimetal",
                "forge:molten_heavy_psimetal",
                BLOCK_AMOUNT,
                1300,
                100 * 3,
                List.of("tconstruct"),
                List.of("forge:molten_heavy_psimetal"));

        castingBasinWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/casting/metal/chaotic_psimetal/block"),
                "forge:molten_chaotic_psimetal",
                BLOCK_AMOUNT,
                "forge:storage_blocks/chaotic_psimetal",
                95 * 3,
                List.of("tconstruct"),
                List.of("forge:storage_blocks/chaotic_psimetal"),
                List.of("forge:molten_chaotic_psimetal"));

        castingBasinWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/casting/metal/flashmetal/block"),
                "forge:molten_flashmetal",
                BLOCK_AMOUNT,
                "forge:storage_blocks/flashmetal",
                70 * 3,
                List.of("tconstruct"),
                List.of("forge:storage_blocks/flashmetal"),
                List.of("forge:molten_flashmetal"));

        castingBasinWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/casting/metal/heavy_psimetal/block"),
                "forge:molten_heavy_psimetal",
                BLOCK_AMOUNT,
                "forge:storage_blocks/heavy_psimetal",
                85 * 3,
                List.of("tconstruct"),
                List.of("forge:storage_blocks/heavy_psimetal"),
                List.of("forge:molten_heavy_psimetal"));

        meltingWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/melting/metal/psimetal/block"),
                "forge:storage_blocks/psimetal",
                "forge:molten_psimetal",
                BLOCK_AMOUNT,
                1000,
                60 * 3,
                List.of("tconstruct", "psi"),
                List.of("forge:molten_psimetal"));

        meltingWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/melting/metal/ebony_psimetal/block"),
                "forge:storage_blocks/ebony_psimetal",
                "forge:molten_ebony_psimetal",
                BLOCK_AMOUNT,
                1000,
                65 * 3,
                List.of("tconstruct", "psi"),
                List.of("forge:molten_ebony_psimetal"));

        meltingWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/melting/metal/ivory_psimetal/block"),
                "forge:storage_blocks/ivory_psimetal",
                "forge:molten_ivory_psimetal",
                BLOCK_AMOUNT,
                1000,
                65 * 3,
                List.of("tconstruct", "psi"),
                List.of("forge:molten_ivory_psimetal"));

        castingBasinWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/casting/metal/psimetal/block"),
                "forge:molten_psimetal",
                BLOCK_AMOUNT,
                "forge:storage_blocks/psimetal",
                75 * 3,
                List.of("tconstruct", "psi"),
                List.of("forge:storage_blocks/psimetal"),
                List.of("forge:molten_psimetal"));

        castingBasinWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/casting/metal/ebony_psimetal/block"),
                "forge:molten_ebony_psimetal",
                BLOCK_AMOUNT,
                "forge:storage_blocks/ebony_psimetal",
                82 * 3,
                List.of("tconstruct", "psi"),
                List.of("forge:storage_blocks/ebony_psimetal"),
                List.of("forge:molten_ebony_psimetal"));

        castingBasinWithTag(consumer,
                ResourceLocation.fromNamespaceAndPath("psitweaks", "smeltery/casting/metal/ivory_psimetal/block"),
                "forge:molten_ivory_psimetal",
                BLOCK_AMOUNT,
                "forge:storage_blocks/ivory_psimetal",
                78 * 3,
                List.of("tconstruct", "psi"),
                List.of("forge:storage_blocks/ivory_psimetal"),
                List.of("forge:molten_ivory_psimetal"));
    }

    private static void alloy(Consumer<FinishedRecipe> consumer, ResourceLocation id, List<FluidInput> inputs,
                              String resultTag, int resultAmount, int temperature, List<String> requiredMods,
                              List<String> requiredFluidTags) {
        consumer.accept(new AlloyRecipeResult(id, inputs, resultTag, resultAmount, temperature, requiredMods, requiredFluidTags));
    }

    private static void meltingWithItem(Consumer<FinishedRecipe> consumer, ResourceLocation id, String ingredientItem,
                                        String resultTag, int resultAmount, int temperature, int time,
                                        List<String> requiredMods, List<String> requiredFluidTags) {
        consumer.accept(new MeltingRecipeResult(id, ingredientItem, resultTag, resultAmount, temperature, time, requiredMods, requiredFluidTags));
    }

    private static void meltingWithTag(Consumer<FinishedRecipe> consumer, ResourceLocation id, String ingredientTag,
                                       String resultTag, int resultAmount, int temperature, int time,
                                       List<String> requiredMods, List<String> requiredFluidTags) {
        consumer.accept(new MeltingTagRecipeResult(id, ingredientTag, resultTag, resultAmount, temperature, time, requiredMods, requiredFluidTags));
    }

    private static void castingBasinWithTag(Consumer<FinishedRecipe> consumer, ResourceLocation id, String fluidTag,
                                            int fluidAmount, String resultTag, int coolingTime, List<String> requiredMods,
                                            List<String> requiredItemTags, List<String> requiredFluidTags) {
        consumer.accept(new CastingBasinRecipeResult(id, fluidTag, fluidAmount, resultTag, coolingTime,
                requiredMods, requiredItemTags, requiredFluidTags));
    }

    private record FluidInput(String tag, int amount) {
    }

    private record AlloyRecipeResult(ResourceLocation id, List<FluidInput> inputs, String resultTag, int resultAmount,
                                     int temperature, List<String> requiredMods,
                                     List<String> requiredFluidTags) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", "tconstruct:alloy");

            JsonArray conditions = new JsonArray();
            for (String modId : requiredMods) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "forge:mod_loaded");
                condition.addProperty("modid", modId);
                conditions.add(condition);
            }
            for (String fluidTag : requiredFluidTags) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "mantle:tag_filled");
                condition.addProperty("registry", "minecraft:fluid");
                condition.addProperty("tag", fluidTag);
                conditions.add(condition);
            }
            json.add("conditions", conditions);

            JsonArray inputsArray = new JsonArray();
            for (FluidInput input : inputs) {
                JsonObject fluid = new JsonObject();
                fluid.addProperty("amount", input.amount());
                fluid.addProperty("tag", input.tag());
                inputsArray.add(fluid);
            }
            json.add("inputs", inputsArray);

            JsonObject result = new JsonObject();
            result.addProperty("amount", resultAmount);
            result.addProperty("tag", resultTag);
            json.add("result", result);
            json.addProperty("temperature", temperature);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    private record MeltingRecipeResult(ResourceLocation id, String ingredientItem, String resultTag, int resultAmount,
                                       int temperature, int time, List<String> requiredMods,
                                       List<String> requiredFluidTags) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", "tconstruct:melting");

            JsonArray conditions = new JsonArray();
            for (String modId : requiredMods) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "forge:mod_loaded");
                condition.addProperty("modid", modId);
                conditions.add(condition);
            }
            for (String fluidTag : requiredFluidTags) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "mantle:tag_filled");
                condition.addProperty("registry", "minecraft:fluid");
                condition.addProperty("tag", fluidTag);
                conditions.add(condition);
            }
            json.add("conditions", conditions);

            JsonObject ingredient = new JsonObject();
            ingredient.addProperty("item", ingredientItem);
            json.add("ingredient", ingredient);

            JsonObject result = new JsonObject();
            result.addProperty("amount", resultAmount);
            result.addProperty("tag", resultTag);
            json.add("result", result);
            json.addProperty("temperature", temperature);
            json.addProperty("time", time);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    private record MeltingTagRecipeResult(ResourceLocation id, String ingredientTag, String resultTag, int resultAmount,
                                          int temperature, int time, List<String> requiredMods,
                                          List<String> requiredFluidTags) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", "tconstruct:melting");

            JsonArray conditions = new JsonArray();
            for (String modId : requiredMods) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "forge:mod_loaded");
                condition.addProperty("modid", modId);
                conditions.add(condition);
            }
            for (String fluidTag : requiredFluidTags) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "mantle:tag_filled");
                condition.addProperty("registry", "minecraft:fluid");
                condition.addProperty("tag", fluidTag);
                conditions.add(condition);
            }
            json.add("conditions", conditions);

            JsonObject ingredient = new JsonObject();
            ingredient.addProperty("tag", ingredientTag);
            json.add("ingredient", ingredient);

            JsonObject result = new JsonObject();
            result.addProperty("amount", resultAmount);
            result.addProperty("tag", resultTag);
            json.add("result", result);
            json.addProperty("temperature", temperature);
            json.addProperty("time", time);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    private record CastingBasinRecipeResult(ResourceLocation id, String fluidTag, int fluidAmount, String resultTag,
                                            int coolingTime, List<String> requiredMods, List<String> requiredItemTags,
                                            List<String> requiredFluidTags) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", "tconstruct:casting_basin");

            JsonArray conditions = new JsonArray();
            for (String modId : requiredMods) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "forge:mod_loaded");
                condition.addProperty("modid", modId);
                conditions.add(condition);
            }
            for (String itemTag : requiredItemTags) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "mantle:tag_filled");
                condition.addProperty("tag", itemTag);
                conditions.add(condition);
            }
            for (String requiredFluidTag : requiredFluidTags) {
                JsonObject condition = new JsonObject();
                condition.addProperty("type", "mantle:tag_filled");
                condition.addProperty("registry", "minecraft:fluid");
                condition.addProperty("tag", requiredFluidTag);
                conditions.add(condition);
            }
            json.add("conditions", conditions);

            JsonObject fluid = new JsonObject();
            fluid.addProperty("amount", fluidAmount);
            fluid.addProperty("tag", fluidTag);
            json.add("fluid", fluid);

            JsonObject result = new JsonObject();
            result.addProperty("tag", resultTag);
            json.add("result", result);
            json.addProperty("cooling_time", coolingTime);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
