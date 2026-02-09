package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemProjectileSpellBullet;

public class ItemAwakenedProjectileSpellBullet extends ItemProjectileSpellBullet {
    public ItemAwakenedProjectileSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.357;
    }
}
