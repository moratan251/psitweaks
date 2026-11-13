package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.network.chat.Component;

/**
 * ピース一覧の 1 ページの大きさ。小は Psi の元の広さ（5 列 5 行）。
 * <p>
 * 大の 9 列 7 行は、一覧が魔法構築機のグリッド（9×9 マス）の中に収まる最大。これより広げると、
 * 一覧が名前欄・ヘルプ・書き出しのボタンなどに重なる。それらは一覧のピースより先にクリックを受け取り、
 * 文字はピースの上に描かれるので、重ならない範囲に留める（2026-11-13、12 列 10 行を試して確認）。
 */
public enum PiecePanelSize {
    SMALL("small", 5, 5),
    MEDIUM("medium", 7, 6),
    LARGE("large", 9, 7);

    /** ピース 1 個分のマスの大きさと、一覧の余白。Psi の PiecePanelWidget の配置と同じ値。 */
    public static final int CELL_SIZE = 18;
    public static final int SIDE_PADDING = 5;
    public static final int SEARCH_ROW_HEIGHT = 20;
    public static final int PAGE_ROW_HEIGHT = 15;
    /** 魔法構築機のグリッドの一辺（9 マス × 18）。 */
    public static final int GRID_PIXELS = 162;

    private static final String KEY_PREFIX = "gui.psitweaks_qol.spell_programmer.settings.size.";

    private final String id;
    private final int columns;
    private final int rows;

    PiecePanelSize(String id, int columns, int rows) {
        this.id = id;
        this.columns = columns;
        this.rows = rows;
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
    }

    public int piecesPerPage() {
        return columns * rows;
    }

    /** 一覧の本体（検索欄からページ番号まで。タブの列は含まない）の幅。 */
    public int panelWidth() {
        return SIDE_PADDING * 2 + columns * CELL_SIZE;
    }

    public int panelHeight() {
        return SEARCH_ROW_HEIGHT + rows * CELL_SIZE + PAGE_ROW_HEIGHT;
    }

    /** 設定の選択肢に出す 1 文字（小・中・大 / S・M・L）。 */
    public Component label() {
        return Component.translatable(KEY_PREFIX + id);
    }

    public Component tooltip() {
        return Component.translatable(KEY_PREFIX + "tooltip", columns, rows, piecesPerPage());
    }
}
