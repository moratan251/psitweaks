package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemChargeSpellBullet;

public class ItemPsitweaksChargeSpellBullet extends ItemChargeSpellBullet {
    private final SpellBulletTier tier;

    public ItemPsitweaksChargeSpellBullet(Item.Properties properties, SpellBulletTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return switch (tier) {
            case ADVANCED -> 0.98;
            case RESONANT -> 0.805;
            case SUBLIMATED -> 0.575;
            case AWAKENED -> 0.4025;
            case TRANSCENDENT -> 0.23;
        };
    }
}
