package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.items.FilterCards;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Template editor of a held Filter Card. The nine template slots are ghost slots: clicking one with an item registers
 * a count-1 copy without taking the item, and clicking with an empty cursor or shift-clicking clears it. Shift-clicking
 * an inventory item registers it in the first empty template slot. The edited card itself cannot be moved.
 */
public class FilterCardMenu extends AbstractContainerMenu {
    private static final int TEMPLATE_END = FilterCards.SLOT_COUNT;
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private final Player player;
    private final InteractionHand hand;
    private final SimpleContainer templates = new SimpleContainer(FilterCards.SLOT_COUNT);
    private final int lockedSlot;

    public static FilterCardMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        return new FilterCardMenu(containerId, playerInventory, buf.readEnum(InteractionHand.class));
    }

    public FilterCardMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.FILTER_CARD.get(), containerId);
        this.player = playerInventory.player;
        this.hand = hand;

        ItemStack card = player.getItemInHand(hand);
        if (FilterCards.isFilterCard(card)) {
            List<ItemStack> stored = FilterCards.templates(card);
            for (int slot = 0; slot < FilterCards.SLOT_COUNT; slot++) {
                templates.setItem(slot, stored.get(slot));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new TemplateSlot(templates, col + row * 3, 62 + col * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        int locked = -1;
        for (int col = 0; col < 9; col++) {
            if (hand == InteractionHand.MAIN_HAND && col == playerInventory.selected) {
                locked = slots.size();
                addSlot(new LockedSlot(playerInventory, col, 8 + col * 18, 142));
            } else {
                addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
            }
        }
        this.lockedSlot = locked;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player clicker) {
        if (slotId >= 0 && slotId < TEMPLATE_END) {
            clickTemplate(slotId, button, clickType);
            return;
        }
        if (slotId == lockedSlot && lockedSlot >= 0) {
            return;
        }
        if (clickType == ClickType.SWAP && movesCard(button)) {
            return;
        }
        super.clicked(slotId, button, clickType, clicker);
    }

    private void clickTemplate(int slot, int button, ClickType clickType) {
        switch (clickType) {
            case PICKUP -> setTemplate(slot, getCarried());
            case QUICK_MOVE -> setTemplate(slot, ItemStack.EMPTY);
            case SWAP -> setTemplate(slot, swapSource(button));
            default -> {
            }
        }
    }

    private ItemStack swapSource(int button) {
        if (button == OFFHAND_SWAP_BUTTON) {
            return player.getOffhandItem();
        }
        return button >= 0 && button < Inventory.getSelectionSize() ? player.getInventory().getItem(button)
                : ItemStack.EMPTY;
    }

    private boolean movesCard(int button) {
        return hand == InteractionHand.OFF_HAND ? button == OFFHAND_SWAP_BUTTON
                : button == player.getInventory().selected;
    }

    private void setTemplate(int slot, ItemStack stack) {
        templates.setItem(slot, FilterCards.asTemplate(stack));
        saveToCard();
    }

    private void saveToCard() {
        ItemStack card = player.getItemInHand(hand);
        if (!FilterCards.isFilterCard(card)) {
            return;
        }
        List<ItemStack> values = new ArrayList<>(FilterCards.SLOT_COUNT);
        for (int slot = 0; slot < FilterCards.SLOT_COUNT; slot++) {
            values.add(templates.getItem(slot));
        }
        FilterCards.setTemplates(card, values);
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        if (index < TEMPLATE_END || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slots.get(index).getItem();
        ItemStack template = FilterCards.asTemplate(source);
        if (template.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < FilterCards.SLOT_COUNT; slot++) {
            if (ItemStack.isSameItemSameComponents(templates.getItem(slot), template)) {
                return ItemStack.EMPTY;
            }
        }
        for (int slot = 0; slot < FilterCards.SLOT_COUNT; slot++) {
            if (templates.getItem(slot).isEmpty()) {
                setTemplate(slot, template);
                break;
            }
        }
        // The source item stays where it is, so the shift-click loop must stop here.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof TemplateSlot) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return !(slot instanceof TemplateSlot) && super.canDragTo(slot);
    }

    @Override
    public boolean stillValid(Player clicker) {
        return !clicker.isRemoved() && FilterCards.isFilterCard(clicker.getItemInHand(hand));
    }

    private static final class TemplateSlot extends Slot {
        private TemplateSlot(Container container, int slot, int x, int y) {
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
            return new FilterCardMenu(containerId, playerInventory, hand);
        }
    }
}
