package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemMineSpellBullet;
import vazkii.psi.common.item.ItemProjectileSpellBullet;


public class ItemAdvancedMineSpellBullet extends ItemMineSpellBullet {
    public ItemAdvancedMineSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.98;
    }
}
