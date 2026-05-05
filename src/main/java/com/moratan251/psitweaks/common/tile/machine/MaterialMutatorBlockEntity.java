package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.client.jei.PsitweaksMekanismJeiRecipeTypes;
import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.basic.BasicInjectingRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MaterialMutatorBlockEntity extends TileEntityAdvancedElectricMachine {
    private static final int BASE_TICKS_REQUIRED = 200;
    private static final long CHEMICAL_TANK_CAPACITY = 1_010L;
    private static final long CHEMICAL_PER_CRAFT = 5L;

    public MaterialMutatorBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.MATERIAL_MUTATOR, pos, state, BASE_TICKS_REQUIRED);
    }

    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener, IContentsListener chemicalTankListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        java.util.function.Predicate<Chemical> validInput = chemical -> chemical == MaterialMutationRecipeHandler.getPsionicEchoChemical();
        chemicalTank = BasicChemicalTank.create(
                CHEMICAL_TANK_CAPACITY,
                allowExtractingChemical() ? BasicChemicalTank.alwaysTrueBi : BasicChemicalTank.notExternal,
                (chemical, automationType) -> validInput.test(chemical),
                validInput,
                chemicalTankListener
        );
        builder.addTank(chemicalTank);
        return builder.build();
    }

    @Override
    public IMekanismRecipeTypeProvider<mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, InputRecipeCache.ItemChemical<ItemStackChemicalToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.INJECTING;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackChemicalToItemStackRecipe> recipeViewerType() {
        return PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR;
    }

    @Override
    public ItemStackChemicalToItemStackRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(itemInputHandler.getInput(), chemicalInputHandler.getInput());
    }

    @Override
    public ItemStackChemicalToItemStackRecipe findFirstRecipe(ItemStack inputA, ChemicalStack inputB) {
        MaterialMutationRecipeHandler.MachineRecipe recipe = MaterialMutationRecipeHandler.findFirstMachineRecipe(inputA, inputB);
        return recipe == null ? null : asMekanismRecipe(recipe);
    }

    @Override
    public boolean containsRecipeAB(ItemStack inputA, ChemicalStack inputB) {
        if (inputA.isEmpty()) {
            return MaterialMutationRecipeHandler.containsMachineRecipeChemical(inputB);
        } else if (inputB.isEmpty()) {
            return true;
        }
        return MaterialMutationRecipeHandler.findFirstMachineRecipe(inputA, inputB) != null;
    }

    @Override
    public boolean containsRecipeBA(ItemStack inputA, ChemicalStack inputB) {
        if (inputB.isEmpty()) {
            return MaterialMutationRecipeHandler.containsMachineRecipeInput(inputA);
        } else if (inputA.isEmpty()) {
            return true;
        }
        return MaterialMutationRecipeHandler.findFirstMachineRecipe(inputA, inputB) != null;
    }

    @Override
    public boolean containsRecipeA(ItemStack input) {
        return MaterialMutationRecipeHandler.containsMachineRecipeInput(input);
    }

    @Override
    public boolean containsRecipeB(ChemicalStack input) {
        return MaterialMutationRecipeHandler.containsMachineRecipeChemical(input);
    }

    @Override
    public CachedRecipe<ItemStackChemicalToItemStackRecipe> createNewCachedRecipe(ItemStackChemicalToItemStackRecipe recipe, int cacheIndex) {
        CachedRecipe<ItemStackChemicalToItemStackRecipe> cachedRecipe = super.createNewCachedRecipe(recipe, cacheIndex);
        if (recipe instanceof TimedInjectingRecipe timedRecipe) {
            cachedRecipe.setRequiredTicks(() -> getScaledRecipeTime(timedRecipe.processingTime()));
        }
        return cachedRecipe;
    }

    public static List<ItemStackChemicalToItemStackRecipe> getAllMutationMachineRecipes(Level level) {
        return MaterialMutationRecipeHandler.getAllMachineRecipes().stream()
                .map(MaterialMutatorBlockEntity::asMekanismRecipe)
                .map(recipe -> (ItemStackChemicalToItemStackRecipe) recipe)
                .toList();
    }

    public static List<RecipeHolder<ItemStackChemicalToItemStackRecipe>> getAllMutationMachineRecipeHolders(Level level) {
        return MaterialMutationRecipeHandler.getAllMachineRecipes().stream()
                .map(recipe -> new RecipeHolder<ItemStackChemicalToItemStackRecipe>(recipe.id(), asMekanismRecipe(recipe)))
                .toList();
    }

    private static TimedInjectingRecipe asMekanismRecipe(MaterialMutationRecipeHandler.MachineRecipe recipe) {
        return new TimedInjectingRecipe(
                IngredientCreatorAccess.item().from(recipe.input(), recipe.inputCount()),
                IngredientCreatorAccess.chemicalStack().from(MaterialMutationRecipeHandler.getPsionicEchoChemical(), CHEMICAL_PER_CRAFT),
                recipe.output(),
                false,
                recipe.time()
        );
    }

    private int getScaledRecipeTime(int recipeTime) {
        long base = Math.max(1L, baseTicksRequired);
        long current = Math.max(1L, ticksRequired);
        long scaled = recipeTime * current;
        long roundedUp = (scaled + base - 1L) / base;
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, roundedUp));
    }

    private static class TimedInjectingRecipe extends BasicInjectingRecipe {
        private final int processingTime;

        private TimedInjectingRecipe(mekanism.api.recipes.ingredients.ItemStackIngredient itemInput,
                                     mekanism.api.recipes.ingredients.ChemicalStackIngredient chemicalInput,
                                     ItemStack output,
                                     boolean perTickUsage,
                                     int processingTime) {
            super(itemInput, chemicalInput, output, perTickUsage);
            this.processingTime = processingTime;
        }

        private int processingTime() {
            return processingTime;
        }
    }
}
