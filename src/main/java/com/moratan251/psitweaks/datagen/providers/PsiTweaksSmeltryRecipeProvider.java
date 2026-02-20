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
}
