package com.moratan251.psitweaks.common.tile.machine;

import net.minecraft.world.SimpleContainer;

public interface InventoryDropProvider {
    SimpleContainer getDropInventory();

    int getComparatorOutput();
}
