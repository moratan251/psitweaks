package com.moratan251.psitweaks.common.spells.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record BlockInventorySlotSource(ResourceKey<Level> dimension, BlockPos pos, int slot) implements ItemSourceRef {
}
