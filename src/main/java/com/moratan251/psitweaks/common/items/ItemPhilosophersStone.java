package com.moratan251.psitweaks.common.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemPhilosophersStone extends Item {
    public ItemPhilosophersStone(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean hasCraftingRemainingItem(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull ItemStack getCraftingRemainingItem(@NotNull ItemStack stack) {
        ItemStack remaining = stack.copy();
        remaining.setCount(1);
        return remaining;
    }
}
