package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemChargeSpellBullet;

public class ItemAwakenedChargeSpellBullet extends ItemChargeSpellBullet {
    public ItemAwakenedChargeSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.4025;
    }
}
