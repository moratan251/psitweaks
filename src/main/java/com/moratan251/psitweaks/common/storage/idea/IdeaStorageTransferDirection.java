package com.moratan251.psitweaks.common.storage.idea;

/** 容器転送planで許可する方向。EITHERは最初の1容器で方向を選ぶ場合だけ使う。 */
public enum IdeaStorageTransferDirection {
    EITHER,
    INTO_STORAGE,
    INTO_CONTAINER;

    public boolean allowsIntoStorage() {
        return this != INTO_CONTAINER;
    }

    public boolean allowsIntoContainer() {
        return this != INTO_STORAGE;
    }

    public static IdeaStorageTransferDirection fixed(boolean intoStorage) {
        return intoStorage ? INTO_STORAGE : INTO_CONTAINER;
    }
}
