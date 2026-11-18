package com.moratan251.psitweaks.common.registries;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class PsitweaksBlockEntityCapabilities {
    private PsitweaksBlockEntityCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        var type = PsitweaksBlockEntityTypes.IDEASPACE_CONNECTOR.get();
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, type,
                (connector, side) -> side == null ? null : connector.handlers(side).items);
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, type,
                (connector, side) -> side == null ? null : connector.handlers(side).fluids);
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, type,
                (connector, side) -> side == null ? null : connector.handlers(side).energy);
        // 領域給電機: どの面からも（面のない問い合わせでも）受け取りと取り出しができる FE の窓口。
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                PsitweaksBlockEntityTypes.ZONE_FEEDER.get(), (feeder, side) -> feeder.energyStorage());
        if (com.moratan251.psitweaks.common.compat.MekanismCompat.isMekanismLoaded())
            com.moratan251.psitweaks.common.compat.ConnectorMekanism.register(event);
    }
}
