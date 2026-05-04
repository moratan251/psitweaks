package com.moratan251.psitweaks.common.items.curios;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemAutoCasterTick extends ItemCuriosCompat {
    public ItemAutoCasterTick(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public String getEvent(ItemStack stack) {
        return "psi.event.tick";
    }
}
