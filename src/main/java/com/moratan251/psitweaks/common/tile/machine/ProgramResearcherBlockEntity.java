package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.menu.ProgramResearcherMenu;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ProgramResearcherBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {

    public static final int INPUT_SLOTS = 9;
    public static final int OUTPUT_SLOT = 9;
    public static final int TOTAL_SLOTS = 10;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_ENERGY = 2;
    public static final int DATA_MAX_ENERGY = 3;
    public static final int DATA_COUNT = 4;

    private static final int MAX_ENERGY = 1000000;
    private static final int MAX_RECEIVE = 1000000;

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot != OUTPUT_SLOT) {
                resetProgress();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot != OUTPUT_SLOT;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot == OUTPUT_SLOT) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    private final InternalEnergyStorage energyStorage = new InternalEnergyStorage(MAX_ENERGY, MAX_RECEIVE);

    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> itemHandler);
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);

    private int progress = 0;
    private int maxProgress = 1;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_MAX_PROGRESS -> maxProgress;
                case DATA_ENERGY -> energyStorage.getEnergyStored();
                case DATA_MAX_ENERGY -> energyStorage.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_PROGRESS -> progress = value;
                case DATA_MAX_PROGRESS -> maxProgress = Math.max(1, value);
                case DATA_ENERGY -> energyStorage.setEnergy(value);
                case DATA_MAX_ENERGY -> {
                }
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ProgramResearcherBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksBlockEntityTypes.PROGRAM_RESEARCHER.get(), pos, state);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
        energyCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> itemHandler);
        energyCapability = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.psitweaks.program_researcher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ProgramResearcherMenu(containerId, playerInventory, this, dataAccess);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public SimpleContainer getDropInventory() {
        SimpleContainer container = new SimpleContainer(TOTAL_SLOTS);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            container.setItem(i, itemHandler.getStackInSlot(i).copy());
        }
        return container;
    }

    public int getComparatorOutput() {
        int filledSlots = 0;
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                filledSlots++;
            }
        }
        return Math.round((filledSlots / (float) TOTAL_SLOTS) * 15);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ProgramResearcherBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        blockEntity.serverTick();
    }

    private void serverTick() {
        if (level == null) {
            return;
        }

        Optional<ProgramResearchRecipe> recipeOptional = getMatchingRecipe();
        if (recipeOptional.isEmpty()) {
            resetProgress();
            return;
        }

        ProgramResearchRecipe recipe = recipeOptional.get();
        int[] consumptionPlan = recipe.createConsumptionPlan(itemHandler);
        if (consumptionPlan == null || !canOutput(recipe.getResultItem(level.registryAccess()))) {
            resetProgress();
            return;
        }

        maxProgress = Math.max(1, recipe.getTime());
        int energyCost = recipe.getEnergyCostForTick(progress);
        if (energyStorage.getEnergyStored() < energyCost) {
            return;
        }

        if (energyCost > 0) {
            energyStorage.consumeEnergy(energyCost);
        }

        progress++;
        setChanged();

        if (progress >= maxProgress) {
            consumeInputs(consumptionPlan);
            craftOutput(recipe.getResultItem(level.registryAccess()));
            progress = 0;
            setChanged();
        }
    }

    private Optional<ProgramResearchRecipe> getMatchingRecipe() {
        if (level == null) {
            return Optional.empty();
        }
        SimpleContainer inventory = new SimpleContainer(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        return level.getRecipeManager().getRecipeFor(PsitweaksRecipeTypes.PROGRAM_RESEARCH.get(), inventory, level);
    }

    private boolean canOutput(ItemStack result) {
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(outputStack, result)) {
            return false;
        }
        return outputStack.getCount() + result.getCount() <= outputStack.getMaxStackSize();
    }

    private void consumeInputs(int[] plan) {
        for (int slot = 0; slot < INPUT_SLOTS && slot < plan.length; slot++) {
            int amount = plan[slot];
            if (amount > 0) {
                itemHandler.extractItem(slot, amount, false);
            }
        }
    }

    private void craftOutput(ItemStack result) {
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result.copy());
            return;
        }
        outputStack.grow(result.getCount());
        itemHandler.setStackInSlot(OUTPUT_SLOT, outputStack);
    }

    private void resetProgress() {
        if (progress != 0 || maxProgress != 1) {
            progress = 0;
            maxProgress = 1;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
        tag.putInt("max_progress", maxProgress);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("inventory"));
        }
        energyStorage.setEnergy(tag.getInt("energy"));
        progress = Math.max(0, tag.getInt("progress"));
        maxProgress = Math.max(1, tag.getInt("max_progress"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private class InternalEnergyStorage extends EnergyStorage {

        protected InternalEnergyStorage(int capacity, int maxReceive) {
            super(capacity, maxReceive, 0);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                setChanged();
            }
            return received;
        }

        public int consumeEnergy(int amount) {
            int consumed = Math.min(amount, this.energy);
            if (consumed > 0) {
                this.energy -= consumed;
                setChanged();
            }
            return consumed;
        }

        public void setEnergy(int amount) {
            this.energy = Math.max(0, Math.min(this.capacity, amount));
        }
    }
}
