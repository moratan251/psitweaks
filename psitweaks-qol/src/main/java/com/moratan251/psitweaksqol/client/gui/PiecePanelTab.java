package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.button.GuiButtonSpellPiece;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** ピース一覧の上に並べる絞り込みタブ。同時に選べるのは 1 つで、検索語とは重ねて使える。 */
public enum PiecePanelTab {
    BOOKMARK("bookmark"),
    RECENT("recent"),
    SELECTOR("selector"),
    OPERATOR("operator"),
    TRICK("trick"),
    OTHER("other");

    private static final String KEY_PREFIX = "gui.psitweaks_qol.spell_programmer.tab.";
    public static final String SHOW_ALL_KEY = KEY_PREFIX + "show_all";

    private final String id;

    PiecePanelTab(String id) {
        this.id = id;
    }

    /** ボタンに出す短い印。★と最近は言語によらない記号、種類は言語ごとの 1 文字。 */
    public Component label(boolean selected) {
        return switch (this) {
            case BOOKMARK -> Component.literal(selected ? "★" : "☆");
            case RECENT -> Component.literal("↺");
            default -> Component.translatable(KEY_PREFIX + id + ".label");
        };
    }

    public Component tooltip() {
        return Component.translatable(KEY_PREFIX + id);
    }

    /**
     * Psi が検索語で作った一覧を、このタブに合うものだけに絞る。
     * 最近のタブだけは並びも変える（検索語があっても新しい順）。
     */
    public void filter(List<GuiButtonSpellPiece> buttons) {
        switch (this) {
            case BOOKMARK -> {
                Set<String> bookmarkIds = PieceBookmarkManager.getBookmarkIds();
                buttons.removeIf(button -> !PieceBookmarkManager.isBookmarked(button.getPiece(), bookmarkIds));
            }
            case RECENT -> {
                Map<String, Integer> order = PieceRecentManager.getOrderSnapshot();
                buttons.removeIf(button -> PieceRecentManager.recentIndex(button.getPiece(), order) < 0);
                // List.sort は安定なので、同じピースの派生は Psi の並びのまま残る。
                buttons.sort(Comparator.comparingInt(button ->
                        PieceRecentManager.recentIndex(button.getPiece(), order)));
            }
            default -> buttons.removeIf(button -> !matchesType(button.getPiece()));
        }
    }

    /** 種類タブがそのピースを含むか。★と最近はピースの種類では決まらないので、ここでは扱わない。 */
    public boolean matchesType(SpellPiece piece) {
        EnumPieceType type = piece.getPieceType();
        return switch (this) {
            case SELECTOR -> type == EnumPieceType.SELECTOR;
            case OPERATOR -> type == EnumPieceType.OPERATOR;
            case TRICK -> type == EnumPieceType.TRICK;
            // 定数子・コネクタ・エラー抑止・安全装置など、上の 3 種類に入らないもの全部。
            // 定数子は数が少ないので専用のタブを作らない（2026-11-13 ユーザーの決定）。
            case OTHER -> type != EnumPieceType.SELECTOR
                    && type != EnumPieceType.OPERATOR
                    && type != EnumPieceType.TRICK;
            case BOOKMARK, RECENT -> false;
        };
    }
}
