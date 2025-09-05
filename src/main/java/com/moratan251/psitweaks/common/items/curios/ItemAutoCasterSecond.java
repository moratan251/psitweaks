package com.moratan251.psitweaks.common.items.curios;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemAutoCasterSecond extends ItemCuriosCompat{
    public ItemAutoCasterSecond(Properties pProperties) {
        super(pProperties.stacksTo(1).rarity(Rarity.RARE));
    }

    public String getEvent(ItemStack stack) {
        return "psitweaks.event.second";
    }
}
