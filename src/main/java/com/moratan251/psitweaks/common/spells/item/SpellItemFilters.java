package com.moratan251.psitweaks.common.spells.item;

import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Item filters of the transfer tricks, from an Item or an Item List. */
public final class SpellItemFilters {
    public static final String ERROR_NULL_ITEM = "psitweaks.spellerror.nullitem";

    private SpellItemFilters() {
    }

    /**
     * Builds the filter for a connected Item filter input. An empty Item is an error, as before; an Item List matches
     * any of its items and an empty list matches nothing. Strict compares data components too, never the count.
     */
    public static Predicate<ItemStack> of(Object value, boolean strict) throws SpellRuntimeException {
        if (value instanceof SpellItemValue item) {
            if (item.isEmpty()) {
                throw new SpellRuntimeException(ERROR_NULL_ITEM);
            }
            ItemStack template = item.snapshot();
            return strict ? stack -> ItemStack.isSameItemSameComponents(stack, template)
                    : stack -> stack.is(template.getItem());
        }
        if (value instanceof SpellItemListWrapper list) {
            if (strict) {
                List<ItemStack> templates = new ArrayList<>(list.size());
                for (SpellItemValue element : list) {
                    if (!element.isEmpty()) {
                        templates.add(element.snapshot());
                    }
                }
                return stack -> {
                    for (ItemStack template : templates) {
                        if (ItemStack.isSameItemSameComponents(stack, template)) {
                            return true;
                        }
                    }
                    return false;
                };
            }
            Set<Item> items = new HashSet<>();
            for (SpellItemValue element : list) {
                if (!element.isEmpty()) {
                    items.add(element.snapshot().getItem());
                }
            }
            return stack -> !stack.isEmpty() && items.contains(stack.getItem());
        }
        throw new SpellRuntimeException(ERROR_NULL_ITEM);
    }
}
