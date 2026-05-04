package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemLoopcastSpellBullet;

public class ItemPsitweaksLoopcastSpellBullet extends ItemLoopcastSpellBullet {
    private final SpellBulletTier tier;

    public ItemPsitweaksLoopcastSpellBullet(Item.Properties properties, SpellBulletTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return switch (tier) {
            case ADVANCED -> 0.85;
            case RESONANT -> 0.7;
            case SUBLIMATED -> 0.5;
            case AWAKENED -> 0.35;
            case TRANSCENDENT -> 0.2;
        };
    }
}
