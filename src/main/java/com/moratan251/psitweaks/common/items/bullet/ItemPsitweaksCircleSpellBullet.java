package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemCircleSpellBullet;

public class ItemPsitweaksCircleSpellBullet extends ItemCircleSpellBullet {
    private final SpellBulletTier tier;

    public ItemPsitweaksCircleSpellBullet(Item.Properties properties, SpellBulletTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return switch (tier) {
            case ADVANCED -> 12.75;
            case RESONANT -> 10.5;
            case SUBLIMATED -> 7.5;
            case AWAKENED -> 5.25;
            case TRANSCENDENT -> 3.0;
        };
    }
}
