package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemCircleSpellBullet;
import vazkii.psi.common.item.ItemMineSpellBullet;


public class ItemAdvancedCircleSpellBullet extends ItemCircleSpellBullet {
    public ItemAdvancedCircleSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 12.75;
    }
}
