package com.moratan251.psitweaks.client.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IdeaStorageAmountFormatterTest {

    @Test
    void convertsRawFluidAndChemicalAmountsToBuckets() {
        assertEquals("3", IdeaStorageAmountFormatter.formatExact(3_000L,
                IdeaStorageAmountFormatter.BUCKET_SCALE));
        assertEquals("3.5", IdeaStorageAmountFormatter.formatExact(3_500L,
                IdeaStorageAmountFormatter.BUCKET_SCALE));
        assertEquals("0.001", IdeaStorageAmountFormatter.formatExact(1L,
                IdeaStorageAmountFormatter.BUCKET_SCALE));
        assertEquals(0, IdeaStorageAmountFormatter.asDisplayAmount(3_000L,
                        IdeaStorageAmountFormatter.BUCKET_SCALE)
                .compareTo(IdeaStorageAmountFormatter.asDisplayAmount(3L,
                        IdeaStorageAmountFormatter.ITEM_SCALE)));
    }

    @Test
    void doesNotAbbreviateBeforeOneHundredThousand() {
        assertEquals("1000", IdeaStorageAmountFormatter.formatGrid(1_000L,
                IdeaStorageAmountFormatter.ITEM_SCALE));
        assertEquals("99999", IdeaStorageAmountFormatter.formatGrid(99_999L,
                IdeaStorageAmountFormatter.ITEM_SCALE));
        assertEquals("3", IdeaStorageAmountFormatter.formatGrid(3_000L,
                IdeaStorageAmountFormatter.BUCKET_SCALE));
    }

    @Test
    void abbreviatesAtOneHundredThousandAndOneMillion() {
        assertEquals("100k", IdeaStorageAmountFormatter.formatGrid(100_000L,
                IdeaStorageAmountFormatter.ITEM_SCALE));
        assertEquals("100.5k", IdeaStorageAmountFormatter.formatGrid(100_500L,
                IdeaStorageAmountFormatter.ITEM_SCALE));
        assertEquals("999.9k", IdeaStorageAmountFormatter.formatGrid(999_999L,
                IdeaStorageAmountFormatter.ITEM_SCALE));
        assertEquals("1M", IdeaStorageAmountFormatter.formatGrid(1_000_000L,
                IdeaStorageAmountFormatter.ITEM_SCALE));
        assertEquals("1.5M", IdeaStorageAmountFormatter.formatGrid(1_500_000L,
                IdeaStorageAmountFormatter.ITEM_SCALE));
        assertEquals("100k", IdeaStorageAmountFormatter.formatGrid(100_000_000L,
                IdeaStorageAmountFormatter.BUCKET_SCALE));
    }

    @Test
    void rejectsNegativeScale() {
        assertThrows(IllegalArgumentException.class,
                () -> IdeaStorageAmountFormatter.formatExact(1L, -1));
    }
}
