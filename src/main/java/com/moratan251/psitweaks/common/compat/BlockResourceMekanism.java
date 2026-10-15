package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.storage.connector.ConnectorCapabilities;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * Chemical reads of the block resource operators. Loaded only when Mekanism is present. Mekanism 10 keeps gases,
 * infuse types, pigments and slurries in separate handlers; the filter sees the plain registry ID shown to players.
 */
public final class BlockResourceMekanism {
    private BlockResourceMekanism() { }

    /**
     * mB of the matching chemicals in every tank of every kind the side exposes; empty when the side has no chemical
     * handler of any kind.
     */
    public static OptionalLong amount(Level level, BlockPos pos, @Nullable Direction side, Predicate<ResourceLocation> filter) {
        boolean found = false;
        long total = 0;
        for (IChemicalHandler<?, ?> handler : handlers(level, pos, side)) {
            if (handler == null) continue;
            found = true;
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                ChemicalStack<?> stack = handler.getChemicalInTank(tank);
                if (!stack.isEmpty() && filter.test(stack.getTypeRegistryName())) {
                    total = stack.getAmount() > Long.MAX_VALUE - total ? Long.MAX_VALUE : total + stack.getAmount();
                }
            }
        }
        return found ? OptionalLong.of(total) : OptionalLong.empty();
    }

    /**
     * mB of this chemical the side would accept now, from a single simulated insertion; unknown IDs have none, and it is
     * empty when the side has no chemical handler of any kind. A plain ID registered under several kinds is asked as the
     * first of gas, infusion type, pigment and slurry that the side handles. Bulk insertion offers the rest to every
     * empty tank, so a pool whose tanks share one storage is asked through its last tank only (the Ideaspace
     * Connector's input tank).
     */
    public static OptionalLong freeCapacity(Level level, BlockPos pos, @Nullable Direction side, ResourceLocation id,
                                            boolean pooled) {
        var gas = ConnectorCapabilities.get(level, pos, side, Capabilities.GAS_HANDLER);
        var infusion = ConnectorCapabilities.get(level, pos, side, Capabilities.INFUSION_HANDLER);
        var pigment = ConnectorCapabilities.get(level, pos, side, Capabilities.PIGMENT_HANDLER);
        var slurry = ConnectorCapabilities.get(level, pos, side, Capabilities.SLURRY_HANDLER);
        if (gas == null && infusion == null && pigment == null && slurry == null) return OptionalLong.empty();
        OptionalLong free = freeCapacity(gas, MekanismAPI.gasRegistry(), GasStack::new, id, pooled);
        if (free.isEmpty()) free = freeCapacity(infusion, MekanismAPI.infuseTypeRegistry(), InfusionStack::new, id, pooled);
        if (free.isEmpty()) free = freeCapacity(pigment, MekanismAPI.pigmentRegistry(), PigmentStack::new, id, pooled);
        if (free.isEmpty()) free = freeCapacity(slurry, MekanismAPI.slurryRegistry(), SlurryStack::new, id, pooled);
        return OptionalLong.of(free.orElse(0));
    }

    /** Empty when the handler is missing or the ID is not a chemical of its kind. */
    private static <C extends Chemical<C>, S extends ChemicalStack<C>> OptionalLong freeCapacity(
            @Nullable IChemicalHandler<C, S> handler, IForgeRegistry<C> registry, BiFunction<C, Long, S> factory,
            ResourceLocation id, boolean pooled) {
        if (handler == null) return OptionalLong.empty();
        C chemical = registry.getValue(id);
        if (chemical == null || chemical.isEmptyType()) return OptionalLong.empty();
        if (handler.getTanks() <= 0) return OptionalLong.of(0);
        S offered = factory.apply(chemical, Long.MAX_VALUE);
        S rest = pooled ? handler.insertChemical(handler.getTanks() - 1, offered, Action.SIMULATE)
                : handler.insertChemical(offered, Action.SIMULATE);
        return OptionalLong.of(Long.MAX_VALUE - (rest.isEmpty() ? 0 : rest.getAmount()));
    }

    private static IChemicalHandler<?, ?>[] handlers(Level level, BlockPos pos, @Nullable Direction side) {
        return new IChemicalHandler<?, ?>[] {
                ConnectorCapabilities.get(level, pos, side, Capabilities.GAS_HANDLER),
                ConnectorCapabilities.get(level, pos, side, Capabilities.INFUSION_HANDLER),
                ConnectorCapabilities.get(level, pos, side, Capabilities.PIGMENT_HANDLER),
                ConnectorCapabilities.get(level, pos, side, Capabilities.SLURRY_HANDLER)};
    }
}
