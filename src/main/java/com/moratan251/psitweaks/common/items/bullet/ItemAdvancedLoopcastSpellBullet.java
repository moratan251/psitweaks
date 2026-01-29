package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemLoopcastSpellBullet;
import vazkii.psi.common.item.ItemMineSpellBullet;


public class ItemAdvancedLoopcastSpellBullet extends ItemLoopcastSpellBullet {
    public ItemAdvancedLoopcastSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.85;
    }
}
