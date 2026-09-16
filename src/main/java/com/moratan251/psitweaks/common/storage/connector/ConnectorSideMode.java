package com.moratan251.psitweaks.common.storage.connector;

public enum ConnectorSideMode {
    BOTH(true, true), INPUT(true, false), OUTPUT(false, true), DISABLED(false, false);

    public final boolean input;
    public final boolean output;

    ConnectorSideMode(boolean input, boolean output) {
        this.input = input;
        this.output = output;
    }

    public ConnectorSideMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static ConnectorSideMode byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : DISABLED;
    }
}
