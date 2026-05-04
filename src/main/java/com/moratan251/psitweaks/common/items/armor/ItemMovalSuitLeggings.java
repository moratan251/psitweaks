package com.moratan251.psitweaks.common.items.armor;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemMovalSuitLeggings extends ItemMovalSuitArmor {
    public ItemMovalSuitLeggings(ArmorItem.Type type, Item.Properties properties) {
        super(type, properties);
    }

    @Override
    public String getEvent(ItemStack stack) {
        return "psi.event.tick";
    }

    @Override
    public int getCastCooldown(ItemStack stack) {
        return 0;
    }

    @Override
    public float getCastVolume() {
        return 0.0F;
    }
}
