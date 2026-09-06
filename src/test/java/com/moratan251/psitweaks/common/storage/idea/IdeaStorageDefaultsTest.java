package com.moratan251.psitweaks.common.storage.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IdeaStorageDefaultsTest {

    @Test
    void convertsBucketLimitsToInternalAmounts() {
        assertEquals(1_048_576_000L, IdeaStorageDefaults.FLUID_PER_TYPE_RAW);
        assertEquals(33_554_432_000L, IdeaStorageDefaults.CHEMICAL_PER_TYPE_RAW);
    }
}
