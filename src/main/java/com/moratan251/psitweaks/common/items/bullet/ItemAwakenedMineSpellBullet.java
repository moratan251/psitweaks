package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemMineSpellBullet;

public class ItemAwakenedMineSpellBullet extends ItemMineSpellBullet {
    public ItemAwakenedMineSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.4025;
    }
}
