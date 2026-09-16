package com.moratan251.psitweaks.common.storage.idea;

import java.util.Map;
import java.util.function.LongConsumer;

/** Short-lived receipt for a synchronous transfer. Only withdrawn units may bypass insertion limits. */
public final class IdeaStorageWithdrawal<K> {
    private final LongConsumer restorer;
    private final long amount;
    private long refundable;

    IdeaStorageWithdrawal(long amount, LongConsumer restorer) {
        this.amount = amount;
        this.refundable = amount;
        this.restorer = restorer;
    }

    static <K> IdeaStorageWithdrawal<K> take(Map<K, Long> entries, K key, long requested, Runnable changed) {
        long available = entries.getOrDefault(key, 0L);
        long amount = Math.min(Math.max(0L, requested), available);
        if (amount > 0) {
            if (available == amount) {
                entries.remove(key);
            } else {
                entries.put(key, available - amount);
            }
            changed.run();
        }
        return new IdeaStorageWithdrawal<>(amount, restored -> {
            entries.put(key, Math.addExact(entries.getOrDefault(key, 0L), restored));
            changed.run();
        });
    }

    public long amount() {
        return amount;
    }

    /** Restore only the unaccepted part; neither new capacity limits nor type limits apply. */
    public void restore(long amount) {
        if (amount < 0 || amount > refundable) {
            throw new IllegalArgumentException("Cannot restore more than the outstanding withdrawal");
        }
        if (amount == 0) {
            return;
        }
        restorer.accept(amount);
        refundable -= amount;
    }
}
