package com.moratan251.psitweaks.common.spells.wrapper;

import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class SpellItemListWrapper implements Iterable<SpellItemValue> {
    public static final SpellItemListWrapper EMPTY = new SpellItemListWrapper(List.of());

    private final List<SpellItemValue> list;

    private SpellItemListWrapper(List<SpellItemValue> list) {
        this.list = List.copyOf(list);
    }

    public static SpellItemListWrapper make(List<SpellItemValue> values) {
        if (values == null || values.isEmpty()) {
            return EMPTY;
        }

        List<SpellItemValue> result = new ArrayList<>(values.size());
        for (SpellItemValue value : values) {
            if (value != null && !value.isEmpty()) {
                SpellItemValue copied = new SpellItemValue(value.copyStack(), value.source());
                if (!containsEquivalent(result, copied)) {
                    result.add(copied);
                }
            }
        }

        return result.isEmpty() ? EMPTY : new SpellItemListWrapper(result);
    }

    public int size() {
        return list.size();
    }

    public SpellItemValue get(int index) {
        SpellItemValue value = list.get(index);
        return new SpellItemValue(value.copyStack(), value.source());
    }

    public List<SpellItemValue> asList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public Iterator<SpellItemValue> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    private static boolean containsEquivalent(List<SpellItemValue> values, SpellItemValue candidate) {
        for (SpellItemValue value : values) {
            if (isEquivalent(value, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEquivalent(SpellItemValue left, SpellItemValue right) {
        if (left.source().isPresent() || right.source().isPresent()) {
            return left.source().equals(right.source());
        }
        return ItemStack.matches(left.copyStack(), right.copyStack());
    }
}
