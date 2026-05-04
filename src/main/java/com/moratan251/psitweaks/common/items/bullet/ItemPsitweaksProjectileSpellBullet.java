package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemProjectileSpellBullet;

public class ItemPsitweaksProjectileSpellBullet extends ItemProjectileSpellBullet {
    private final SpellBulletTier tier;

    public ItemPsitweaksProjectileSpellBullet(Item.Properties properties, SpellBulletTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return switch (tier) {
            case ADVANCED -> 0.87;
            case RESONANT -> 0.714;
            case SUBLIMATED -> 0.51;
            case AWAKENED -> 0.357;
            case TRANSCENDENT -> 0.204;
        };
    }
}
