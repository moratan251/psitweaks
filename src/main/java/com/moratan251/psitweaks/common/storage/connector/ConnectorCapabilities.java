package com.moratan251.psitweaks.common.storage.connector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
public final class ConnectorCapabilities {
    private ConnectorCapabilities() { }
    public static <T> T get(Level level, BlockPos pos, Direction side, Capability<T> capability) {
        if (!level.hasChunkAt(pos)) return null;
        var block = level.getBlockEntity(pos);
        return block == null ? null : block.getCapability(capability, side).orElse(null);
    }
}
