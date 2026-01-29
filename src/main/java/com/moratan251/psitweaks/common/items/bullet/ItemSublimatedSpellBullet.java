package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemSpellBullet;

public class ItemSublimatedSpellBullet extends ItemSpellBullet {
    public ItemSublimatedSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.50;
    }
}
