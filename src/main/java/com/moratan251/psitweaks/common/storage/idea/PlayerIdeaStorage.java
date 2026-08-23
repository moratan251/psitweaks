package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1プレイヤー分のイデアストレージ(Item カテゴリ)の有界状態。
 * 量は long 集約し、0 になったエントリは除去する。容量はコンフィグを都度参照する。
 * 変更のたびに version をインクリメントし、dirty コールバックを呼ぶ。
 */
public final class PlayerIdeaStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerIdeaStorage.class);

    public static final int GRID_ROWS_DEFAULT = 4;
    public static final int GRID_ROWS_MIN = 2;
    public static final int GRID_ROWS_MAX = 8;

    private final Map<ItemResourceKey, Long> items = new LinkedHashMap<>();
    private long totalItems;
    private long version;
    private boolean loadFailed;
    private int gridRows = GRID_ROWS_DEFAULT;
    private Runnable dirtyCallback = () -> {
    };

    public void setDirtyCallback(Runnable dirtyCallback) {
        this.dirtyCallback = dirtyCallback;
    }

    public long simulateInsert(ItemStack template, long amount) {
        if (loadFailed || amount <= 0 || template == null || template.isEmpty()) {
            return 0;
        }
        Optional<ItemResourceKey> keyOptional = ItemResourceKey.of(template);
        if (keyOptional.isEmpty()) {
            return 0;
        }
        ItemResourceKey key = keyOptional.get();
        boolean existing = items.containsKey(key);
        if (!existing && items.size() >= maxItemTypes()) {
            return 0;
        }
        long current = existing ? items.get(key) : 0L;
        long typeFree = Math.max(0L, perTypeLimit(key) - current);
        long totalFree = Math.max(0L, maxTotalItems() - totalItems);
        return Math.min(amount, Math.min(typeFree, totalFree));
    }

    public long insert(ItemStack template, long amount) {
        long accepted = simulateInsert(template, amount);
        if (accepted <= 0) {
            return 0;
        }
        ItemResourceKey key = ItemResourceKey.of(template).orElseThrow();
        items.put(key, Math.addExact(items.getOrDefault(key, 0L), accepted));
        totalItems = Math.addExact(totalItems, accepted);
        markChanged();
        return accepted;
    }

    public long simulateExtract(ItemResourceKey key, long amount) {
        if (loadFailed || amount <= 0 || key == null) {
            return 0;
        }
        return Math.min(amount, items.getOrDefault(key, 0L));
    }

    public long extract(ItemResourceKey key, long amount) {
        long extracted = simulateExtract(key, amount);
        if (extracted <= 0) {
            return 0;
        }
        long next = items.get(key) - extracted;
        if (next <= 0) {
            items.remove(key);
        } else {
            items.put(key, next);
        }
        totalItems -= extracted;
        markChanged();
        return extracted;
    }

    /**
     * ロード時の復元専用。容量チェックを行わず、超過状態のまま復元する。
     * 重複キーは安全に加算し、overflow 時は警告のうえ大きい方を採用する。
     */
    void loadEntry(ItemResourceKey key, long count) {
        Long existing = items.get(key);
        if (existing == null) {
            items.put(key, count);
            totalItems = Math.addExact(totalItems, count);
            return;
        }
        long merged;
        try {
            merged = Math.addExact(existing, count);
        } catch (ArithmeticException overflow) {
            LOGGER.warn("Idea storage entry overflow while merging duplicate keys for {}; keeping the larger amount.", key);
            merged = Math.max(existing, count);
            totalItems = Math.max(totalItems, Math.addExact(totalItems - existing, merged));
            items.put(key, merged);
            return;
        }
        items.put(key, merged);
        totalItems = Math.addExact(totalItems, count);
    }

    private void markChanged() {
        version++;
        dirtyCallback.run();
    }

    public List<Map.Entry<ItemResourceKey, Long>> itemEntries() {
        return List.copyOf(items.entrySet());
    }

    public int itemTypeCount() {
        return items.size();
    }

    public long totalItems() {
        return totalItems;
    }

    public long getVersion() {
        return version;
    }

    public boolean isLoadFailed() {
        return loadFailed;
    }

    public void markLoadFailed() {
        this.loadFailed = true;
    }

    /** GUI の表示行数。範囲外はクランプする。ロード時復元は dirty を立てない。 */
    public int getGridRows() {
        return gridRows;
    }

    public void setGridRows(int rows) {
        int clamped = Math.max(GRID_ROWS_MIN, Math.min(GRID_ROWS_MAX, rows));
        if (clamped != this.gridRows) {
            this.gridRows = clamped;
            dirtyCallback.run();
        }
    }

    /** ロード時の復元専用。dirty を立てずにセットする。 */
    void loadGridRows(int rows) {
        this.gridRows = Math.max(GRID_ROWS_MIN, Math.min(GRID_ROWS_MAX, rows));
    }

    public int maxItemTypes() {
        return PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.get();
    }

    public int itemStacksPerType() {
        return PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.get();
    }

    public long maxTotalItems() {
        return PsitweaksConfig.COMMON.ideaStorageMaxTotalItems.get();
    }

    public long perTypeLimit(ItemResourceKey key) {
        return key.getMaxStackSize() * (long) itemStacksPerType();
    }
}
