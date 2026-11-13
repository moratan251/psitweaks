package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.client.config.PsitweaksQolConfig;
import net.minecraft.client.gui.components.EditBox;
import vazkii.psi.client.gui.GuiProgrammer;

/** ピース一覧の設定（大きさ・検索内容の保持・右クリックで検索欄のクリア・並び順）。クライアント設定に保存する。 */
public final class PiecePanelSettings {
    /** 検索欄の左にある虫眼鏡の絵の幅。右クリックの当たり判定に含める。 */
    private static final int SEARCH_ICON_WIDTH = 14;
    private static final int SEARCH_ROW_MARGIN = 3;

    /**
     * 最後に一覧を閉じたときの検索欄の文字。ゲームを終了するまで覚える（設定ファイルには書かない）。
     * 魔法構築機を開き直すと検索欄ごと作り直されるので、欄ではなくここに持つ。
     */
    private static String lastSearchText = "";

    private PiecePanelSettings() {
    }

    public static PiecePanelSize size() {
        return PsitweaksQolConfig.CLIENT.piecePanelSize.get();
    }

    public static void setSize(PiecePanelSize size) {
        PsitweaksQolConfig.CLIENT.piecePanelSize.set(size);
        PsitweaksQolConfig.CLIENT_SPEC.save();
    }

    public static boolean keepSearchText() {
        return PsitweaksQolConfig.CLIENT.keepSearchText.get();
    }

    public static void setKeepSearchText(boolean keep) {
        PsitweaksQolConfig.CLIENT.keepSearchText.set(keep);
        PsitweaksQolConfig.CLIENT_SPEC.save();
    }

    public static boolean rightClickClearsSearch() {
        return PsitweaksQolConfig.CLIENT.rightClickClearsSearch.get();
    }

    public static void setRightClickClearsSearch(boolean clear) {
        PsitweaksQolConfig.CLIENT.rightClickClearsSearch.set(clear);
        PsitweaksQolConfig.CLIENT_SPEC.save();
    }

    public static PieceSortOrder sortOrder() {
        return PsitweaksQolConfig.CLIENT.pieceSortOrder.get();
    }

    public static void setSortOrder(PieceSortOrder order) {
        PsitweaksQolConfig.CLIENT.pieceSortOrder.set(order);
        PsitweaksQolConfig.CLIENT_SPEC.save();
    }

    public static void rememberSearchText(String text) {
        lastSearchText = text == null ? "" : text;
    }

    /** 「検索内容の保持」が入のときに、一覧を開いた直後の検索欄へ戻す文字。切のときは空。 */
    public static String searchTextToRestore() {
        return keepSearchText() ? lastSearchText : "";
    }

    /**
     * 「右クリックで検索欄のクリア」。検索欄（虫眼鏡の絵を含む）の上での右クリックなら、打った文字を消して一覧を並べ直す。
     *
     * @return このクリックを使ったら true（Psi には渡さない）
     */
    public static boolean handleSearchRightClick(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        if (button != 1
                || !rightClickClearsSearch()
                || screen.panelWidget == null
                || !screen.panelWidget.panelEnabled) {
            return false;
        }

        EditBox field = screen.panelWidget.searchField;
        if (field == null || !field.isVisible()
                || mouseX < field.getX() - SEARCH_ICON_WIDTH
                || mouseX >= field.getX() + field.getWidth()
                || mouseY < field.getY() - SEARCH_ROW_MARGIN
                || mouseY >= field.getY() + field.getHeight() + SEARCH_ROW_MARGIN) {
            return false;
        }

        if (!field.getValue().isEmpty()) {
            // setValue は Psi の並べ直しを呼ばないので、ここで並べ直す。
            field.setValue("");
            screen.panelWidget.page = 0;
            screen.panelWidget.updatePanelButtons();
            ClientGuiSounds.playClick();
        }
        return true;
    }
}
