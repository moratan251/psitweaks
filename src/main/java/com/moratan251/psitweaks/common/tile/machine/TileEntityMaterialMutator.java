package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.recipe.MaterialMutationInjectingRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.BiPredicate;

public class TileEntityMaterialMutator extends TileEntityAdvancedElectricMachine {

    private static final int BASE_TICKS_REQUIRED = 200;
    private static final long GAS_TANK_CAPACITY = 1_010L;

    public TileEntityMaterialMutator(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.MATERIAL_MUTATOR, pos, state, BASE_TICKS_REQUIRED);
    }

    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        BiPredicate<Gas, AutomationType> canExtract = allowExtractingChemical() ? ChemicalTankBuilder.GAS.alwaysTrueBi : ChemicalTankBuilder.GAS.notExternal;
        gasTank = ChemicalTankBuilder.GAS.create(
                GAS_TANK_CAPACITY,
                canExtract,
                (gas, automationType) -> containsRecipeBA(itemInputHandler.getInput(), gas),
                this::containsRecipeB,
                recipeCacheUnpauseListener
        );
        builder.addTank(gasTank);
        return builder.build();
    }

    @Override
    public IMekanismRecipeTypeProvider<ItemStackGasToItemStackRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.INJECTING;
    }

    @Override
    public ItemStackGasToItemStackRecipe getRecipe(int cacheIndex) {
        return MaterialMutationRecipeHandler.findFirstMachineRecipe(itemInputHandler.getInput(), gasInputHandler.getInput());
    }

    @Override
    public ItemStackGasToItemStackRecipe findFirstRecipe(ItemStack inputA, GasStack inputB) {
        return MaterialMutationRecipeHandler.findFirstMachineRecipe(inputA, inputB);
    }

    @Override
    public boolean containsRecipeAB(ItemStack inputA, GasStack inputB) {
        if (inputA.isEmpty()) {
            return containsRecipeB(inputB);
        } else if (inputB.isEmpty()) {
            return true;
        }
        return MaterialMutationRecipeHandler.containsMachineRecipe(inputA, inputB.getType());
    }

    @Override
    public boolean containsRecipeBA(ItemStack inputA, GasStack inputB) {
        if (inputB.isEmpty()) {
            return containsRecipeA(inputA);
        } else if (inputA.isEmpty()) {
            return true;
        }
        return MaterialMutationRecipeHandler.containsMachineRecipe(inputA, inputB.getType());
    }

    @Override
    public boolean containsRecipeA(ItemStack input) {
        return MaterialMutationRecipeHandler.containsMachineRecipeInput(input);
    }

    @Override
    public boolean containsRecipeB(GasStack input) {
        return MaterialMutationRecipeHandler.containsMachineRecipeGas(input);
    }

    @Override
    public CachedRecipe<ItemStackGasToItemStackRecipe> createNewCachedRecipe(ItemStackGasToItemStackRecipe recipe, int cacheIndex) {
        CachedRecipe<ItemStackGasToItemStackRecipe> cachedRecipe = super.createNewCachedRecipe(recipe, cacheIndex);
        if (recipe instanceof MaterialMutationInjectingRecipe timedRecipe) {
            cachedRecipe.setRequiredTicks(() -> getScaledRecipeTime(timedRecipe.getProcessingTime()));
        }
        return cachedRecipe;
    }

    public static List<ItemStackGasToItemStackRecipe> getAllMutationMachineRecipes(Level level) {
        return MaterialMutationRecipeHandler.getAllMachineRecipes();
    }

    private int getScaledRecipeTime(int recipeTime) {
        long base = Math.max(1L, baseTicksRequired);
        long current = Math.max(1L, ticksRequired);
        long scaled = recipeTime * current;
        long roundedUp = (scaled + base - 1L) / base;
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, roundedUp));
    }
}
