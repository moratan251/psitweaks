package com.moratan251.psitweaks.common.storage.idea;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class IdeaStorageWithdrawalTest {
    @Test
    void fullInventoryRestoresOverCapacityEntryAndMarksBothChanges() {
        Map<String, Long> entries = new LinkedHashMap<>(Map.of("stone", 1000L));
        AtomicInteger changes = new AtomicInteger();
        var withdrawal = IdeaStorageWithdrawal.take(entries, "stone", 64, changes::incrementAndGet);
        assertEquals(936L, entries.get("stone"));
        assertEquals(0, IdeaStorageCapacity.simulateInsert(false, true, 1, 256, 936, 64, 64));
        withdrawal.restore(withdrawal.amount());
        assertEquals(1000L, entries.get("stone"));
        assertEquals(2, changes.get());
    }

    @Test
    void partiallyAcceptedTransferConservesItems() {
        Map<String, Long> entries = new LinkedHashMap<>(Map.of("stone", 1000L));
        var withdrawal = IdeaStorageWithdrawal.take(entries, "stone", 64, () -> {});
        long accepted = 10;
        withdrawal.restore(withdrawal.amount() - accepted);
        assertEquals(1000L, entries.get("stone") + accepted);
    }

    @Test
    void failedCraftingRestoresRemovedTypeEvenWhenNewTypeLimitIsExceeded() {
        Map<String, Long> entries = new LinkedHashMap<>(Map.of("stone", 1L, "dirt", 2L, "sand", 3L));
        Map<String, Long> original = Map.copyOf(entries);
        var first = IdeaStorageWithdrawal.take(entries, "stone", 1, () -> {});
        var second = IdeaStorageWithdrawal.take(entries, "dirt", 1, () -> {});
        assertFalse(entries.containsKey("stone"));
        assertEquals(0, IdeaStorageCapacity.simulateInsert(false, false, entries.size(), 1, 0, 64, 1));
        second.restore(second.amount());
        first.restore(first.amount());
        assertEquals(original, entries);
        assertThrows(IllegalArgumentException.class, () -> first.restore(1));
        assertEquals(original, entries);
    }

    @Test
    void clampsWithdrawalAndRejectsExcessiveOrNegativeRefunds() {
        Map<String, Long> entries = new LinkedHashMap<>(Map.of("stone", 1L));
        var withdrawal = IdeaStorageWithdrawal.take(entries, "stone", 64, () -> {});
        assertEquals(1, withdrawal.amount());
        assertThrows(IllegalArgumentException.class, () -> withdrawal.restore(2));
        assertThrows(IllegalArgumentException.class, () -> withdrawal.restore(-1));
        withdrawal.restore(1);
        assertEquals(1L, entries.get("stone"));
    }

    @Test
    void missingResourceDoesNotChangeState() {
        Map<String, Long> entries = new LinkedHashMap<>();
        AtomicInteger changes = new AtomicInteger();
        var withdrawal = IdeaStorageWithdrawal.take(entries, "stone", 64, changes::incrementAndGet);
        assertEquals(0, withdrawal.amount());
        withdrawal.restore(0);
        assertEquals(0, changes.get());
        assertTrue(entries.isEmpty());
    }
}
