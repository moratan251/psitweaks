package com.moratan251.psitweaks.common.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IdeaStorageCraftingLimitsTest {

    @Test
    void shiftCraftLimitProducesAtMostOneNormalStack() {
        assertEquals(64, IdeaStorageCraftingLimits.calculateShiftCraftLimit(1, 64));
        assertEquals(21, IdeaStorageCraftingLimits.calculateShiftCraftLimit(3, 64));
        assertEquals(1, IdeaStorageCraftingLimits.calculateShiftCraftLimit(64, 64));
    }

    @Test
    void shiftCraftLimitAllowsOneOversizedRecipeResult() {
        assertEquals(1, IdeaStorageCraftingLimits.calculateShiftCraftLimit(65, 64));
    }

    @Test
    void shiftCraftLimitRejectsInvalidSizes() {
        assertEquals(0, IdeaStorageCraftingLimits.calculateShiftCraftLimit(0, 64));
        assertEquals(0, IdeaStorageCraftingLimits.calculateShiftCraftLimit(1, 0));
    }
}
