package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.recipe.MachineRecipeInput;
import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class ProgramResearcherBlockEntity extends TileEntityConfigurableMachine {
    public static final int INPUT_SLOTS = 9;
    public static final int OUTPUT_SLOT = 9;
    public static final int TOTAL_SLOTS = 10;

    private static final String NBT_PROGRESS = "progress";
    private static final String NBT_MAX_PROGRESS = "max_progress";
    private static final String NBT_LEGACY_INVENTORY = "inventory";
    private static final String NBT_LEGACY_ENERGY = "energy";

    private static final int INPUT_START_X = 38;
    private static final int INPUT_START_Y = 17;
    private static final int OUTPUT_X = 134;
    private static final int OUTPUT_Y = 35;
    private static final int ENERGY_SLOT_X = 8;
    private static final int ENERGY_SLOT_Y = 53;

    private List<InputInventorySlot> inputSlots;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;
    private MachineEnergyContainer<ProgramResearcherBlockEntity> energyContainer;

    private int progress;
    private int maxProgress = 1;
    private long currentEnergyCost;
    @Nullable
    private ProgramResearchRecipe activeRecipe;
    private List<ItemStack> activeInputSnapshot = List.of();

    public ProgramResearcherBlockEntity(BlockPos pos, BlockState blockState) {
        super(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER, pos, blockState);

        List<IInventorySlot> inputs = new ArrayList<>(inputSlots);
        List<IInventorySlot> outputs = new ArrayList<>();
        outputs.add(outputSlot);
        configComponent.setupItemIOConfig(inputs, outputs, energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        ejectorComponent = new TileComponentEjector(this)
                .setOutputData(configComponent, TransmissionType.ITEM);
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        energyContainer = MachineEnergyContainer.input(this, listener);
        builder.addContainer(energyContainer);
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);

        List<InputInventorySlot> inputs = new ArrayList<>(INPUT_SLOTS);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            int x = INPUT_START_X + (slot % 3) * 18;
            int y = INPUT_START_Y + (slot / 3) * 18;
            inputs.add(builder.addSlot(InputInventorySlot.at(listener, x, y)));
        }
        inputSlots = List.copyOf(inputs);

        outputSlot = builder.addSlot(OutputInventorySlot.at(listener, OUTPUT_X, OUTPUT_Y));
        energySlot = builder.addSlot(EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdate = super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        if (!canFunction()) {
            currentEnergyCost = 0L;
            return sendUpdate || setActiveState(false);
        }

        ProgramResearchRecipe recipe = findRecipe();
        if (recipe == null) {
            currentEnergyCost = 0L;
            boolean changed = resetProgress();
            changed |= setActiveState(false);
            return sendUpdate || changed;
        }

        int[] consumptionPlan = recipe.createConsumptionPlan(inputSlots);
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (consumptionPlan == null || !canOutput(result)) {
            currentEnergyCost = 0L;
            boolean changed = resetProgress();
            changed |= setActiveState(false);
            return sendUpdate || changed;
        }

        boolean changed = validateActiveInputs(recipe);
        maxProgress = Math.max(1, recipe.getTime());
        long energyCost = recipe.getEnergyPerTick();
        currentEnergyCost = energyCost;
        if (energyContainer.getEnergy() < energyCost) {
            return sendUpdate || setActiveState(false);
        }

        if (energyCost > 0) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.INTERNAL);
        }

        progress++;
        changed |= setActiveState(true);
        setChanged();

        if (progress >= maxProgress) {
            consumeInputs(consumptionPlan);
            craftOutput(result);
            progress = 0;
            clearActiveInputs();
            changed = true;
            setChanged();
        }

        return sendUpdate || changed;
    }

    @Nullable
    private ProgramResearchRecipe findRecipe() {
        if (level == null) {
            return null;
        }
        return level.getRecipeManager()
                .getRecipeFor(PsitweaksRecipeTypes.PROGRAM_RESEARCH.get(), MachineRecipeInput.of(inputSlots), level)
                .map(holder -> holder.value())
                .orElse(null);
    }

    private boolean validateActiveInputs(ProgramResearchRecipe recipe) {
        if (progress <= 0 || activeRecipe == null) {
            captureActiveInputs(recipe);
            return false;
        }
        if (recipe == activeRecipe && activeInputSnapshotMatches()) {
            return false;
        }
        boolean changed = resetProgress();
        captureActiveInputs(recipe);
        return changed;
    }

    private void captureActiveInputs(ProgramResearchRecipe recipe) {
        activeRecipe = recipe;
        List<ItemStack> snapshot = new ArrayList<>(inputSlots.size());
        for (InputInventorySlot inputSlot : inputSlots) {
            snapshot.add(inputSlot.getStack().copy());
        }
        activeInputSnapshot = List.copyOf(snapshot);
    }

    private boolean activeInputSnapshotMatches() {
        if (activeInputSnapshot.size() != inputSlots.size()) {
            return false;
        }
        for (int slot = 0; slot < inputSlots.size(); slot++) {
            ItemStack current = inputSlots.get(slot).getStack();
            ItemStack snapshot = activeInputSnapshot.get(slot);
            if (current.isEmpty() && snapshot.isEmpty()) {
                continue;
            }
            if (current.getCount() != snapshot.getCount()
                    || !ItemStack.isSameItemSameComponents(current, snapshot)) {
                return false;
            }
        }
        return true;
    }

    private void clearActiveInputs() {
        activeRecipe = null;
        activeInputSnapshot = List.of();
    }

    private boolean canOutput(ItemStack result) {
        ItemStack outputStack = outputSlot.getStack();
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
                inputSlots.get(slot).extractItem(plan[slot], Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
    }

    private void craftOutput(ItemStack result) {
        ItemStack outputStack = outputSlot.getStack();
        if (outputStack.isEmpty()) {
            outputSlot.setStack(result.copy());
        } else {
            ItemStack updated = outputStack.copy();
            updated.grow(result.getCount());
            outputSlot.setStack(updated);
        }
    }

    private boolean resetProgress() {
        if (progress == 0 && maxProgress == 1 && activeRecipe == null && activeInputSnapshot.isEmpty()) {
            return false;
        }
        progress = 0;
        maxProgress = 1;
        clearActiveInputs();
        setChanged();
        return true;
    }

    private boolean setActiveState(boolean active) {
        boolean changed = getActive() != active;
        setActive(active);
        return changed;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> progress, value -> progress = Math.max(0, value)));
        container.track(SyncableInt.create(() -> maxProgress, value -> maxProgress = Math.max(1, value)));
        container.track(SyncableLong.create(() -> currentEnergyCost, value -> currentEnergyCost = Math.max(0L, value)));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(NBT_PROGRESS, progress);
        tag.putInt(NBT_MAX_PROGRESS, maxProgress);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        migrateLegacyInventory(tag, registries);
        migrateLegacyEnergy(tag);
        progress = Math.max(0, tag.getInt(NBT_PROGRESS));
        maxProgress = Math.max(1, tag.getInt(NBT_MAX_PROGRESS));
    }

    public double getScaledProgress() {
        return progress <= 0 || maxProgress <= 0 ? 0.0D : progress / (double) maxProgress;
    }

    public MachineEnergyContainer<ProgramResearcherBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    public long getCurrentEnergyCost() {
        return currentEnergyCost;
    }

    private void migrateLegacyInventory(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.contains(NBT_LEGACY_INVENTORY)) {
            return;
        }
        ItemStackHandler legacyInventory = new ItemStackHandler(TOTAL_SLOTS);
        legacyInventory.deserializeNBT(registries, tag.getCompound(NBT_LEGACY_INVENTORY));
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            inputSlots.get(slot).setStack(legacyInventory.getStackInSlot(slot).copy());
        }
        outputSlot.setStack(legacyInventory.getStackInSlot(OUTPUT_SLOT).copy());
    }

    private void migrateLegacyEnergy(CompoundTag tag) {
        if (!tag.contains(NBT_LEGACY_ENERGY)) {
            return;
        }
        Tag legacyEnergy = tag.get(NBT_LEGACY_ENERGY);
        if (legacyEnergy instanceof NumericTag numericTag) {
            energyContainer.setEnergy(Math.max(0L, numericTag.getAsLong()));
        }
    }
}
