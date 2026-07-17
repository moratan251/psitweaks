package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.recipe.ProgramResearchRecipe;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
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
    private FloatingLong currentEnergyCost = FloatingLong.ZERO;
    @Nullable
    private ProgramResearchRecipe activeRecipe;
    private List<ItemStack> activeInputSnapshot = List.of();

    public ProgramResearcherBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

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
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(
                this::getDirection,
                () -> configComponent
        );
        energyContainer = MachineEnergyContainer.input(this, listener);
        builder.addContainer(energyContainer);
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(
                this::getDirection,
                () -> configComponent
        );

        List<InputInventorySlot> inputs = new ArrayList<>(INPUT_SLOTS);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            int x = INPUT_START_X + (slot % 3) * 18;
            int y = INPUT_START_Y + (slot / 3) * 18;
            inputs.add(builder.addSlot(InputInventorySlot.at(listener, x, y)));
        }
        inputSlots = List.copyOf(inputs);

        outputSlot = builder.addSlot(OutputInventorySlot.at(listener, OUTPUT_X, OUTPUT_Y));
        energySlot = builder.addSlot(EnergyInventorySlot.fillOrConvert(
                energyContainer,
                this::getLevel,
                listener,
                ENERGY_SLOT_X,
                ENERGY_SLOT_Y
        ));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        if (!MekanismUtils.canFunction(this)) {
            currentEnergyCost = FloatingLong.ZERO;
            setActive(false);
            return;
        }

        ProgramResearchRecipe recipe = findRecipe();
        if (recipe == null) {
            currentEnergyCost = FloatingLong.ZERO;
            resetProgress();
            setActive(false);
            return;
        }

        SimpleContainer inputInventory = createInputInventory();
        int[] consumptionPlan = recipe.createConsumptionPlan(inputInventory);
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (consumptionPlan == null || !canOutput(result)) {
            currentEnergyCost = FloatingLong.ZERO;
            resetProgress();
            setActive(false);
            return;
        }

        validateActiveInputs(recipe);
        maxProgress = Math.max(1, recipe.getTime());
        FloatingLong energyCost = feToJoules(recipe.getEnergyCostForTick(progress));
        currentEnergyCost = energyCost;
        if (energyContainer.getEnergy().smallerThan(energyCost)) {
            setActive(false);
            return;
        }

        if (!energyCost.isZero()) {
            energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.INTERNAL);
        }

        progress++;
        setActive(true);
        setChanged();

        if (progress >= maxProgress) {
            consumeInputs(consumptionPlan);
            craftOutput(result);
            progress = 0;
            clearActiveInputs();
            setChanged();
        }
    }

    @Nullable
    private ProgramResearchRecipe findRecipe() {
        if (level == null) {
            return null;
        }
        return level.getRecipeManager()
                .getRecipeFor(PsitweaksRecipeTypes.PROGRAM_RESEARCH.get(), createInputInventory(), level)
                .orElse(null);
    }

    private SimpleContainer createInputInventory() {
        SimpleContainer inventory = new SimpleContainer(INPUT_SLOTS);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            inventory.setItem(slot, inputSlots.get(slot).getStack());
        }
        return inventory;
    }

    private void validateActiveInputs(ProgramResearchRecipe recipe) {
        if (progress <= 0 || activeRecipe == null) {
            captureActiveInputs(recipe);
            return;
        }
        if (recipe == activeRecipe && activeInputSnapshotMatches()) {
            return;
        }
        resetProgress();
        captureActiveInputs(recipe);
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
                    || !ItemStack.isSameItemSameTags(current, snapshot)) {
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
        if (!ItemStack.isSameItemSameTags(outputStack, result)) {
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

    private void resetProgress() {
        if (progress == 0 && maxProgress == 1 && activeRecipe == null && activeInputSnapshot.isEmpty()) {
            return;
        }
        progress = 0;
        maxProgress = 1;
        clearActiveInputs();
        setChanged();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> progress, value -> progress = Math.max(0, value)));
        container.track(SyncableInt.create(() -> maxProgress, value -> maxProgress = Math.max(1, value)));
        container.track(SyncableFloatingLong.create(
                () -> currentEnergyCost,
                value -> currentEnergyCost = value.max(FloatingLong.ZERO)
        ));
    }

    @Override
    protected void addGeneralPersistentData(CompoundTag dataMap) {
        super.addGeneralPersistentData(dataMap);
        dataMap.putInt(NBT_PROGRESS, progress);
        dataMap.putInt(NBT_MAX_PROGRESS, maxProgress);
    }

    @Override
    protected void loadGeneralPersistentData(CompoundTag dataMap) {
        super.loadGeneralPersistentData(dataMap);
        migrateLegacyInventory(dataMap);
        migrateLegacyEnergy(dataMap);
        progress = Math.max(0, dataMap.getInt(NBT_PROGRESS));
        maxProgress = Math.max(1, dataMap.getInt(NBT_MAX_PROGRESS));
    }

    @Override
    public int getRedstoneLevel() {
        int filledSlots = 0;
        for (InputInventorySlot inputSlot : inputSlots) {
            if (!inputSlot.isEmpty()) {
                filledSlots++;
            }
        }
        if (!outputSlot.isEmpty()) {
            filledSlots++;
        }
        return Math.round((filledSlots / (float) TOTAL_SLOTS) * 15);
    }

    public double getScaledProgress() {
        return progress <= 0 || maxProgress <= 0 ? 0.0D : progress / (double) maxProgress;
    }

    public MachineEnergyContainer<ProgramResearcherBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    public FloatingLong getCurrentEnergyCost() {
        return currentEnergyCost;
    }

    private void migrateLegacyInventory(CompoundTag dataMap) {
        if (!dataMap.contains(NBT_LEGACY_INVENTORY)) {
            return;
        }
        ItemStackHandler legacyInventory = new ItemStackHandler(TOTAL_SLOTS);
        legacyInventory.deserializeNBT(dataMap.getCompound(NBT_LEGACY_INVENTORY));
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            inputSlots.get(slot).setStack(legacyInventory.getStackInSlot(slot).copy());
        }
        outputSlot.setStack(legacyInventory.getStackInSlot(OUTPUT_SLOT).copy());
    }

    private void migrateLegacyEnergy(CompoundTag dataMap) {
        if (dataMap.contains(NBT_LEGACY_ENERGY)) {
            energyContainer.setEnergy(feToJoules(Math.max(0L, dataMap.getLong(NBT_LEGACY_ENERGY))));
        }
    }

    private static FloatingLong feToJoules(long forgeEnergy) {
        return IEnergyConversionHelper.INSTANCE.feConversion().convertFrom(forgeEnergy);
    }
}
