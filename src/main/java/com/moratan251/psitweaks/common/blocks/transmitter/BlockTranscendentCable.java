package com.moratan251.psitweaks.common.blocks.transmitter;

import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlockTypes;
import com.moratan251.psitweaks.common.tile.transmitter.TileEntityTranscendentCable;
import mekanism.common.block.transmitter.BlockSmallTransmitter;

public class BlockTranscendentCable extends BlockSmallTransmitter<TileEntityTranscendentCable> {
    public BlockTranscendentCable() {
        super(PsitweaksMekanismBlockTypes.TRANSCENDENT_CABLE);
    }
}
