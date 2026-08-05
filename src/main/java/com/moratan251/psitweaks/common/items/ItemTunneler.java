package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemTunneler extends ArrowItem {
    public ItemTunneler(Item.Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        EntityTunnelerArrow arrow = new EntityTunnelerArrow(level, shooter, ammo.copyWithCount(1), weapon);
        if (weapon != null && weapon.getItem() instanceof ItemGravstringer) {
            arrow.setMode(ItemGravstringer.getArrowMode(weapon));
        }
        return arrow;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        EntityTunnelerArrow arrow = new EntityTunnelerArrow(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }
}
