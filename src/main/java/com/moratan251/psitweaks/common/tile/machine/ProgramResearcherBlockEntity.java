package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.menu.ProgramResearcherMenu;
import com.moratan251.psitweaks.common.recipe.MachineRecipeInput;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProgramResearcherBlockEntity extends BlockEntity implements MenuProvider, InventoryDropProvider {
    public static final int INPUT_SLOTS = 9;
    public static final int OUTPUT_SLOT = 9;
    public static final int TOTAL_SLOTS = 10;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_ENERGY = 2;
    public static final int DATA_MAX_ENERGY = 3;
    public static final int DATA_COUNT = 4;

    private static final int MAX_ENERGY = 1_000_000;

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
            return slot == OUTPUT_SLOT ? stack : super.insertItem(slot, stack, simulate);
        }
    };
    private final MutableEnergyStorage energyStorage = new MutableEnergyStorage(MAX_ENERGY, MAX_ENERGY, 0, this::setChanged);

    private int progress;
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
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ProgramResearcherBlockEntity(BlockPos pos, BlockState blockState) {
        super(PsitweaksBlockEntityTypes.PROGRAM_RESEARCHER.get(), pos, blockState);
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

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ProgramResearcherBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.serverTick();
        }
    }

    private void serverTick() {
        if (level == null) {
            return;
        }

        Optional<ProgramResearchRecipe> recipeOptional = level.getRecipeManager()
                .getRecipeFor(PsitweaksRecipeTypes.PROGRAM_RESEARCH.get(), MachineRecipeInput.of(itemHandler, INPUT_SLOTS), level)
                .map(holder -> holder.value());
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

    private boolean canOutput(ItemStack result) {
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(outputStack, result)) {
            return false;
        }
        return outputStack.getCount() + result.getCount() <= outputStack.getMaxStackSize();
    }

    private void consumeInputs(int[] plan) {
        for (int slot = 0; slot < INPUT_SLOTS && slot < plan.length; slot++) {
            if (plan[slot] > 0) {
                itemHandler.extractItem(slot, plan[slot], false);
            }
        }
    }

    private void craftOutput(ItemStack result) {
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            outputStack.grow(result.getCount());
            itemHandler.setStackInSlot(OUTPUT_SLOT, outputStack);
        }
    }

    private void resetProgress() {
        if (progress != 0 || maxProgress != 1) {
            progress = 0;
            maxProgress = 1;
            setChanged();
        }
    }

    @Override
    public SimpleContainer getDropInventory() {
        SimpleContainer container = new SimpleContainer(TOTAL_SLOTS);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            container.setItem(i, itemHandler.getStackInSlot(i).copy());
        }
        return container;
    }

    @Override
    public int getComparatorOutput() {
        int filledSlots = 0;
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                filledSlots++;
            }
        }
        return Math.round((filledSlots / (float) TOTAL_SLOTS) * 15);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("energy", energyStorage.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putInt("max_progress", maxProgress);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(registries, tag.get("energy"));
        }
        progress = Math.max(0, tag.getInt("progress"));
        maxProgress = Math.max(1, tag.getInt("max_progress"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
