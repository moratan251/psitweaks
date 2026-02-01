package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemGrenadeSpellBullet;


public class ItemSublimatedGrenadeSpellBullet extends ItemGrenadeSpellBullet {
    public ItemSublimatedGrenadeSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.525;
    }
}
