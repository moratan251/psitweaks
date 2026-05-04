package com.moratan251.psitweaks.common.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPhilosophersStone extends Item {
    public ItemPhilosophersStone(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copyWithCount(1);
    }
}
