package com.moratan251.psitweaks.common.storage.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IdeaStorageCapacityTest {

    @Test
    void acceptsSixtyFourthTypeAndRejectsSixtyFifthType() {
        assertEquals(1, IdeaStorageCapacity.simulateInsert(false, false, 63, 64,
                0, 63, 100, 1_000, 1));
        assertEquals(0, IdeaStorageCapacity.simulateInsert(false, false, 64, 64,
                0, 64, 100, 1_000, 1));
    }

    @Test
    void existingTypeCanGrowAtTypeLimit() {
        assertEquals(5, IdeaStorageCapacity.simulateInsert(false, true, 64, 64,
                10, 640, 100, 1_000, 5));
    }

    @Test
    void clampsIndependentlyToPerTypeAndTotalCapacity() {
        assertEquals(3, IdeaStorageCapacity.simulateInsert(false, true, 1, 64,
                97, 100, 100, 1_000, 50));
        assertEquals(4, IdeaStorageCapacity.simulateInsert(false, true, 1, 64,
                10, 996, 100, 1_000, 50));
    }

    @Test
    void rejectsInsertWhileCategoryIsOverCapacity() {
        assertEquals(0, IdeaStorageCapacity.simulateInsert(false, true, 1, 64,
                101, 101, 100, 1_000, 1));
        assertEquals(0, IdeaStorageCapacity.simulateInsert(false, true, 1, 64,
                10, 1_001, 100, 1_000, 1));
    }
}
