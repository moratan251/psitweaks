package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemChargeSpellBullet;
import vazkii.psi.common.item.ItemMineSpellBullet;


public class ItemAdvancedChargeSpellBullet extends ItemChargeSpellBullet {
    public ItemAdvancedChargeSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.98;
    }
}
