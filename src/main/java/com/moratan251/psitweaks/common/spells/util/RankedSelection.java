package com.moratan251.psitweaks.common.spells.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Picks from elements ranked by a key, for the distance-ranked and value-ranked pieces. Ranks are ascending and
 * elements with equal keys keep their input order.
 */
public final class RankedSelection {
    private RankedSelection() {
    }

    /** The elements sorted by their keys, smallest first; equal keys keep the input order. */
    public static <T> List<T> ascending(List<T> elements, double[] keys) {
        Integer[] order = new Integer[elements.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingDouble((Integer i) -> keys[i]));
        List<T> sorted = new ArrayList<>(order.length);
        for (Integer i : order) {
            sorted.add(elements.get(i));
        }
        return sorted;
    }

    /**
     * The first n of the ranking for a positive n, or the last -n, last first, for a negative n. The count is
     * truncated toward zero, 0 and NaN keep nothing, and a count beyond the size keeps everything.
     */
    public static <T> List<T> keep(List<T> ascending, double count) {
        int size = ascending.size();
        long requested = Double.isNaN(count) ? 0L : (long) count;
        long magnitude = requested == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(requested);
        int kept = (int) Math.min(magnitude, size);
        List<T> result = new ArrayList<>(kept);
        if (requested >= 0) {
            result.addAll(ascending.subList(0, kept));
        } else {
            for (int i = size - 1; i >= size - kept; i--) {
                result.add(ascending.get(i));
            }
        }
        return result;
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
