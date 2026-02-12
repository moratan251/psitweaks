package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.client.gui.machine.ModMenuTypes;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.tile.machine.ProgramResearcherBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ProgramResearcherMenu extends AbstractContainerMenu {

    private static final int INPUT_SLOT_COUNT = ProgramResearcherBlockEntity.INPUT_SLOTS;
    private static final int MACHINE_SLOT_COUNT = ProgramResearcherBlockEntity.TOTAL_SLOTS;
    private static final int OUTPUT_SLOT_INDEX = ProgramResearcherBlockEntity.OUTPUT_SLOT;
    private static final int DATA_COUNT = ProgramResearcherBlockEntity.DATA_COUNT;

    private static final int DATA_PROGRESS = ProgramResearcherBlockEntity.DATA_PROGRESS;
    private static final int DATA_MAX_PROGRESS = ProgramResearcherBlockEntity.DATA_MAX_PROGRESS;
    private static final int DATA_ENERGY = ProgramResearcherBlockEntity.DATA_ENERGY;
    private static final int DATA_MAX_ENERGY = ProgramResearcherBlockEntity.DATA_MAX_ENERGY;

    private static final int INPUT_START_X = 38;
    private static final int INPUT_START_Y = 17;
    private static final int OUTPUT_X = 134;
    private static final int OUTPUT_Y = 35;

    private final IItemHandler itemHandler;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public static ProgramResearcherMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ProgramResearcherBlockEntity researcher) {
            return new ProgramResearcherMenu(containerId, playerInventory, researcher, researcher.getDataAccess());
        }
        return new ProgramResearcherMenu(
                containerId,
                playerInventory,
                new ItemStackHandler(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public ProgramResearcherMenu(int containerId, Inventory playerInventory, ProgramResearcherBlockEntity blockEntity, ContainerData data) {
        this(containerId,
                playerInventory,
                blockEntity.getItemHandler(),
                data,
                ContainerLevelAccess.create(playerInventory.player.level(), blockEntity.getBlockPos()));
    }

    private ProgramResearcherMenu(int containerId, Inventory playerInventory, IItemHandler itemHandler, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.PROGRAM_RESEARCHER.get(), containerId);
        this.itemHandler = itemHandler;
        this.data = data;
        this.access = access;
        checkContainerDataCount(data, DATA_COUNT);

        // 3x3 input slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                int x = INPUT_START_X + col * 18;
                int y = INPUT_START_Y + row * 18;
                this.addSlot(new SlotItemHandler(itemHandler, index, x, y));
            }
        }

        // Output slot
        this.addSlot(new SlotItemHandler(itemHandler, OUTPUT_SLOT_INDEX, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(access, player, PsitweaksBlocks.PROGRAM_RESEARCHER.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, MACHINE_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(sourceStack, 0, INPUT_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copy;
    }

    public boolean isCrafting() {
        return data.get(DATA_PROGRESS) > 0 && data.get(DATA_MAX_PROGRESS) > 0;
    }

    public int getScaledProgress(int width) {
        int current = data.get(DATA_PROGRESS);
        int max = data.get(DATA_MAX_PROGRESS);
        if (current <= 0 || max <= 0) {
            return 0;
        }
        return Math.min(width, (int) ((current / (float) max) * width));
    }

    public int getScaledEnergy(int height) {
        int current = data.get(DATA_ENERGY);
        int max = data.get(DATA_MAX_ENERGY);
        if (current <= 0 || max <= 0) {
            return 0;
        }
        return Math.min(height, (int) ((current / (float) max) * height));
    }

    public int getEnergyStored() {
        return data.get(DATA_ENERGY);
    }

    public int getMaxEnergyStored() {
        return data.get(DATA_MAX_ENERGY);
    }
}
