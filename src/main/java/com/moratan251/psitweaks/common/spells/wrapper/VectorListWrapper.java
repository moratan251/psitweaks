package com.moratan251.psitweaks.common.spells.wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import vazkii.psi.api.internal.Vector3;

public final class VectorListWrapper implements Iterable<Vector3> {
    public static final VectorListWrapper EMPTY = new VectorListWrapper(List.of());

    private final List<Vector3> list;

    private VectorListWrapper(List<Vector3> list) {
        this.list = copyValues(list);
    }

    public static VectorListWrapper make(List<Vector3> values) {
        if (values == null || values.isEmpty()) {
            return EMPTY;
        }

        List<Vector3> result = new ArrayList<>(values.size());
        for (Vector3 value : values) {
            if (value != null) {
                result.add(value.copy());
            }
        }

        return result.isEmpty() ? EMPTY : new VectorListWrapper(result);
    }

    public int size() {
        return list.size();
    }

    public Vector3 get(int index) {
        return list.get(index).copy();
    }

    public List<Vector3> asList() {
        return Collections.unmodifiableList(copyValues(list));
    }

    @Override
    public Iterator<Vector3> iterator() {
        return asList().iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    private static List<Vector3> copyValues(List<Vector3> values) {
        List<Vector3> result = new ArrayList<>(values.size());
        for (Vector3 value : values) {
            if (value != null) {
                result.add(value.copy());
            }
        }
        return List.copyOf(result);
    }
}
