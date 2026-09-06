package com.moratan251.psitweaks.common.storage.idea;

import java.util.Map;

/** Short-lived receipt for a synchronous transfer. Only withdrawn units may bypass insertion limits. */
public final class IdeaStorageWithdrawal<K> {
    private final Map<K, Long> entries;
    private final K key;
    private final Runnable changed;
    private final long amount;
    private long refundable;

    private IdeaStorageWithdrawal(Map<K, Long> entries, K key, long amount, Runnable changed) {
        this.entries = entries;
        this.key = key;
        this.amount = amount;
        this.refundable = amount;
        this.changed = changed;
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
        return new IdeaStorageWithdrawal<>(entries, key, amount, changed);
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
        entries.put(key, Math.addExact(entries.getOrDefault(key, 0L), amount));
        refundable -= amount;
        changed.run();
    }
}
