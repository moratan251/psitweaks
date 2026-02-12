package com.moratan251.psitweaks.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ProgramResearchRecipeBuilder {

    private final ItemStack result;
    private final List<RequiredInputData> inputs = new ArrayList<>();
    private int energy = 0;
    private int time = 200;

    private ProgramResearchRecipeBuilder(ItemStack result) {
        this.result = result.copy();
    }

    public static ProgramResearchRecipeBuilder research(ItemLike result) {
        return new ProgramResearchRecipeBuilder(new ItemStack(result));
    }

    public ProgramResearchRecipeBuilder requires(ItemLike item, int count) {
        return requires(Ingredient.of(item), count);
    }

    public ProgramResearchRecipeBuilder requires(Ingredient ingredient, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Program research input count must be >= 1");
        }
        if (inputs.size() >= ProgramResearchRecipe.MAX_INPUT_SLOTS) {
            throw new IllegalStateException("Program research supports up to " + ProgramResearchRecipe.MAX_INPUT_SLOTS + " inputs");
        }
        inputs.add(new RequiredInputData(ingredient, count));
        return this;
    }

    public ProgramResearchRecipeBuilder energy(int energy) {
        if (energy < 0) {
            throw new IllegalArgumentException("Program research energy must be >= 0");
        }
        this.energy = energy;
        return this;
    }

    public ProgramResearchRecipeBuilder time(int time) {
        if (time < 1) {
            throw new IllegalArgumentException("Program research time must be >= 1");
        }
        this.time = time;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
        if (inputs.isEmpty()) {
            throw new IllegalStateException("Program research recipe has no inputs: " + recipeId);
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("Program research recipe output cannot be empty: " + recipeId);
        }
        consumer.accept(new Result(recipeId, result, List.copyOf(inputs), energy, time));
    }

    private record RequiredInputData(Ingredient ingredient, int count) {
    }

    private record Result(ResourceLocation id, ItemStack result, List<RequiredInputData> inputs, int energy,
                          int time) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", "psitweaks:program_research");

            JsonArray inputArray = new JsonArray();
            for (RequiredInputData input : inputs) {
                JsonObject inputObject = new JsonObject();
                inputObject.add("ingredient", input.ingredient().toJson());
                inputObject.addProperty("count", input.count());
                inputArray.add(inputObject);
            }
            json.add("inputs", inputArray);

            JsonObject outputObject = new JsonObject();
            ResourceLocation itemId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(result.getItem()),
                    "Unregistered output item in program research recipe: " + id);
            outputObject.addProperty("item", itemId.toString());
            if (result.getCount() != 1) {
                outputObject.addProperty("count", result.getCount());
            }
            if (result.hasTag()) {
                outputObject.addProperty("nbt", result.getTag().toString());
            }
            json.add("output", outputObject);

            json.addProperty("energy", energy);
            json.addProperty("time", time);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return PsitweaksRecipeSerializers.PROGRAM_RESEARCH.get();
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
