package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.tile.machine.TileEntityMaterialMutator;
import com.moratan251.psitweaks.common.tile.machine.TileEntitySculkEroder;
import java.util.List;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.ItemStackGasToItemStackRecipeCategory;
import mekanism.client.jei.machine.ItemStackToItemStackRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.world.level.Level;

final class MekanismJeiIntegration {
    private MekanismJeiIntegration() {
    }

    static void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ItemStackToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER,
                PsitweaksMekanismBlocks.SCULK_ERODER
        ));
        registration.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(
                registration.getJeiHelpers().getGuiHelper(),
                PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR,
                PsitweaksMekanismBlocks.MATERIAL_MUTATOR
        ));
    }

    static void registerRecipes(IRecipeRegistration registration, Level level) {
        RecipeType<ItemStackToItemStackRecipe> sculkType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
        List<ItemStackToItemStackRecipe> sculkRecipes = level == null ? List.of() : TileEntitySculkEroder.getAllSculkRecipes(level);
        registration.addRecipes(sculkType, sculkRecipes);

        RecipeType<ItemStackGasToItemStackRecipe> mutationType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR);
        List<ItemStackGasToItemStackRecipe> mutationRecipes = level == null ? List.of() : TileEntityMaterialMutator.getAllMutationMachineRecipes(level);
        registration.addRecipes(mutationType, mutationRecipes);
    }

    static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        RecipeType<ItemStackToItemStackRecipe> sculkType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.SCULK_ERODER);
        RecipeType<ItemStackGasToItemStackRecipe> mutationType = MekanismJEI.recipeType(PsitweaksMekanismJeiRecipeTypes.MATERIAL_MUTATOR);
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.SCULK_ERODER.getBlock(), sculkType);
        registration.addRecipeCatalyst(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.getBlock(), mutationType);
    }
}
