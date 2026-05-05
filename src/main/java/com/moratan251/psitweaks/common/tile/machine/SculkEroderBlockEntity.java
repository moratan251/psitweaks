package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.client.jei.PsitweaksMekanismJeiRecipeTypes;
import com.moratan251.psitweaks.common.recipe.SculkEroderRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import java.util.List;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SculkEroderBlockEntity extends TileEntityElectricMachine {
    public SculkEroderBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.SCULK_ERODER, pos, state, 200);
    }

    @Override
    public IMekanismRecipeTypeProvider<net.minecraft.world.item.crafting.SingleRecipeInput, ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.ENRICHING;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackToItemStackRecipe> recipeViewerType() {
        return PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER;
    }

    @Override
    public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
        return findFirstSculkRecipe(inputHandler.getInput());
    }

    @Override
    public boolean containsRecipe(ItemStack input) {
        return findFirstSculkRecipe(input) != null;
    }

    @Override
    public ItemStackToItemStackRecipe findFirstRecipe(ItemStack input) {
        return findFirstSculkRecipe(input);
    }

    @Override
    public ItemStackToItemStackRecipe findFirstRecipe(IInputHandler<ItemStack> inputHandler) {
        return findFirstSculkRecipe(inputHandler.getInput());
    }

    private ItemStackToItemStackRecipe findFirstSculkRecipe(ItemStack input) {
        if (input.isEmpty() || level == null || !isBlockItem(input.getItem())) {
            return null;
        }
        for (var holder : level.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.SCULK_ERODER.get())) {
            SculkEroderRecipe recipe = holder.value();
            if (recipe.matchesInput(input)) {
                return recipe.asMekanismRecipe();
            }
        }
        return null;
    }

    public static List<ItemStackToItemStackRecipe> getAllSculkRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.SCULK_ERODER.get()).stream()
                .map(holder -> holder.value().asMekanismRecipe())
                .toList();
    }

    public static List<RecipeHolder<ItemStackToItemStackRecipe>> getAllSculkRecipeHolders(Level level) {
        return level.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.SCULK_ERODER.get()).stream()
                .map(holder -> new RecipeHolder<ItemStackToItemStackRecipe>(holder.id(), holder.value().asMekanismRecipe()))
                .toList();
    }

    private static boolean isBlockItem(Item item) {
        Block block = Block.byItem(item);
        return block != Blocks.AIR;
    }
}
