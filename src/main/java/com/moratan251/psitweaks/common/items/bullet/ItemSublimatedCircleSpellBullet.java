package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemCircleSpellBullet;


public class ItemSublimatedCircleSpellBullet extends ItemCircleSpellBullet {
    public ItemSublimatedCircleSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 7.5;
    }
}
