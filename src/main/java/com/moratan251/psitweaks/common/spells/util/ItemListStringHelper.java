package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public final class ItemListStringHelper {
    private ItemListStringHelper() {
    }

    public static StringListWrapper fromContainer(Container container) {
        if (container == null) {
            return StringListWrapper.EMPTY;
        }

        Map<String, Long> counts = new TreeMap<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            add(counts, container.getItem(i));
        }
        return toStringList(counts);
    }

    public static StringListWrapper fromItemHandler(IItemHandler handler) {
        if (handler == null) {
            return StringListWrapper.EMPTY;
        }

        Map<String, Long> counts = new TreeMap<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            add(counts, handler.getStackInSlot(i));
        }
        return toStringList(counts);
    }

    public static StringListWrapper fromStacks(Iterable<ItemStack> stacks) {
        if (stacks == null) {
            return StringListWrapper.EMPTY;
        }

        Map<String, Long> counts = new TreeMap<>();
        for (ItemStack stack : stacks) {
            add(counts, stack);
        }
        return toStringList(counts);
    }

    private static void add(Map<String, Long> counts, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        counts.merge(id.toString(), (long) stack.getCount(), Long::sum);
    }

    private static StringListWrapper toStringList(Map<String, Long> counts) {
        if (counts.isEmpty()) {
            return StringListWrapper.EMPTY;
        }

        List<String> result = new ArrayList<>(counts.size());
        counts.forEach((id, count) -> result.add(id + "|" + count));
        return StringListWrapper.make(result);
    }
}
