package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemMineSpellBullet;

public class ItemTranscendentMineSpellBullet extends ItemMineSpellBullet {
    public ItemTranscendentMineSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.23;
    }
}
