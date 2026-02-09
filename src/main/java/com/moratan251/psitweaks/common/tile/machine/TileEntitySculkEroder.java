package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.recipe.SculkEroderRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TileEntitySculkEroder extends TileEntityElectricMachine {

    public TileEntitySculkEroder(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.SCULK_ERODER, pos, state, 200);
    }

    @Override
    public IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.ENRICHING;
    }

    @Override
    public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler.getInput());
    }

    @Override
    public boolean containsRecipe(ItemStack input) {
        return findFirstRecipe(input) != null;
    }

    @Override
    public ItemStackToItemStackRecipe findFirstRecipe(ItemStack input) {
        if (input.isEmpty() || level == null || !isBlockItem(input.getItem())) {
            return null;
        }
        for (SculkEroderRecipe recipe : level.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.SCULK_ERODER.get())) {
            if (recipe.matchesInput(input)) {
                return recipe.asMekanismRecipe();
            }
        }
        return null;
    }

    public static List<ItemStackToItemStackRecipe> getAllSculkRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(PsitweaksRecipeTypes.SCULK_ERODER.get()).stream()
                .map(SculkEroderRecipe::asMekanismRecipe)
                .toList();
    }

    private static boolean isBlockItem(Item item) {
        Block block = Block.byItem(item);
        return block != Blocks.AIR;
    }
}
