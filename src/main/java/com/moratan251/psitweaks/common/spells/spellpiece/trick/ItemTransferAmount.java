package com.moratan251.psitweaks.common.spells.spellpiece.trick;

/** Shared send/pull amount bound; an unspecified request still cannot exceed the source stack. */
final class ItemTransferAmount {
    private ItemTransferAmount() {
    }

    static int calculate(int available, int maxStackSize, int limit) {
        int cap = Math.min(available, maxStackSize);
        return limit < 0 ? cap : Math.min(limit, cap);
    }
}
