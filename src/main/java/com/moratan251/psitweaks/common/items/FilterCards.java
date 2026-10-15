package com.moratan251.psitweaks.common.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Template storage and lookup of Filter Cards.
 *
 * <p>Only one card is in use at a time: the first one found in the Curios slots (in Curios' slot order), then the
 * main hand, the offhand, the hotbar and the main inventory. Every other card is idle. Server-side spell pieces and the
 * client-side item texture both use this order, so the card shown as in use is the card that spells read.</p>
 *
 * <p>The templates are filter samples, not stored items, so they live under their own NBT key
 * ({@value #TAG_TEMPLATES}) instead of a container tag and are never exposed as a real inventory.</p>
 */
public final class FilterCards {
    public static final int SLOT_COUNT = 9;
    private static final String TAG_TEMPLATES = "FilterCardTemplates";

    private FilterCards() {
    }

    public static boolean isFilterCard(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFilterCard;
    }

    /** Copies of the card's templates, always {@link #SLOT_COUNT} entries; empty slots are {@link ItemStack#EMPTY}. */
    public static NonNullList<ItemStack> templates(ItemStack card) {
        NonNullList<ItemStack> templates = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        CompoundTag stored = card.getTagElement(TAG_TEMPLATES);
        if (stored != null) {
            ContainerHelper.loadAllItems(stored, templates);
        }
        return templates;
    }

    /** A copy of one template, or {@link ItemStack#EMPTY} for an empty or out-of-range slot (0-based). */
    public static ItemStack template(ItemStack card, int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return templates(card).get(slot);
    }

    /** Stores the templates as count-1 copies. Entries past {@link #SLOT_COUNT} are ignored. */
    public static void setTemplates(ItemStack card, List<ItemStack> templates) {
        NonNullList<ItemStack> normalized = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        boolean empty = true;
        for (int i = 0; i < SLOT_COUNT && i < templates.size(); i++) {
            ItemStack template = asTemplate(templates.get(i));
            normalized.set(i, template);
            empty &= template.isEmpty();
        }
        if (empty) {
            card.removeTagKey(TAG_TEMPLATES);
        } else {
            card.addTagElement(TAG_TEMPLATES, ContainerHelper.saveAllItems(new CompoundTag(), normalized, false));
        }
    }

    /** A template is a count-1 copy; a Filter Card cannot be registered as a template of another card. */
    public static ItemStack asTemplate(ItemStack stack) {
        if (stack.isEmpty() || isFilterCard(stack)) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(1);
    }

    /** The card spells read, or empty when the player carries no Filter Card. */
    public static Optional<ItemStack> findActive(Player player) {
        List<ItemStack> cards = findCards(player);
        return cards.isEmpty() ? Optional.empty() : Optional.of(cards.get(0));
    }

    /**
     * True for a card the player carries that is not the card in use. The comparison is by identity, so a copy of a
     * card (a creative tab entry, a recipe viewer, the cursor stack) is never idle.
     */
    public static boolean isIdle(Player player, ItemStack stack) {
        if (!isFilterCard(stack)) {
            return false;
        }
        List<ItemStack> cards = findCards(player);
        for (int i = 1; i < cards.size(); i++) {
            if (cards.get(i) == stack) {
                return true;
            }
        }
        return false;
    }

    /** The player's Filter Cards in lookup order. The returned stacks are the live stacks, not copies. */
    public static List<ItemStack> findCards(Player player) {
        List<ItemStack> cards = new ArrayList<>();
        Set<ItemStack> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        collectCurios(player, cards, seen);

        Inventory inventory = player.getInventory();
        add(cards, seen, player.getMainHandItem());
        add(cards, seen, player.getOffhandItem());
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            add(cards, seen, inventory.items.get(slot));
        }
        return cards;
    }

    private static void collectCurios(Player player, List<ItemStack> cards, Set<ItemStack> seen) {
        CuriosApi.getCuriosInventory(player).ifPresent(curios -> {
            List<Map.Entry<String, ICurioStacksHandler>> handlers = new ArrayList<>(curios.getCurios().entrySet());
            // The map order is not guaranteed to match between server and client; sort like the Curios screen does.
            handlers.sort(Comparator
                    .comparingInt((Map.Entry<String, ICurioStacksHandler> entry) -> slotOrder(player, entry.getKey()))
                    .thenComparing(Map.Entry::getKey));
            for (Map.Entry<String, ICurioStacksHandler> entry : handlers) {
                ICurioStacksHandler handler = entry.getValue();
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    add(cards, seen, handler.getStacks().getStackInSlot(slot));
                }
            }
        });
    }

    private static int slotOrder(Player player, String identifier) {
        return CuriosApi.getSlot(identifier, player.level()).map(ISlotType::getOrder).orElse(Integer.MAX_VALUE);
    }

    private static void add(List<ItemStack> cards, Set<ItemStack> seen, ItemStack stack) {
        if (isFilterCard(stack) && seen.add(stack)) {
            cards.add(stack);
        }
    }
}
