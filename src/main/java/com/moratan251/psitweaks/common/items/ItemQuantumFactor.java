package com.moratan251.psitweaks.common.items;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Quantum Factor turns into a Graviton Factor in mid-air after falling
 * {@value #FALL_DISTANCE_THRESHOLD} blocks as a dropped item.
 * <p>
 * Backport of the 1.21.1 fall conversion. Uses Forge's onEntityItemUpdate hook
 * instead of the 1.21.1 EntityTickEvent + data attachment approach, so only
 * dropped Quantum Factors pay any per-tick cost.
 */
public class ItemQuantumFactor extends Item {
    private static final float FALL_DISTANCE_THRESHOLD = 380.0f;

    public ItemQuantumFactor(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide && entity.fallDistance >= FALL_DISTANCE_THRESHOLD) {
            entity.setItem(new ItemStack(PsitweaksItems.GRAVITON_FACTOR.get(), stack.getCount()));
        }
        return false;
    }
}
