package com.moratan251.psitweaksqol.client.snippet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * 盤面から切り出したピースの並び。ピースのある列・行の最小を 0 にした相対座標と、ピースの NBT
 * （{@code SpellPiece#writeToNBT} の出力）を持つ。空きマスは持たない。
 */
public record SpellPieceLayout(List<Entry> entries, int width, int height) {
    public static final int GRID_SIZE = 9;
    public static final SpellPieceLayout EMPTY = new SpellPieceLayout(List.of(), 0, 0);

    private static final String TAG_KEY = "key";
    private static final String TAG_KEY_LEGACY = "spellKey";

    public SpellPieceLayout {
        entries = List.copyOf(entries);
    }

    /**
     * 並びを作る。座標は最小が 0 になるようにずらし、列・行の順に並べる。同じマスが 2 度出たら先のものを残す。
     * 9×9 に収まらない並びと、ピースの ID がない NBT は受け付けない。
     */
    public static SpellPieceLayout of(List<Entry> rawEntries) {
        if (rawEntries.isEmpty()) {
            return EMPTY;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Entry entry : rawEntries) {
            if (pieceKey(entry.pieceData()).isEmpty()) {
                throw new IllegalArgumentException("Spell piece data has no key");
            }
            minX = Math.min(minX, entry.offsetX());
            minY = Math.min(minY, entry.offsetY());
            maxX = Math.max(maxX, entry.offsetX());
            maxY = Math.max(maxY, entry.offsetY());
        }
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        if (width > GRID_SIZE || height > GRID_SIZE) {
            throw new IllegalArgumentException("Spell piece layout is larger than the grid: " + width + "x" + height);
        }

        Set<Long> occupied = new HashSet<>();
        List<Entry> normalized = new ArrayList<>(rawEntries.size());
        for (Entry entry : rawEntries) {
            int x = entry.offsetX() - minX;
            int y = entry.offsetY() - minY;
            if (occupied.add(((long) x << 32) | y)) {
                normalized.add(new Entry(x, y, entry.pieceData()));
            }
        }
        normalized.sort(Comparator.comparingInt(Entry::offsetX).thenComparingInt(Entry::offsetY));
        return new SpellPieceLayout(normalized, width, height);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int pieceCount() {
        return entries.size();
    }

    /** ピースの NBT から登録 ID の文字列を取り出す。古い形式の {@code spellKey} も読む。 */
    public static String pieceKey(CompoundTag pieceData) {
        if (pieceData.contains(TAG_KEY_LEGACY, Tag.TAG_STRING)) {
            return pieceData.getString(TAG_KEY_LEGACY);
        }
        return pieceData.getString(TAG_KEY);
    }

    public record Entry(int offsetX, int offsetY, CompoundTag pieceData) {
    }
}
