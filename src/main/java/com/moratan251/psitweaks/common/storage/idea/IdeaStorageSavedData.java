package com.moratan251.psitweaks.common.storage.idea;

import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

/**
 * 1プレイヤー分のイデアストレージ永続データ。
 * ファイル名は {@code psitweaks_idea_storage_<uuid>}。
 * DataVersion が自分より新しい場合は読み込まず上書きもせず、loadFailed として扱う。
 */
public final class IdeaStorageSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int CURRENT_DATA_VERSION = 3;

    private static final String TAG_DATA_VERSION = "DataVersion";
    private static final String TAG_GRID_ROWS = "gridRows";
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_ITEM = "item";
    private static final String TAG_FLUIDS = "Fluids";
    private static final String TAG_FLUID = "fluid";
    private static final String TAG_CHEMICALS = "Chemicals";
    private static final String TAG_CHEMICAL = "chemical";
    private static final String TAG_COUNT = "count";

    private final UUID owner;
    private final PlayerIdeaStorage storage;

    private IdeaStorageSavedData(UUID owner) {
        this.owner = owner;
        this.storage = new PlayerIdeaStorage();
        this.storage.setDirtyCallback(this::setDirty);
    }

    public static SavedData.Factory<IdeaStorageSavedData> factory(UUID owner) {
        return new SavedData.Factory<>(
                () -> new IdeaStorageSavedData(owner),
                (tag, registries) -> load(owner, tag, registries)
        );
    }

    private static IdeaStorageSavedData load(UUID owner, CompoundTag tag, HolderLookup.Provider registries) {
        IdeaStorageSavedData data = new IdeaStorageSavedData(owner);

        int dataVersion = tag.getInt(TAG_DATA_VERSION);
        if (dataVersion > CURRENT_DATA_VERSION) {
            LOGGER.warn("Idea storage data for {} uses newer DataVersion {} (current {}); leaving it unread and unmodified.",
                    owner, dataVersion, CURRENT_DATA_VERSION);
            data.storage.markLoadFailed();
            return data;
        }

        // オプションフィールド: 欠落時はデフォルト(DataVersion 据え置きの後方互換)
        if (tag.contains(TAG_GRID_ROWS, Tag.TAG_INT)) {
            data.storage.loadGridRows(tag.getInt(TAG_GRID_ROWS));
        }

        data.storage.loadEnergy(tag.getLong("Energy"));
        ListTag items = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            if (!entry.contains(TAG_ITEM, Tag.TAG_COMPOUND)) {
                LOGGER.warn("Skipping item entry without item data in idea storage of {} (index {}).", owner, i);
                continue;
            }
            Optional<ItemResourceKey> key = ItemResourceKey.parse(registries, entry.get(TAG_ITEM));
            if (key.isEmpty()) {
                LOGGER.warn("Skipping unknown or invalid item entry in idea storage of {} (index {}).", owner, i);
                continue;
            }
            long count = entry.getLong(TAG_COUNT);
            if (count <= 0) {
                LOGGER.warn("Skipping non-positive item entry {} in idea storage of {} (count={}).", key.get(), owner, count);
                continue;
            }
            data.storage.loadEntry(key.get(), count);
        }

        ListTag fluids = tag.getList(TAG_FLUIDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < fluids.size(); i++) {
            CompoundTag entry = fluids.getCompound(i);
            if (!entry.contains(TAG_FLUID, Tag.TAG_COMPOUND)) {
                LOGGER.warn("Skipping fluid entry without fluid data in idea storage of {} (index {}).", owner, i);
                continue;
            }
            Optional<FluidResourceKey> key = FluidResourceKey.parse(registries, entry.get(TAG_FLUID));
            if (key.isEmpty()) {
                LOGGER.warn("Skipping unknown or invalid fluid entry in idea storage of {} (index {}).", owner, i);
                continue;
            }
            long amount = entry.getLong(TAG_COUNT);
            if (amount <= 0) {
                LOGGER.warn("Skipping non-positive fluid entry {} in idea storage of {} (amount={}).",
                        key.get(), owner, amount);
                continue;
            }
            data.storage.loadFluidEntry(key.get(), amount);
        }

        ListTag chemicals = tag.getList(TAG_CHEMICALS, Tag.TAG_COMPOUND);
        for (int i = 0; i < chemicals.size(); i++) {
            CompoundTag entry = chemicals.getCompound(i);
            ResourceLocation chemicalId = ResourceLocation.tryParse(entry.getString(TAG_CHEMICAL));
            if (chemicalId == null) {
                LOGGER.warn("Skipping invalid chemical ID in idea storage of {} (index {}).", owner, i);
                continue;
            }
            long amount = entry.getLong(TAG_COUNT);
            if (amount <= 0) {
                LOGGER.warn("Skipping non-positive chemical entry {} in idea storage of {} (amount={}).",
                        chemicalId, owner, amount);
                continue;
            }
            data.storage.loadChemicalEntry(chemicalId, amount);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(TAG_DATA_VERSION, CURRENT_DATA_VERSION);
        tag.putInt(TAG_GRID_ROWS, storage.getGridRows());
        tag.putLong("Energy", storage.energy());
        ListTag items = new ListTag();
        for (Map.Entry<ItemResourceKey, Long> entry : storage.itemEntries()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put(TAG_ITEM, entry.getKey().save(registries));
            entryTag.putLong(TAG_COUNT, entry.getValue());
            items.add(entryTag);
        }
        tag.put(TAG_ITEMS, items);

        ListTag fluids = new ListTag();
        for (Map.Entry<FluidResourceKey, Long> entry : storage.fluidEntries()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put(TAG_FLUID, entry.getKey().save(registries));
            entryTag.putLong(TAG_COUNT, entry.getValue());
            fluids.add(entryTag);
        }
        tag.put(TAG_FLUIDS, fluids);

        ListTag chemicals = new ListTag();
        for (Map.Entry<ResourceLocation, Long> entry : storage.chemicalEntries()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(TAG_CHEMICAL, entry.getKey().toString());
            entryTag.putLong(TAG_COUNT, entry.getValue());
            chemicals.add(entryTag);
        }
        tag.put(TAG_CHEMICALS, chemicals);
        return tag;
    }

    public PlayerIdeaStorage storage() {
        return storage;
    }

    public UUID owner() {
        return owner;
    }
}
