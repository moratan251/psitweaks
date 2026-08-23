package com.moratan251.psitweaks.common.storage.idea;

import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * 所有者 UUID から個人イデアストレージを取得する唯一の入口。
 * キャッシュは Overworld の DimensionDataStorage に任せ、独自の static キャッシュは持たない。
 */
public final class IdeaStorageService {
    private static final String FILE_PREFIX = "psitweaks_idea_storage_";

    private IdeaStorageService() {
    }

    public static PlayerIdeaStorage get(MinecraftServer server, UUID owner) {
        DimensionDataStorage dataStorage = server.overworld().getDataStorage();
        return dataStorage.computeIfAbsent(IdeaStorageSavedData.factory(owner), fileName(owner)).storage();
    }

    public static String fileName(UUID owner) {
        return FILE_PREFIX + owner;
    }
}
