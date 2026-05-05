package com.moratan251.psitweaks.common.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.items.IItemHandler;

public class MachineRecipeInput implements RecipeInput {
    private final List<ItemStack> stacks;

    private MachineRecipeInput(List<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public static MachineRecipeInput of(IItemHandler itemHandler, int slotCount) {
        ItemStack[] stacks = new ItemStack[slotCount];
        for (int i = 0; i < slotCount; i++) {
            stacks[i] = itemHandler.getStackInSlot(i);
        }
        return new MachineRecipeInput(List.of(stacks));
    }

    @Override
    public ItemStack getItem(int index) {
        return stacks.get(index);
    }

    @Override
    public int size() {
        return stacks.size();
    }
}
