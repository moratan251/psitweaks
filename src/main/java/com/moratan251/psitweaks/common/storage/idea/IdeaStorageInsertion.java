package com.moratan251.psitweaks.common.storage.idea;

import java.util.Objects;
import java.util.function.BiPredicate;

/** Capacity reserved before an external extraction; valid only until this synchronous operation closes. */
public final class IdeaStorageInsertion<K> implements AutoCloseable {
    private final K expectedKey;
    private final long amount;
    private final BiPredicate<K, Long> commit;
    private final Runnable release;
    private boolean committed, closed;

    IdeaStorageInsertion(K expectedKey, long amount, BiPredicate<K, Long> commit, Runnable release) {
        this.expectedKey = expectedKey;
        this.amount = amount;
        this.commit = commit;
        this.release = release;
    }

    public long amount() { return amount; }

    /**
     * Own the actual extraction before reporting a broken handler contract. Capacity was checked for
     * the preview, but must never discard a different resource or an excessive amount already taken.
     * Returns false after recovery if the caller must stop and report an abnormal transfer.
     */
    public boolean commit(K actualKey, long transferred) {
        if (closed || committed || transferred < 0)
            throw new IllegalArgumentException("Invalid reserved insertion");
        if (transferred > 0) Objects.requireNonNull(actualKey);
        committed = true;
        if (transferred == 0) return true;
        boolean merged = commit.test(actualKey, transferred);
        return merged && transferred <= amount && expectedKey.equals(actualKey);
    }

    @Override public void close() {
        if (!closed) { closed = true; release.run(); }
    }
}
