package com.moratan251.psitweaks.common.storage.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IdeaStorageCapacityTest {

    @Test
    void acceptsSixtyFourthTypeAndRejectsSixtyFifthType() {
        assertEquals(1, IdeaStorageCapacity.simulateInsert(false, false, 63, 64,
                0, 100, 1));
        assertEquals(0, IdeaStorageCapacity.simulateInsert(false, false, 64, 64,
                0, 100, 1));
    }

    @Test
    void existingTypeCanGrowAtTypeLimit() {
        assertEquals(5, IdeaStorageCapacity.simulateInsert(false, true, 64, 64,
                10, 100, 5));
    }

    @Test
    void clampsOnlyToPerTypeCapacity() {
        assertEquals(3, IdeaStorageCapacity.simulateInsert(false, true, 1, 64,
                97, 100, 50));
        assertEquals(50, IdeaStorageCapacity.simulateInsert(false, true, 64, 64,
                10, 100, 50));
    }

    @Test
    void rejectsInsertWhileEntryIsOverPerTypeCapacity() {
        assertEquals(0, IdeaStorageCapacity.simulateInsert(false, true, 1, 64,
                101, 100, 1));
    }
}
