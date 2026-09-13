package com.moratan251.psitweaks.common.storage.connector;

/** Per-resource limits for one automatic export attempt on each enabled face. */
public record ConnectorExportSettings(int amount, int interval) {
    public static final int TYPES = 4, DEFAULT_INTERVAL = 5, MAX_INTERVAL = 1200;

    public static int type(ConnectorResource.Kind kind) { return kind.ordinal() - 1; }

    public static int maximumAmount(int type) { return type == 0 ? 64 : Integer.MAX_VALUE; }

    public static ConnectorExportSettings defaults(int type) {
        return new ConnectorExportSettings(switch (type) { case 0 -> 64; case 3 -> 16000; default -> 1000; }, DEFAULT_INTERVAL);
    }

    public boolean valid(int type) {
        return type >= 0 && type < TYPES && amount >= 1 && amount <= maximumAmount(type)
                && interval >= 1 && interval <= MAX_INTERVAL;
    }

    public static ConnectorExportSettings read(int type, int[] amounts, int[] intervals, int index) {
        ConnectorExportSettings fallback = defaults(type);
        var settings = new ConnectorExportSettings(index < amounts.length ? amounts[index] : fallback.amount,
                index < intervals.length ? intervals[index] : fallback.interval);
        return settings.valid(type) ? settings : fallback;
    }
}
