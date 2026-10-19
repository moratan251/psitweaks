package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.entities.combat.EntityDancingBlade;
import com.moratan251.psitweaks.common.items.BladePouches;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The eight sword slots of a Blade Pouch held in one hand. Every change is written back to the pouch at once; the pouch
 * itself cannot be moved while the screen is open, and the screen closes if that pouch leaves the hand (or the selected
 * hotbar slot changes), so edits can never land on another item. While the player's blades are out the swords are in
 * use and the sword slots are locked.
 */
public class BladePouchMenu extends AbstractContainerMenu {
    public static final int SWORDS_X = 8;
    public static final int SWORDS_Y = 18;
    private static final int POUCH_END = BladePouches.SLOT_COUNT;
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private final Player player;
    private final InteractionHand hand;
    /** The pouch the screen was opened on: the only stack it writes to. */
    private final ItemStack pouch;
    /** The selected hotbar slot when opened from the main hand, else -1. */
    private final int hotbarSlot;
    private final SimpleContainer swords;
    private final int lockedSlot;
    /** 1 while the player's Dancing Blades are out; worked out on the server and synced to the screen. */
    private final DataSlot deployed = DataSlot.standalone();

    public static BladePouchMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        return new BladePouchMenu(containerId, playerInventory, buf.readEnum(InteractionHand.class));
    }

    public BladePouchMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.BLADE_POUCH.get(), containerId);
        this.player = playerInventory.player;
        this.hand = hand;
        this.pouch = player.getItemInHand(hand);
        this.hotbarSlot = hand == InteractionHand.MAIN_HAND ? playerInventory.selected : -1;
        this.swords = new SimpleContainer(BladePouches.SLOT_COUNT) {
            @Override
            public void setChanged() {
                super.setChanged();
                saveToPouch();
            }
        };

        if (BladePouches.isPouch(pouch)) {
            List<ItemStack> stored = BladePouches.contents(pouch);
            for (int slot = 0; slot < BladePouches.SLOT_COUNT; slot++) {
                swords.setItem(slot, stored.get(slot));
            }
        }

        for (int slot = 0; slot < BladePouches.SLOT_COUNT; slot++) {
            addSlot(new SwordSlot(swords, slot, SWORDS_X + slot * 18, SWORDS_Y));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 49 + row * 18));
            }
        }
        int locked = -1;
        for (int col = 0; col < 9; col++) {
            if (col == hotbarSlot) {
                locked = slots.size();
                addSlot(new LockedSlot(playerInventory, col, 8 + col * 18, 107));
            } else {
                addSlot(new Slot(playerInventory, col, 8 + col * 18, 107));
            }
        }
        this.lockedSlot = locked;
        addDataSlot(deployed);
        updateDeployed();
    }

    /** Whether the swords are out as blades, so the sword slots are locked. */
    public boolean isDeployed() {
        return deployed.get() != 0;
    }

    private void updateDeployed() {
        if (player.level() instanceof ServerLevel level) {
            deployed.set(EntityDancingBlade.hasBladesOut(level, player) ? 1 : 0);
        }
    }

    @Override
    public void broadcastChanges() {
        updateDeployed();
        super.broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player clicker) {
        if (slotId == lockedSlot && lockedSlot >= 0) {
            return;
        }
        if (clickType == ClickType.SWAP && movesPouch(button)) {
            return;
        }
        updateDeployed();
        super.clicked(slotId, button, clickType, clicker);
    }

    private boolean movesPouch(int button) {
        return hand == InteractionHand.OFF_HAND ? button == OFFHAND_SWAP_BUTTON : button == hotbarSlot;
    }

    private void saveToPouch() {
        if (!BladePouches.isPouch(pouch)) {
            return;
        }
        List<ItemStack> values = new ArrayList<>(BladePouches.SLOT_COUNT);
        for (int slot = 0; slot < BladePouches.SLOT_COUNT; slot++) {
            values.add(swords.getItem(slot));
        }
        BladePouches.setContents(pouch, values);
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < POUCH_END) {
            if (isDeployed() || !moveItemStackTo(stack, POUCH_END, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (isDeployed() || !BladePouches.isSword(stack) || !moveItemStackTo(stack, 0, POUCH_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    /** Valid only while the very pouch it was opened on is still in that hand (and that hotbar slot is selected). */
    @Override
    public boolean stillValid(Player clicker) {
        return !clicker.isRemoved() && BladePouches.isPouch(pouch) && clicker.getItemInHand(hand) == pouch
                && (hotbarSlot < 0 || clicker.getInventory().selected == hotbarSlot);
    }

    private final class SwordSlot extends Slot {
        private SwordSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !isDeployed() && BladePouches.isSword(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !isDeployed();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class LockedSlot extends Slot {
        private LockedSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    public record Provider(InteractionHand hand, Component title) implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return title;
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new BladePouchMenu(containerId, playerInventory, hand);
        }
    }
}
