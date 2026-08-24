package com.moratan251.psitweaks.common.storage.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class IdeaStorageContainerTransferBatchTest {

    @Test
    void normalClickStopsAfterFirstSuccessfulTransfer() {
        AtomicInteger attempts = new AtomicInteger();
        assertEquals(1, IdeaStorageContainerTransferBatch.continueAfterFirst(
                false, () -> {
                    attempts.incrementAndGet();
                    return true;
                }));
        assertEquals(0, attempts.get());
    }

    @Test
    void shiftClickContinuesUntilTheNextTransferCannotProgress() {
        AtomicInteger attempts = new AtomicInteger();
        assertEquals(4, IdeaStorageContainerTransferBatch.continueAfterFirst(
                true, () -> attempts.incrementAndGet() <= 3));
        assertEquals(4, attempts.get());
    }

    @Test
    void shiftClickStopsImmediatelyWhenContainerIsAlreadyFullOrEmpty() {
        AtomicInteger attempts = new AtomicInteger();
        assertEquals(1, IdeaStorageContainerTransferBatch.continueAfterFirst(
                true, () -> {
                    attempts.incrementAndGet();
                    return false;
                }));
        assertEquals(1, attempts.get());
    }

    @Test
    void shiftClickHasAServerTickSafetyLimit() {
        AtomicInteger attempts = new AtomicInteger();
        assertEquals(IdeaStorageContainerTransferBatch.MAX_SUCCESSFUL_TRANSFERS,
                IdeaStorageContainerTransferBatch.continueAfterFirst(
                        true, () -> {
                            attempts.incrementAndGet();
                            return true;
                        }));
        assertEquals(IdeaStorageContainerTransferBatch.MAX_SUCCESSFUL_TRANSFERS - 1, attempts.get());
    }
}
