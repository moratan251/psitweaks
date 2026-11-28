package com.moratan251.psitweaks.common.spells.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Picks from elements ranked by a key, for the distance-ranked and value-ranked pieces. Ranks are ascending (descending
 * when a negative count keeps the largest) and elements with equal keys keep their input order.
 */
public final class RankedSelection {
    private RankedSelection() {
    }

    /** The elements sorted by their keys, smallest first; equal keys keep the input order. */
    public static <T> List<T> ascending(List<T> elements, double[] keys) {
        return sorted(elements, keys, Comparator.comparingDouble((Integer i) -> keys[i]));
    }

    /** The elements sorted by their keys, largest first; equal keys keep the input order. */
    public static <T> List<T> descending(List<T> elements, double[] keys) {
        return sorted(elements, keys, Comparator.comparingDouble((Integer i) -> keys[i]).reversed());
    }

    private static <T> List<T> sorted(List<T> elements, double[] keys, Comparator<Integer> byKey) {
        Integer[] order = new Integer[elements.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        // Arrays.sort on objects is stable, so equal keys stay in the input order either way.
        Arrays.sort(order, byKey);
        List<T> sorted = new ArrayList<>(order.length);
        for (Integer i : order) {
            sorted.add(elements.get(i));
        }
        return sorted;
    }

    /**
     * The n elements with the smallest keys, smallest first, for a positive n, or the -n with the largest keys,
     * largest first, for a negative n. Equal keys keep the input order in both cases. The count is truncated toward
     * zero, 0 and NaN keep nothing, and a count beyond the size keeps everything.
     */
    public static <T> List<T> keep(List<T> elements, double[] keys, double count) {
        long requested = Double.isNaN(count) ? 0L : (long) count;
        long magnitude = requested == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(requested);
        int kept = (int) Math.min(magnitude, elements.size());
        if (kept == 0) {
            return new ArrayList<>();
        }
        List<T> ranked = requested >= 0 ? ascending(elements, keys) : descending(elements, keys);
        return new ArrayList<>(ranked.subList(0, kept));
    }

    /**
     * The element at the index of the ranking, 0 being the first; a negative index counts from the end (-1 is the
     * last). The index is truncated toward zero, and one outside the ranking is psi.spellerror.out_of_bounds.
     */
    public static <T> T element(List<T> ascending, double index) throws SpellRuntimeException {
        int size = ascending.size();
        long requested = Double.isNaN(index) ? 0L : (long) index;
        long normalized = requested < 0 ? size + requested : requested;
        if (normalized < 0 || normalized >= size) {
            throw new SpellRuntimeException("psi.spellerror.out_of_bounds");
        }
        return ascending.get((int) normalized);
    }
}
