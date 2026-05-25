package com.moratan251.psitweaks.common.spells.wrapper;

import com.moratan251.psitweaks.api.PsitweaksListLike;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class NumberListWrapper implements Iterable<Double>, PsitweaksListLike {
    public static final NumberListWrapper EMPTY = new NumberListWrapper(List.of());

    private final List<Double> list;

    private NumberListWrapper(List<Double> list) {
        this.list = List.copyOf(list);
    }

    public static NumberListWrapper make(List<? extends Number> values) {
        if (values == null || values.isEmpty()) {
            return EMPTY;
        }

        List<Double> result = new ArrayList<>(values.size());
        for (Number value : values) {
            if (value != null) {
                double number = value.doubleValue();
                if (Double.isFinite(number)) {
                    result.add(number);
                }
            }
        }

        return result.isEmpty() ? EMPTY : new NumberListWrapper(result);
    }

    public int size() {
        return list.size();
    }

    public Double get(int index) {
        return list.get(index);
    }

    public List<Double> asList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public Iterator<Double> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
