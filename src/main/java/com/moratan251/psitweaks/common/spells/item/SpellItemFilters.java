package com.moratan251.psitweaks.common.spells.item;

import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Item filters of the transfer tricks, from an Item, an Item List, a strict Item or (for search inputs) an ID String. */
public final class SpellItemFilters {
    public static final String ERROR_NULL_ITEM = "psitweaks.spellerror.nullitem";

    private SpellItemFilters() {
    }

    /**
     * Builds the filter for a connected Item filter input. An empty Item is an error, as before; an Item List matches
     * any of its items and an empty list matches nothing. Strict compares data components too, never the count; a
     * strict Item from Operator: Strict Item always does.
     */
    public static Predicate<ItemStack> of(Object value, boolean strict) throws SpellRuntimeException {
        if (value instanceof StrictItemFilter filter) {
            return filter::test;
        }
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

    /**
     * Builds the filter for a search input, which takes an ID String besides the values of {@link #of}. A String is a
     * wildcard pattern over item IDs; null and an empty String match everything.
     */
    public static Predicate<ItemStack> search(Object value) throws SpellRuntimeException {
        if (value == null) {
            return stack -> true;
        }
        if (value instanceof String pattern) {
            Predicate<ResourceLocation> ids = idFilter(pattern);
            return stack -> ids.test(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        return of(value, false);
    }

    /** False when a search input is unconnected or an empty String, i.e. when {@link #search} accepts everything. */
    public static boolean isSearchSet(Object value) {
        return value != null && !(value instanceof String pattern && pattern.isEmpty());
    }

    /**
     * Wildcard filter over resource IDs: * is any text and ? any single character. Without a namespace the pattern
     * matches the path of every mod's IDs. Null and an empty pattern match everything.
     */
    public static Predicate<ResourceLocation> idFilter(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return id -> true;
        }
        var matcher = WildcardStringMatcher.compile(pattern);
        return id -> matcher.matches(pattern.indexOf(':') >= 0 ? id.toString() : id.getPath());
    }
}
