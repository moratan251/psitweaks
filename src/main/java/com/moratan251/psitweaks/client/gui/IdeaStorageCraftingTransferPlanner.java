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
        int[][] candidates = new int[MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT][];
        for (int slot = 0; slot < candidates.length; slot++) {
            candidates[slot] = slot < candidatesBySlot.size()
                    ? findCandidates(available, candidatesBySlot.get(slot)) : new int[0];
        }
        int[] assignment = IdeaStorageIngredientAssignment.assign(candidates,
                available.stream().mapToLong(entry -> entry.count).toArray());
        List<ItemStack> templates = new ArrayList<>(MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT);
        List<Integer> missingSlots = new ArrayList<>();
        for (int slot = 0; slot < MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT; slot++) {
            if (slot >= candidatesBySlot.size() || candidatesBySlot.get(slot).isEmpty()) {
                templates.add(ItemStack.EMPTY);
                continue;
            }
            int selected = assignment[slot];
            if (selected < 0) {
                missingSlots.add(slot);
                templates.add(ItemStack.EMPTY);
            } else {
                templates.add(available.get(selected).template.copyWithCount(1));
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
                entry.count = Math.min(MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT,
                        entry.count + Math.min(count, MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT));
                return;
            }
        }
        available.add(new AvailableStack(template.copyWithCount(1),
                Math.min(count, MessageIdeaStorageFillCrafting.CRAFT_SLOT_COUNT)));
    }

    private static int[] findCandidates(List<AvailableStack> available, List<ItemStack> candidates) {
        List<Integer> matches = new ArrayList<>();
        boolean[] added = new boolean[available.size()];
        // Preserve exact-match preference, then the existing item-only fallback.
        for (boolean exact : new boolean[] {true, false}) {
            for (ItemStack candidate : candidates) {
                if (candidate.isEmpty()) {
                    continue;
                }
                for (int index = 0; index < available.size(); index++) {
                    ItemStack template = available.get(index).template;
                    if (!added[index] && (exact ? ItemStack.isSameItemSameTags(template, candidate)
                            : ItemStack.isSameItem(template, candidate))) {
                        added[index] = true;
                        matches.add(index);
                    }
                }
            }
        }
        return matches.stream().mapToInt(Integer::intValue).toArray();
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
