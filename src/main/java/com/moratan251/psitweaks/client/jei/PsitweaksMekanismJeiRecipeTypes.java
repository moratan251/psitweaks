package com.moratan251.psitweaks.client.jei;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.RVRecipeTypeWrapper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import net.minecraft.world.item.crafting.SingleRecipeInput;

public final class PsitweaksMekanismJeiRecipeTypes {
    public static final RVRecipeTypeWrapper<SingleRecipeInput, ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> SCULK_ERODER =
            new RVRecipeTypeWrapper<>(
                    Psitweaks.location("sculk_eroder"),
                    PsitweaksMekanismBlocks.SCULK_ERODER.get(),
                    ItemStackToItemStackRecipe.class,
                    MekanismRecipeType.ENRICHING,
                    -28,
                    -16,
                    144,
                    54,
                    java.util.List.of(PsitweaksMekanismBlocks.SCULK_ERODER.get())
            );
    public static final RVRecipeTypeWrapper<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, InputRecipeCache.ItemChemical<ItemStackChemicalToItemStackRecipe>> MATERIAL_MUTATOR =
            new RVRecipeTypeWrapper<>(
                    Psitweaks.location("material_mutator"),
                    PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get(),
                    ItemStackChemicalToItemStackRecipe.class,
                    MekanismRecipeType.INJECTING,
                    -28,
                    -16,
                    144,
                    54,
                    java.util.List.of(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get())
            );

    private PsitweaksMekanismJeiRecipeTypes() {
    }
}
