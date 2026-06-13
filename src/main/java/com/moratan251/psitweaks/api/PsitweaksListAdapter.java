package com.moratan251.psitweaks.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Adapter used by Psitweaks spell pieces to read and transform custom list-like
 * spell value types.
 */
@FunctionalInterface
public interface PsitweaksListAdapter<T> {
    int size(T list);

    default PsitweaksModeOption modeOption() {
        return null;
    }

    default Class<?> elementType() {
        return Object.class;
    }

    default SpellParam<?> createListParam(String name, boolean canDisable) {
        throw unsupported("createListParam");
    }

    default SpellParam<?> createElementParam(String name, boolean canDisable) {
        throw unsupported("createElementParam");
    }

    default Object emptyList() {
        throw unsupported("emptyList");
    }

    default Object emptyElement() {
        return null;
    }

    default Object coerceElement(SpellContext context, Object value) throws SpellRuntimeException {
        return value;
    }

    default Object elementFromString(String value) {
        throw unsupported("elementFromString");
    }

    default Object listFromStrings(Iterable<String> values) {
        List<Object> elements = new ArrayList<>();
        for (String value : values) {
            elements.add(elementFromString(value));
        }
        return add(castList(emptyList()), elements);
    }

    default String elementToString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    default List<String> listToStrings(T list) {
        List<String> result = new ArrayList<>();
        int size = size(list);
        for (int i = 0; i < size; i++) {
            result.add(elementToString(get(list, i)));
        }
        return List.copyOf(result);
    }

    default Object get(T list, int index) {
        throw unsupported("get");
    }

    default Object add(T list, List<?> elements) {
        throw unsupported("add");
    }

    default Object remove(T list, List<?> elements) {
        throw unsupported("remove");
    }

    default Object removeIndices(T list, List<Integer> indices) {
        Set<Integer> removed = new HashSet<>(indices);
        List<Object> result = new ArrayList<>();
        int size = size(list);
        for (int i = 0; i < size; i++) {
            if (!removed.contains(i)) {
                result.add(get(list, i));
            }
        }
        return add(castList(emptyList()), result);
    }

    default Object insert(T list, int index, Object element) {
        int size = size(list);
        List<Object> result = new ArrayList<>(size + 1);
        for (int i = 0; i <= size; i++) {
            if (i == index) {
                result.add(element);
            }
            if (i < size) {
                result.add(get(list, i));
            }
        }
        return add(castList(emptyList()), result);
    }

    default Object exclusion(T left, T right) {
        throw unsupported("exclusion");
    }

    default Object intersection(T left, T right) {
        throw unsupported("intersection");
    }

    default Object concatenation(T left, T right) {
        throw unsupported("concatenation");
    }

    @SuppressWarnings("unchecked")
    private T castList(Object list) {
        return (T) list;
    }

    private UnsupportedOperationException unsupported(String operation) {
        PsitweaksModeOption option = modeOption();
        String target = option == null ? getClass().getName() : option.id().toString();
        return new UnsupportedOperationException("PsiTweaks list adapter " + target
                + " does not support " + operation);
    }
}
