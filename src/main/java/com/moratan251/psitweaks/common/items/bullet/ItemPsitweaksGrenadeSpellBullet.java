package com.moratan251.psitweaks.common.items.bullet;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.common.item.ItemGrenadeSpellBullet;

public class ItemPsitweaksGrenadeSpellBullet extends ItemGrenadeSpellBullet {
    private final SpellBulletTier tier;

    public ItemPsitweaksGrenadeSpellBullet(Item.Properties properties, SpellBulletTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public double getCostModifier(ItemStack stack) {
        return switch (tier) {
            case ADVANCED -> 0.89;
            case RESONANT -> 0.735;
            case SUBLIMATED -> 0.525;
            case AWAKENED -> 0.3675;
            case TRANSCENDENT -> 0.21;
        };
    }
}
