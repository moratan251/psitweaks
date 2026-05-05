package com.moratan251.psitweaks.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeSerializers;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicEnrichingRecipe;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class SculkEroderRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final int inputCount;
    private final ItemStack output;
    private ItemStackToItemStackRecipe mekanismRecipe;

    public SculkEroderRecipe(Ingredient input, int inputCount, ItemStack output) {
        this.input = input;
        this.inputCount = Math.max(1, inputCount);
        this.output = output.copy();
    }

    public boolean matchesInput(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() >= inputCount && input.test(stack);
    }

    public int getInputCount() {
        return inputCount;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public ItemStackToItemStackRecipe asMekanismRecipe() {
        if (mekanismRecipe == null) {
            mekanismRecipe = new BasicEnrichingRecipe(
                    IngredientCreatorAccess.item().from(input, inputCount),
                    output.copy()
            );
        }
        return mekanismRecipe;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return matchesInput(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
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

    private record InputData(Ingredient ingredient, int count) {
        private static final Codec<InputData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(InputData::ingredient),
                Codec.INT.optionalFieldOf("count", 1).forGetter(InputData::count)
        ).apply(instance, InputData::new));
    }

    public static class Serializer implements RecipeSerializer<SculkEroderRecipe> {
        private static final MapCodec<SculkEroderRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                InputData.CODEC.fieldOf("input").forGetter(recipe -> new InputData(recipe.input, recipe.inputCount)),
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(recipe -> recipe.output)
        ).apply(instance, (input, output) -> new SculkEroderRecipe(input.ingredient(), input.count(), output)));
        private static final StreamCodec<RegistryFriendlyByteBuf, SculkEroderRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<SculkEroderRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SculkEroderRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SculkEroderRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            int count = Math.max(1, buf.readVarInt());
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            return new SculkEroderRecipe(ingredient, count, output);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, SculkEroderRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
            buf.writeVarInt(recipe.inputCount);
            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
        }
    }
}
