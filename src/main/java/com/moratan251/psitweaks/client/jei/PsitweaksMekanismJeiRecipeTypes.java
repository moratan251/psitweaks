package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.client.jei.MekanismJEIRecipeType;

public class PsitweaksMekanismJeiRecipeTypes {

    public static final MekanismJEIRecipeType<ItemStackToItemStackRecipe> SCULK_ERODER =
            new MekanismJEIRecipeType<>(PsitweaksMekanismBlocks.SCULK_ERODER, ItemStackToItemStackRecipe.class);
    public static final MekanismJEIRecipeType<ItemStackGasToItemStackRecipe> MATERIAL_MUTATOR =
            new MekanismJEIRecipeType<>(PsitweaksMekanismBlocks.MATERIAL_MUTATOR, ItemStackGasToItemStackRecipe.class);

    private PsitweaksMekanismJeiRecipeTypes() {
    }

    public static void ensureLoaded() {
    }
}
