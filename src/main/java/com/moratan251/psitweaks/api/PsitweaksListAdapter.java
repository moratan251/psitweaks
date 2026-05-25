package com.moratan251.psitweaks.api;

import java.util.List;
import vazkii.psi.api.spell.SpellParam;

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

    default Object get(T list, int index) {
        throw unsupported("get");
    }

    default Object add(T list, List<?> elements) {
        throw unsupported("add");
    }

    default Object remove(T list, List<?> elements) {
        throw unsupported("remove");
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

    private UnsupportedOperationException unsupported(String operation) {
        PsitweaksModeOption option = modeOption();
        String target = option == null ? getClass().getName() : option.id().toString();
        return new UnsupportedOperationException("PsiTweaks list adapter " + target
                + " does not support " + operation);
    }
}
