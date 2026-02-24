package com.moratan251.psitweaks.client.jei;

import net.minecraft.world.item.ItemStack;

public record MaterialMutationJeiRecipe(ItemStack input, ItemStack output) {

    public MaterialMutationJeiRecipe {
        input = input.copy();
        output = output.copy();
    }
}
