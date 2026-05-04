package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemMovalSuitChestplate extends ItemMovalSuitArmor {
    public ItemMovalSuitChestplate(ArmorItem.Type type, Item.Properties properties) {
        super(type, properties);
    }

    @Override
    public String getEvent(ItemStack stack) {
        return "psi.event.damage";
    }
}
