package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemGrenadeSpellBullet;

public class ItemTranscendentGrenadeSpellBullet extends ItemGrenadeSpellBullet {
    public ItemTranscendentGrenadeSpellBullet(Properties properties) {
        super(properties);
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return 0.21;
    }
}
