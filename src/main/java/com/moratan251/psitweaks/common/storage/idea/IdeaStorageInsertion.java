package com.moratan251.psitweaks.common.storage.idea;

import java.util.function.LongConsumer;

/** Capacity reserved before an external extraction; valid only until this synchronous operation closes. */
public final class IdeaStorageInsertion implements AutoCloseable {
    private final long amount;
    private final LongConsumer commit;
    private final Runnable release;
    private boolean committed, closed;

    IdeaStorageInsertion(long amount, LongConsumer commit, Runnable release) {
        this.amount = amount;
        this.commit = commit;
        this.release = release;
    }

    public long amount() { return amount; }

    public void commit(long transferred) {
        if (closed || committed || transferred < 0 || transferred > amount)
            throw new IllegalArgumentException("Invalid reserved insertion");
        committed = true;
        if (transferred > 0) commit.accept(transferred);
    }

    @Override public void close() {
        if (!closed) { closed = true; release.run(); }
    }
}
