package com.moratan251.psitweaks.common.storage.connector;

public enum ConnectorRedstoneMode {
    ALWAYS, HIGH, LOW;

    public boolean allows(boolean powered) {
        return this == ALWAYS || (this == HIGH) == powered;
    }

    public ConnectorRedstoneMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static ConnectorRedstoneMode byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : ALWAYS;
    }
}
