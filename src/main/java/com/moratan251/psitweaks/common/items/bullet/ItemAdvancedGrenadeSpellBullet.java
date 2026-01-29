package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemGrenadeSpellBullet;
import vazkii.psi.common.item.ItemMineSpellBullet;


public class ItemAdvancedGrenadeSpellBullet extends ItemGrenadeSpellBullet {
    public ItemAdvancedGrenadeSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.89;
    }
}
