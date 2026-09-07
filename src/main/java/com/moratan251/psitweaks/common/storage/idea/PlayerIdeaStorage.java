package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1プレイヤー分のイデアストレージ(Item / Fluid / Chemical カテゴリ)の有界状態。
 * 量は long 集約し、0 になったエントリは除去する。容量はコンフィグを都度参照する。
 * 変更のたびに version をインクリメントし、dirty コールバックを呼ぶ。
 */
public final class PlayerIdeaStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerIdeaStorage.class);

    public static final int GRID_ROWS_DEFAULT = 4;
    public static final int GRID_ROWS_MIN = 2;
    public static final int GRID_ROWS_MAX = 8;

    private final Map<ItemResourceKey, Long> items = new LinkedHashMap<>();
    private final Map<FluidResourceKey, Long> fluids = new LinkedHashMap<>();
    private final Map<ResourceLocation, Long> chemicals = new LinkedHashMap<>();
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
        return IdeaStorageCapacity.simulateInsert(loadFailed, existing, items.size(), maxItemTypes(),
                items.getOrDefault(key, 0L), perTypeLimit(key), amount);
    }

    public long insert(ItemStack template, long amount) {
        long accepted = simulateInsert(template, amount);
        if (accepted <= 0) {
            return 0;
        }
        ItemResourceKey key = ItemResourceKey.of(template).orElseThrow();
        items.put(key, Math.addExact(items.getOrDefault(key, 0L), accepted));
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
        markChanged();
        return extracted;
    }

    /** For synchronous transfers that may need to return unaccepted items, including over-capacity data. */
    public IdeaStorageWithdrawal<ItemResourceKey> withdrawItem(ItemResourceKey key, long amount) {
        return IdeaStorageWithdrawal.take(items, key, simulateExtract(key, amount), this::markChanged);
    }

    public long simulateInsertFluid(FluidStack template, long amount) {
        if (loadFailed || amount <= 0 || template == null || template.isEmpty()) {
            return 0;
        }
        Optional<FluidResourceKey> keyOptional = FluidResourceKey.of(template);
        if (keyOptional.isEmpty()) {
            return 0;
        }
        FluidResourceKey key = keyOptional.get();
        boolean existing = fluids.containsKey(key);
        return IdeaStorageCapacity.simulateInsert(loadFailed, existing, fluids.size(), maxFluidTypes(),
                fluids.getOrDefault(key, 0L), maxFluidPerType(), amount);
    }

    public long insertFluid(FluidStack template, long amount) {
        long accepted = simulateInsertFluid(template, amount);
        if (accepted <= 0) {
            return 0;
        }
        FluidResourceKey key = FluidResourceKey.of(template).orElseThrow();
        fluids.put(key, Math.addExact(fluids.getOrDefault(key, 0L), accepted));
        markChanged();
        return accepted;
    }

    public long simulateExtractFluid(FluidResourceKey key, long amount) {
        if (loadFailed || amount <= 0 || key == null) {
            return 0;
        }
        return Math.min(amount, fluids.getOrDefault(key, 0L));
    }

    public long extractFluid(FluidResourceKey key, long amount) {
        long extracted = simulateExtractFluid(key, amount);
        if (extracted <= 0) {
            return 0;
        }
        long next = fluids.get(key) - extracted;
        if (next <= 0) {
            fluids.remove(key);
        } else {
            fluids.put(key, next);
        }
        markChanged();
        return extracted;
    }

    /** ItemとFluidを1回の検証・変更として減算する。途中状態やrollback不能を発生させない。 */
    public boolean extractItemAndFluid(ItemResourceKey itemKey, long itemAmount,
                                       FluidResourceKey fluidKey, long fluidAmount) {
        if (simulateExtract(itemKey, itemAmount) != itemAmount
                || simulateExtractFluid(fluidKey, fluidAmount) != fluidAmount) {
            return false;
        }

        long nextItemAmount = items.get(itemKey) - itemAmount;
        if (nextItemAmount == 0) {
            items.remove(itemKey);
        } else {
            items.put(itemKey, nextItemAmount);
        }

        long nextFluidAmount = fluids.get(fluidKey) - fluidAmount;
        if (nextFluidAmount == 0) {
            fluids.remove(fluidKey);
        } else {
            fluids.put(fluidKey, nextFluidAmount);
        }
        markChanged();
        return true;
    }

    public long simulateInsertChemical(ResourceLocation chemicalId, long amount) {
        if (loadFailed || amount <= 0 || chemicalId == null) {
            return 0;
        }
        boolean existing = chemicals.containsKey(chemicalId);
        return IdeaStorageCapacity.simulateInsert(loadFailed, existing, chemicals.size(), maxChemicalTypes(),
                chemicals.getOrDefault(chemicalId, 0L), maxChemicalPerType(), amount);
    }

    public long insertChemical(ResourceLocation chemicalId, long amount) {
        long accepted = simulateInsertChemical(chemicalId, amount);
        if (accepted <= 0) {
            return 0;
        }
        chemicals.put(chemicalId, Math.addExact(chemicals.getOrDefault(chemicalId, 0L), accepted));
        markChanged();
        return accepted;
    }

    public long simulateExtractChemical(ResourceLocation chemicalId, long amount) {
        if (loadFailed || amount <= 0 || chemicalId == null) {
            return 0;
        }
        return Math.min(amount, chemicals.getOrDefault(chemicalId, 0L));
    }

    public long extractChemical(ResourceLocation chemicalId, long amount) {
        long extracted = simulateExtractChemical(chemicalId, amount);
        if (extracted <= 0) {
            return 0;
        }
        long next = chemicals.get(chemicalId) - extracted;
        if (next <= 0) {
            chemicals.remove(chemicalId);
        } else {
            chemicals.put(chemicalId, next);
        }
        markChanged();
        return extracted;
    }

    /**
     * ロード時の復元専用。容量チェックを行わず、超過状態のまま復元する。
     * 重複キーは安全に加算し、overflow 時は警告のうえ大きい方を採用する。
     */
    void loadEntry(ItemResourceKey key, long count) {
        long existing = items.getOrDefault(key, 0L);
        long merged = mergeLoadedAmount("item", key, existing, count);
        items.put(key, merged);
    }

    void loadFluidEntry(FluidResourceKey key, long amount) {
        long existing = fluids.getOrDefault(key, 0L);
        long merged = mergeLoadedAmount("fluid", key, existing, amount);
        fluids.put(key, merged);
    }

    void loadChemicalEntry(ResourceLocation chemicalId, long amount) {
        long existing = chemicals.getOrDefault(chemicalId, 0L);
        long merged = mergeLoadedAmount("chemical", chemicalId, existing, amount);
        chemicals.put(chemicalId, merged);
    }

    private static long mergeLoadedAmount(String category, Object key, long existing, long amount) {
        try {
            return Math.addExact(existing, amount);
        } catch (ArithmeticException overflow) {
            LOGGER.warn("Idea storage {} entry overflow while merging duplicate keys for {}; keeping the larger amount.",
                    category, key);
            return Math.max(existing, amount);
        }
    }

    private void markChanged() {
        version++;
        dirtyCallback.run();
    }

    public List<Map.Entry<ItemResourceKey, Long>> itemEntries() {
        return List.copyOf(items.entrySet());
    }

    public List<Map.Entry<FluidResourceKey, Long>> fluidEntries() {
        return List.copyOf(fluids.entrySet());
    }

    public List<Map.Entry<ResourceLocation, Long>> chemicalEntries() {
        return List.copyOf(chemicals.entrySet());
    }

    public int itemTypeCount() {
        return items.size();
    }

    public int fluidTypeCount() {
        return fluids.size();
    }

    public int chemicalTypeCount() {
        return chemicals.size();
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
        if (loadFailed) {
            return;
        }
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

    public long perTypeLimit(ItemResourceKey key) {
        return key.getMaxStackSize() * (long) itemStacksPerType();
    }

    public int maxFluidTypes() {
        return PsitweaksConfig.COMMON.ideaStorageMaxFluidTypes.get();
    }

    public long maxFluidPerType() {
        return PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.get();
    }

    public int maxChemicalTypes() {
        return PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.get();
    }

    public long maxChemicalPerType() {
        return PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get();
    }

}
