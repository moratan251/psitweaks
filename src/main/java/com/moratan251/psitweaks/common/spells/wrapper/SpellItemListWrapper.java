package com.moratan251.psitweaks.common.spells.wrapper;

import com.moratan251.psitweaks.api.PsitweaksListLike;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A List of Items without empty or equivalent elements. {@link SpellItemValue} never exposes its stack, so the elements
 * are shared rather than copied: an element keeps its identity, which lets the NBT pieces reuse its NBT within a cast.
 */
public final class SpellItemListWrapper implements Iterable<SpellItemValue>, PsitweaksListLike {
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
        Set<EquivalenceKey> seen = new HashSet<>();
        for (SpellItemValue value : values) {
            if (value != null && !value.isEmpty() && seen.add(new EquivalenceKey(value))) {
                result.add(value);
            }
        }

        return result.isEmpty() ? EMPTY : new SpellItemListWrapper(result);
    }

    public int size() {
        return list.size();
    }

    public SpellItemValue get(int index) {
        return list.get(index);
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

    /** Compares by {@link SpellItemValue#equivalent}. */
    private record EquivalenceKey(SpellItemValue value) {
        @Override
        public boolean equals(Object other) {
            return other instanceof EquivalenceKey key && SpellItemValue.equivalent(value, key.value);
        }

        @Override
        public int hashCode() {
            return value.equivalenceHash();
        }
    }
}
