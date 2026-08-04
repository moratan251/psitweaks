package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class EntityTunnelerArrow extends AbstractArrow {
    public EntityTunnelerArrow(EntityType<? extends EntityTunnelerArrow> entityType, Level level) {
        super(entityType, level);
    }

    public EntityTunnelerArrow(Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(PsitweaksEntities.TUNNELER_ARROW.get(), owner, level, pickupItemStack, firedFromWeapon);
    }

    public EntityTunnelerArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(PsitweaksEntities.TUNNELER_ARROW.get(), x, y, z, level, pickupItemStack, firedFromWeapon);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(PsitweaksItems.TUNNELER.get());
    }
}
