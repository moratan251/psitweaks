package com.moratan251.psitweaks.common.spells.spellpiece.operator;

final class SliceIndexHelper {
    private SliceIndexHelper() {
    }

    static int normalize(int index, int size) {
        long normalized = index < 0 ? (long) size + index : index;
        return (int) Math.max(0L, Math.min(normalized, size));
    }
}
