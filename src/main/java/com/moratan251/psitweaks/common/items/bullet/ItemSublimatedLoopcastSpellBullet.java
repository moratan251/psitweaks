package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemLoopcastSpellBullet;


public class ItemSublimatedLoopcastSpellBullet extends ItemLoopcastSpellBullet {
    public ItemSublimatedLoopcastSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.50;
    }
}
