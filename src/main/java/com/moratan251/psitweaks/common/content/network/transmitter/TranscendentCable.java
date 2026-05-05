package com.moratan251.psitweaks.common.content.network.transmitter;

import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

public class TranscendentCable extends UniversalCable {
    public static final long CAPACITY = 1_048_576_000L;

    public TranscendentCable(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        super(blockProvider, tile);
    }

    @Override
    public long getCapacity() {
        return CAPACITY;
    }
}
