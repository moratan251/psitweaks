package com.moratan251.psitweaks.common.storage.connector;

import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.storage.idea.*;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public final class ConnectorTransfers {
    private ConnectorTransfers() { }

    public static boolean push(IdeaspaceConnectorBlockEntity source, PlayerIdeaStorage storage, Direction direction) {
        return push(source, storage, direction, (1 << IdeaspaceConnectorBlockEntity.SLOTS) - 1);
    }

    public static boolean push(IdeaspaceConnectorBlockEntity source, PlayerIdeaStorage storage, Direction direction, int dueSlots) {
        var level = source.getLevel();
        BlockPos targetPos = source.getBlockPos().relative(direction);
        if (level == null || !level.hasChunkAt(targetPos)) return false;
        if (level.getBlockEntity(targetPos) instanceof IdeaspaceConnectorBlockEntity target
                && source.owner().equals(target.owner())) return false;
        Direction face = direction.getOpposite();
        boolean moved = false;
        IItemHandler items = null;
        IFluidHandler fluids = null;
        IEnergyStorage energy = null;
        boolean checkedItems = false, checkedFluids = false, checkedEnergy = false;
        for (int slot = 0; slot < IdeaspaceConnectorBlockEntity.SLOTS; slot++) {
            if ((dueSlots & (1 << slot)) == 0) continue;
            if (!source.sideMode(slot, direction).output || !source.automatic(slot, direction)) continue;
            ConnectorResource resource = source.resource(slot);
            if (resource.amount(storage) <= 0) continue;
            int amount = source.exportSettings(slot, ConnectorExportSettings.type(resource.kind())).amount();
            switch (resource.kind()) {
                case ITEM -> {
                    if (!checkedItems) {
                        items = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, face);
                        checkedItems = true;
                    }
                    if (items != null) moved |= pushItem(storage, resource.item(), items, amount) > 0;
                }
                case FLUID -> {
                    if (!checkedFluids) {
                        fluids = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, face);
                        checkedFluids = true;
                    }
                    if (fluids != null) moved |= pushFluid(storage, resource.fluid(), fluids, amount) > 0;
                }
                case ENERGY -> {
                    if (!checkedEnergy) {
                        energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, face);
                        checkedEnergy = true;
                    }
                    if (energy != null) moved |= pushEnergy(storage, energy, amount) > 0;
                }
                case CHEMICAL -> {
                    if (MekanismCompat.isMekanismLoaded())
                        moved |= ConnectorMekanism.push(storage, resource.chemical(), level, targetPos, face, amount) > 0;
                }
                default -> { }
            }
        }
        return moved;
    }

    public static int pushItem(PlayerIdeaStorage storage, ItemResourceKey key, IItemHandler target, int maximum) {
        int available = (int) storage.simulateExtract(key, Math.min(maximum, key.getMaxStackSize()));
        if (available <= 0) return 0;
        ItemStack offered = key.template().copyWithCount(available);
        int planned = available - ItemHandlerHelper.insertItemStacked(target, offered, true).getCount();
        var withdrawal = storage.withdrawItem(key, Math.max(0, planned));
        int accepted = 0;
        try {
            if (withdrawal.amount() > 0) {
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(target,
                        key.template().copyWithCount((int) withdrawal.amount()), false);
                accepted = (int) Math.max(0, Math.min(withdrawal.amount(), withdrawal.amount() - remainder.getCount()));
            }
            return accepted;
        } finally {
            withdrawal.restore(withdrawal.amount() - accepted);
        }
    }

    public static int pushFluid(PlayerIdeaStorage storage, FluidResourceKey key, IFluidHandler target, int maximum) {
        int available = (int) storage.simulateExtractFluid(key, maximum);
        if (available <= 0) return 0;
        FluidStack offered = key.template().copyWithAmount(available);
        int planned = Math.max(0, Math.min(available, target.fill(offered, IFluidHandler.FluidAction.SIMULATE)));
        var withdrawal = storage.withdrawFluid(key, planned);
        int accepted = 0;
        try {
            if (withdrawal.amount() > 0) accepted = (int) Math.max(0, Math.min(withdrawal.amount(), target.fill(
                    key.template().copyWithAmount((int) withdrawal.amount()), IFluidHandler.FluidAction.EXECUTE)));
            return accepted;
        } finally {
            withdrawal.restore(withdrawal.amount() - accepted);
        }
    }

    public static int pushEnergy(PlayerIdeaStorage storage, IEnergyStorage target, int maximum) {
        return IdeaStorageEnergyTransfer.supply(storage, target, maximum);
    }
}
