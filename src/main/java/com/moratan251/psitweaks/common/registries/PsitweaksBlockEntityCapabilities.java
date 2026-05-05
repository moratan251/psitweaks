package com.moratan251.psitweaks.common.registries;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class PsitweaksBlockEntityCapabilities {
    private PsitweaksBlockEntityCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PsitweaksBlockEntityTypes.PROGRAM_RESEARCHER.get(),
                (blockEntity, side) -> blockEntity.getItemHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PsitweaksBlockEntityTypes.PROGRAM_RESEARCHER.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage());
        event.registerBlockEntity(mekanism.common.capabilities.Capabilities.ENERGY.block(), PsitweaksBlockEntityTypes.PROGRAM_RESEARCHER.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage());
    }
}
