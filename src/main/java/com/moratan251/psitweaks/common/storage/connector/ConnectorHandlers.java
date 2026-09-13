package com.moratan251.psitweaks.common.storage.connector;

import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/** Stable sided adapters recheck settings/lifecycle on every call, including previously cached handles. */
public final class ConnectorHandlers {
    private final IdeaspaceConnectorBlockEntity connector;
    private final Direction side;
    public ConnectorHandlers(IdeaspaceConnectorBlockEntity connector, Direction side) {
        this.connector = connector;
        this.side = side;
    }

    private boolean output(int slot) { return connector.sideMode(slot, side).output; }
    private boolean itemInput(ItemStack stack) {
        return !stack.isEmpty() && ItemResourceKey.of(stack)
                .map(key -> connector.allowsInput(side, ConnectorResource.item(key))).orElse(false);
    }
    private boolean fluidInput(FluidStack stack) {
        return !stack.isEmpty() && FluidResourceKey.of(stack)
                .map(key -> connector.allowsInput(side, ConnectorResource.fluid(key))).orElse(false);
    }
    private boolean energyInput() { return connector.allowsInput(side, ConnectorResource.ENERGY); }
    private boolean energyOutput() { return output(connector.publishedSlot(ConnectorResource.ENERGY)); }
    private static boolean valid(int slot) { return slot >= 0 && slot < IdeaspaceConnectorBlockEntity.SLOTS; }
    private static int bounded(long amount) { return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, amount)); }

    public final IItemHandler items = new IItemHandler() {
        @Override public int getSlots() { return IdeaspaceConnectorBlockEntity.SLOTS; }
        @Override public ItemStack getStackInSlot(int slot) {
            ConnectorResource resource = connector.resource(slot);
            return output(slot) ? resource.itemStack(bounded(resource.amount(connector.storage()))) : ItemStack.EMPTY;
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            PlayerIdeaStorage storage = connector.storage();
            if (!valid(slot) || !itemInput(stack) || storage == null) return stack;
            long accepted = simulate ? storage.simulateInsert(stack, stack.getCount()) : storage.insert(stack, stack.getCount());
            return stack.copyWithCount(stack.getCount() - (int) accepted);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            PlayerIdeaStorage storage = connector.storage();
            ConnectorResource resource = connector.resource(slot);
            if (!output(slot) || storage == null || resource.item() == null || amount <= 0) return ItemStack.EMPTY;
            int request = Math.min(amount, resource.item().getMaxStackSize());
            long extracted = simulate ? storage.simulateExtract(resource.item(), request) : storage.extract(resource.item(), request);
            return resource.itemStack((int) extracted);
        }
        @Override public int getSlotLimit(int slot) { return valid(slot) ? Integer.MAX_VALUE : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return valid(slot) && itemInput(stack); }
    };

    public final IFluidHandler fluids = new IFluidHandler() {
        @Override public int getTanks() { return IdeaspaceConnectorBlockEntity.SLOTS; }
        @Override public FluidStack getFluidInTank(int tank) {
            ConnectorResource resource = connector.resource(tank);
            return output(tank) ? resource.fluidStack(bounded(resource.amount(connector.storage()))) : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int tank) {
            PlayerIdeaStorage storage = connector.storage();
            return valid(tank) && storage != null ? bounded(storage.maxFluidPerType()) : 0;
        }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return valid(tank) && fluidInput(stack); }
        @Override public int fill(FluidStack stack, FluidAction action) {
            PlayerIdeaStorage storage = connector.storage();
            if (!fluidInput(stack) || storage == null) return 0;
            return (int) (action.simulate() ? storage.simulateInsertFluid(stack, stack.getAmount())
                    : storage.insertFluid(stack, stack.getAmount()));
        }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) {
            if (stack.isEmpty()) return FluidStack.EMPTY;
            FluidResourceKey key = FluidResourceKey.of(stack).orElseThrow();
            for (int i = 0; i < getTanks(); i++) if (key.equals(connector.resource(i).fluid())) return drainSlot(i, stack.getAmount(), action);
            return FluidStack.EMPTY;
        }
        @Override public FluidStack drain(int amount, FluidAction action) {
            for (int i = 0; i < getTanks(); i++) {
                FluidStack result = drainSlot(i, amount, action);
                if (!result.isEmpty()) return result;
            }
            return FluidStack.EMPTY;
        }
        private FluidStack drainSlot(int slot, int amount, FluidAction action) {
            PlayerIdeaStorage storage = connector.storage();
            ConnectorResource resource = connector.resource(slot);
            if (!output(slot) || storage == null || resource.fluid() == null || amount <= 0) return FluidStack.EMPTY;
            long extracted = action.simulate() ? storage.simulateExtractFluid(resource.fluid(), amount)
                    : storage.extractFluid(resource.fluid(), amount);
            return resource.fluidStack((int) extracted);
        }
    };

    public final IEnergyStorage energy = new IEnergyStorage() {
        @Override public int receiveEnergy(int amount, boolean simulate) {
            PlayerIdeaStorage storage = connector.storage();
            return energyInput() && storage != null ? (int) storage.insertEnergy(amount, simulate) : 0;
        }
        @Override public int extractEnergy(int amount, boolean simulate) {
            PlayerIdeaStorage storage = connector.storage();
            return energyOutput() && storage != null
                    ? (int) storage.extractEnergy(amount, simulate) : 0;
        }
        @Override public int getEnergyStored() {
            PlayerIdeaStorage storage = connector.storage();
            return energyOutput() && storage != null ? bounded(storage.extractEnergy(Long.MAX_VALUE, true)) : 0;
        }
        @Override public int getMaxEnergyStored() {
            PlayerIdeaStorage storage = connector.storage();
            return storage == null ? 0 : bounded(storage.maxEnergy());
        }
        @Override public boolean canExtract() { return energyOutput() && connector.storage() != null; }
        @Override public boolean canReceive() { return energyInput() && connector.storage() != null; }
    };
}
