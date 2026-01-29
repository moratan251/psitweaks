package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemSpellBullet;

public class ItemAwakenedSpellBullet extends ItemSpellBullet {
    public ItemAwakenedSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.35;
    }
}
