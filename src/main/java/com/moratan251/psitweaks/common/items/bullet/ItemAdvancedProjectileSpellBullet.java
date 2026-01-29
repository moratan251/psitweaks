package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemProjectileSpellBullet;


public class ItemAdvancedProjectileSpellBullet extends ItemProjectileSpellBullet {
    public ItemAdvancedProjectileSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.87;
    }
}
