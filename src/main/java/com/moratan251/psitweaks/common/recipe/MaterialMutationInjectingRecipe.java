package com.moratan251.psitweaks.common.recipe;

import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.impl.InjectingIRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MaterialMutationInjectingRecipe extends InjectingIRecipe {

    private final int processingTime;

    public MaterialMutationInjectingRecipe(ResourceLocation id,
                                           ItemStackIngredient itemInput,
                                           ChemicalStackIngredient.GasStackIngredient gasInput,
                                           ItemStack output,
                                           int processingTime) {
        super(id, itemInput, gasInput, output);
        this.processingTime = Math.max(1, processingTime);
    }

    public int getProcessingTime() {
        return processingTime;
    }
}
