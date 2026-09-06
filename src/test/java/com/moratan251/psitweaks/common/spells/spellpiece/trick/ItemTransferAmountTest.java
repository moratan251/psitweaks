package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ItemTransferAmountTest {
    @Test
    void singleItemNeverOffersAFullStack() {
        assertEquals(1, ItemTransferAmount.calculate(1, 64, -1));
        assertEquals(1, ItemTransferAmount.calculate(1, 64, 64));
    }

    @Test
    void honorsBothRequestAndAvailableCount() {
        assertEquals(5, ItemTransferAmount.calculate(12, 64, 5));
        assertEquals(12, ItemTransferAmount.calculate(12, 64, 64));
        assertEquals(64, ItemTransferAmount.calculate(64, 64, -1));
        assertEquals(1, ItemTransferAmount.calculate(1, 1, 64));
        assertEquals(0, ItemTransferAmount.calculate(0, 64, -1));
    }
}
