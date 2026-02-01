package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemCircleSpellBullet;


public class ItemResonantCircleSpellBullet extends ItemCircleSpellBullet {
    public ItemResonantCircleSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 10.50;
    }
}
