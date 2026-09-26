package com.moratan251.psitweaks.common.storage.connector;

public enum ConnectorInputMode {
    ANY, ALLOW_LIST, DENY_LIST, EXISTING;

    public ConnectorInputMode next() { return values()[(ordinal() + 1) % values().length]; }

    public static ConnectorInputMode byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : ANY;
    }
}
