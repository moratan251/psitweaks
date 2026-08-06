package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemTunneler extends ArrowItem {
    public ItemTunneler(Item.Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter) {
        return new EntityTunnelerArrow(level, shooter);
    }
}
