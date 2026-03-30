package com.moratan251.psitweaks.common.content.network.transmitter;

import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.tile.transmitter.TileEntityTransmitter;

public class TranscendentCable extends UniversalCable {

    public static final long CAPACITY_LONG = 1_048_576_000L;
    public static final FloatingLong CAPACITY = FloatingLong.createConst(CAPACITY_LONG);

    public TranscendentCable(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(blockProvider, tile);
    }

    @Override
    public FloatingLong getCapacityAsFloatingLong() {
        return CAPACITY;
    }

    @Override
    public long getCapacity() {
        return CAPACITY_LONG;
    }
}
