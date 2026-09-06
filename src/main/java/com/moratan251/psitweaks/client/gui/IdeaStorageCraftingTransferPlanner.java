package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageFillCrafting;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** JEI/EMI共通のクライアント側材料候補選択。実移動と再検証はサーバー側が行う。 */
public final class IdeaStorageCraftingTransferPlanner {
    private IdeaStorageCraftingTransferPlanner() {
    }

    public static Plan plan(IdeaStorageMenu menu, List<List<ItemStack>> candidatesBySlot) {
        if (candidatesBySlot.size() > MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT) {
            return new Plan(List.of(), List.of(0));
        }

        List<AvailableStack> available = collectAvailable(menu);
        List<ItemStack> templates = new ArrayList<>(MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT);
        List<Integer> missingSlots = new ArrayList<>();
        for (int slot = 0; slot < MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT; slot++) {
            if (slot >= candidatesBySlot.size() || candidatesBySlot.get(slot).isEmpty()) {
                templates.add(ItemStack.EMPTY);
                continue;
            }
            ItemStack selected = consumeOne(available, candidatesBySlot.get(slot));
            if (selected.isEmpty()) {
                missingSlots.add(slot);
                templates.add(ItemStack.EMPTY);
            } else {
                templates.add(selected.copyWithCount(1));
            }
        }
        return new Plan(List.copyOf(templates), List.copyOf(missingSlots));
    }

    private static List<AvailableStack> collectAvailable(IdeaStorageMenu menu) {
        List<AvailableStack> available = new ArrayList<>();
        for (int slot = 0; slot < IdeaStorageMenu.CRAFT_RESULT_SLOT; slot++) {
            addAvailable(available, menu.getSlot(slot).getItem(), menu.getSlot(slot).getItem().getCount());
        }
        for (MessageIdeaStorageSync.Entry entry : menu.clientStorageEntries()) {
            addAvailable(available, entry.template(), entry.count());
        }
        return available;
    }

    private static void addAvailable(List<AvailableStack> available, ItemStack template, long count) {
        if (template.isEmpty() || count <= 0) {
            return;
        }
        for (AvailableStack entry : available) {
            if (ItemStack.isSameItemSameTags(entry.template, template)) {
                entry.count = Math.addExact(entry.count, count);
                return;
            }
        }
        available.add(new AvailableStack(template.copyWithCount(1), count));
    }

    private static ItemStack consumeOne(List<AvailableStack> available, List<ItemStack> candidates) {
        for (ItemStack candidate : candidates) {
            if (candidate.isEmpty()) {
                continue;
            }
            for (AvailableStack entry : available) {
                if (entry.count > 0 && ItemStack.isSameItemSameTags(entry.template, candidate)) {
                    entry.count--;
                    return entry.template;
                }
            }
        }
        for (ItemStack candidate : candidates) {
            if (candidate.isEmpty()) {
                continue;
            }
            for (AvailableStack entry : available) {
                if (entry.count > 0 && ItemStack.isSameItem(entry.template, candidate)) {
                    entry.count--;
                    return entry.template;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public record Plan(List<ItemStack> templates, List<Integer> missingSlots) {
        public boolean complete() {
            return templates.size() == MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT && missingSlots.isEmpty();
        }
    }

    private static final class AvailableStack {
        private final ItemStack template;
        private long count;

        private AvailableStack(ItemStack template, long count) {
            this.template = template;
            this.count = count;
        }
    }
}
