package com.moratan251.psitweaks.common.compat;

import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Chemical reads of the block resource operators. Loaded only when Mekanism is present. */
public final class BlockResourceMekanism {
    private BlockResourceMekanism() { }

    /** mB of the matching chemicals in every tank the side exposes. */
    public static long amount(Level level, BlockPos pos, @Nullable Direction side, Predicate<ResourceLocation> filter) {
        IChemicalHandler handler = handler(level, pos, side);
        if (handler == null) return 0;
        long total = 0;
        for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
            ChemicalStack stack = handler.getChemicalInTank(tank);
            if (stack.isEmpty()) continue;
            ResourceLocation id = MekanismAPI.CHEMICAL_REGISTRY.getKey(stack.getChemical());
            if (id != null && filter.test(id)) {
                total = stack.getAmount() > Long.MAX_VALUE - total ? Long.MAX_VALUE : total + stack.getAmount();
            }
        }
        return total;
    }

    /**
     * mB of this chemical the side would accept now, from a single simulated insertion; unknown IDs have none. Bulk
     * insertion offers the rest to every empty tank, so a pool whose tanks share one storage is asked through its last
     * tank only (the Ideaspace Connector's input tank).
     */
    public static long freeCapacity(Level level, BlockPos pos, @Nullable Direction side, ResourceLocation id, boolean pooled) {
        IChemicalHandler handler = handler(level, pos, side);
        ChemicalStack offered = ConnectorMekanism.stack(id, Long.MAX_VALUE);
        if (handler == null || offered.isEmpty() || handler.getChemicalTanks() <= 0) return 0;
        ChemicalStack rest = pooled ? handler.insertChemical(handler.getChemicalTanks() - 1, offered, Action.SIMULATE)
                : handler.insertChemical(offered, Action.SIMULATE);
        return Long.MAX_VALUE - (rest.isEmpty() ? 0 : rest.getAmount());
    }

    /** Same lookup as the other resources: without a side, the side-less handler or else the first face with one. */
    @Nullable
    private static IChemicalHandler handler(Level level, BlockPos pos, @Nullable Direction side) {
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapabilityIfLoaded(level, pos, side);
        if (handler != null || side != null) return handler;
        for (Direction face : Direction.values()) {
            handler = Capabilities.CHEMICAL.getCapabilityIfLoaded(level, pos, face);
            if (handler != null) return handler;
        }
        return null;
    }
}
