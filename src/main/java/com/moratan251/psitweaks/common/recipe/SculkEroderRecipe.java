package com.moratan251.psitweaks.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.impl.EnrichingIRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

public class SculkEroderRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient input;
    private final int inputCount;
    private final ItemStack output;
    private ItemStackToItemStackRecipe mekanismRecipe;

    public SculkEroderRecipe(ResourceLocation id, Ingredient input, int inputCount, ItemStack output) {
        this.id = id;
        this.input = input;
        this.inputCount = inputCount;
        this.output = output;
    }

    public boolean matchesInput(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() >= inputCount && input.test(stack);
    }

    public ItemStackToItemStackRecipe asMekanismRecipe() {
        if (mekanismRecipe == null) {
            mekanismRecipe = new EnrichingIRecipe(id, IngredientCreatorAccess.item().from(input, inputCount), output.copy());
        }
        return mekanismRecipe;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return container.getContainerSize() > 0 && matchesInput(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PsitweaksRecipeSerializers.SCULK_ERODER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PsitweaksRecipeTypes.SCULK_ERODER.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<SculkEroderRecipe> {

        @Override
        public SculkEroderRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonObject inputObject = GsonHelper.getAsJsonObject(json, "input");
            JsonElement ingredientElement = GsonHelper.isValidNode(inputObject, "ingredient") ? inputObject.get("ingredient") : inputObject;
            Ingredient ingredient = Ingredient.fromJson(ingredientElement);
            int inputCount = GsonHelper.getAsInt(inputObject, "count", 1);
            if (inputCount < 1) {
                throw new IllegalArgumentException("Sculk eroder input count must be >= 1: " + recipeId);
            }

            JsonObject outputObject = GsonHelper.getAsJsonObject(json, "output");
            ItemStack output = CraftingHelper.getItemStack(outputObject, true);
            if (output.isEmpty()) {
                throw new IllegalArgumentException("Sculk eroder output cannot be empty: " + recipeId);
            }
            return new SculkEroderRecipe(recipeId, ingredient, inputCount, output);
        }

        @Override
        public SculkEroderRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            int inputCount = Math.max(1, buffer.readVarInt());
            ItemStack output = buffer.readItem();
            return new SculkEroderRecipe(recipeId, ingredient, inputCount, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SculkEroderRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeVarInt(recipe.inputCount);
            buffer.writeItem(recipe.output);
        }
    }
}
