package com.moratan251.psitweaks.common.storage.connector;

/** Per-resource limits for one automatic export attempt on each enabled face. */
public record ConnectorExportSettings(int amount, int interval, long minimumStock, long targetStock) {
    public static final int TYPES = 4, DEFAULT_INTERVAL = 5, MAX_INTERVAL = 1200;

    public ConnectorExportSettings(int amount, int interval) { this(amount, interval, 0, -1); }

    public static int type(ConnectorResource.Kind kind) { return kind.ordinal() - 1; }

    public static int maximumAmount(int type) { return type == 0 ? 64 : Integer.MAX_VALUE; }

    public static ConnectorExportSettings defaults(int type) {
        return new ConnectorExportSettings(switch (type) { case 0 -> 64; case 3 -> 16000; default -> 1000; }, DEFAULT_INTERVAL);
    }

    public boolean valid(int type) {
        return type >= 0 && type < TYPES && amount >= 1 && amount <= maximumAmount(type)
                && interval >= 1 && interval <= MAX_INTERVAL && minimumStock >= 0 && targetStock >= -1;
    }

    /** A start threshold, not a reserved balance. A target of -1 disables destination counting. */
    public int limit(long sourceStock, long destinationStock) {
        if (sourceStock < minimumStock || sourceStock <= 0) return 0;
        long maximum = Math.min(amount, sourceStock);
        if (targetStock >= 0) maximum = Math.min(maximum, Math.max(0, targetStock - Math.max(0, destinationStock)));
        return (int) maximum;
    }

    public static ConnectorExportSettings read(int type, int[] amounts, int[] intervals, int index) {
        return read(type, amounts, intervals, new long[0], new long[0], index);
    }

    public static ConnectorExportSettings read(int type, int[] amounts, int[] intervals, long[] minimums, long[] targets, int index) {
        ConnectorExportSettings fallback = defaults(type);
        var settings = new ConnectorExportSettings(index < amounts.length ? amounts[index] : fallback.amount,
                index < intervals.length ? intervals[index] : fallback.interval,
                index < minimums.length ? minimums[index] : 0, index < targets.length ? targets[index] : -1);
        return settings.valid(type) ? settings : fallback;
    }
}
