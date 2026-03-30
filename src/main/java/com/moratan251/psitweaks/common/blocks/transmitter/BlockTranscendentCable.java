package com.moratan251.psitweaks.common.blocks.transmitter;

import com.moratan251.psitweaks.common.registries.PsitweaksMekanismTileEntityTypes;
import mekanism.common.block.transmitter.BlockUniversalCable;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.CableTier;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;

public class BlockTranscendentCable extends BlockUniversalCable {

    public BlockTranscendentCable() {
        super(CableTier.ULTIMATE);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public TileEntityTypeRegistryObject<TileEntityUniversalCable> getTileType() {
        return (TileEntityTypeRegistryObject) PsitweaksMekanismTileEntityTypes.TRANSCENDENT_CABLE;
    }
}
