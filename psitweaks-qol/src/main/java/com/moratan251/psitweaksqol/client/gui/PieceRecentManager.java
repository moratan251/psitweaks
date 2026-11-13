package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.client.config.PsitweaksQolConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.GuiProgrammer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** ピース一覧から置いたピースを新しい順に覚える。保存先はクライアント設定で、書き込みは画面を閉じるときにまとめる。 */
public final class PieceRecentManager {
    /** 一覧の 2 ページ分。これより古いものは捨てる。 */
    public static final int MAX_RECENT_PIECES = 50;

    private static volatile List<? extends String> cachedConfigValues;
    /** ピース ID から新しさの順位（0 が最新）への対応。 */
    private static volatile Map<String, Integer> cachedOrder = Map.of();
    private static boolean dirty;

    private PieceRecentManager() {
    }

    /** 一覧のボタンが押されたときに呼ぶ。観戦中は Psi が何も置かないので覚えない。 */
    public static void recordPlaced(SpellPiece piece) {
        if (!(Minecraft.getInstance().screen instanceof GuiProgrammer screen) || screen.isSpectator()) {
            return;
        }

        ResourceLocation pieceId = piece.getRegistryKey();
        if (pieceId == null) {
            return;
        }

        String pieceIdValue = pieceId.toString();
        Map<String, Integer> order = getOrderSnapshot();
        Integer current = order.get(pieceIdValue);
        if (current != null && current == 0) {
            return;
        }

        List<String> values = new ArrayList<>(Math.min(order.size() + 1, MAX_RECENT_PIECES));
        values.add(pieceIdValue);
        for (String value : order.keySet()) {
            if (values.size() >= MAX_RECENT_PIECES) {
                break;
            }
            if (!value.equals(pieceIdValue)) {
                values.add(value);
            }
        }

        PsitweaksQolConfig.CLIENT.recentPieces.set(values);
        cachedConfigValues = null;
        dirty = true;
    }

    /** 新しさの順位（0 が最新）。最近使っていないピースは -1。 */
    public static int recentIndex(SpellPiece piece, Map<String, Integer> order) {
        ResourceLocation pieceId = piece.getRegistryKey();
        if (pieceId == null) {
            return -1;
        }
        return order.getOrDefault(pieceId.toString(), -1);
    }

    public static Map<String, Integer> getOrderSnapshot() {
        List<? extends String> configValues = PsitweaksQolConfig.CLIENT.recentPieces.get();
        if (configValues == cachedConfigValues) {
            return cachedOrder;
        }
        return rebuildOrderSnapshot(configValues);
    }

    private static synchronized Map<String, Integer> rebuildOrderSnapshot(List<? extends String> configValues) {
        if (configValues == cachedConfigValues) {
            return cachedOrder;
        }
        Map<String, Integer> order = new LinkedHashMap<>();
        for (String value : configValues) {
            if (order.size() >= MAX_RECENT_PIECES) {
                break;
            }
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id != null) {
                order.putIfAbsent(id.toString(), order.size());
            }
        }
        cachedOrder = Collections.unmodifiableMap(order);
        cachedConfigValues = configValues;
        return cachedOrder;
    }

    /** 覚えた内容を設定ファイルへ書く。変更がなければ何もしない。 */
    public static void flush() {
        if (!dirty) {
            return;
        }
        dirty = false;
        PsitweaksQolConfig.CLIENT_SPEC.save();
    }
}
