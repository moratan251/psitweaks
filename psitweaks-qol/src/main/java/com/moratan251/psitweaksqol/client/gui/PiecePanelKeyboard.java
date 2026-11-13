package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import vazkii.psi.client.gui.GuiProgrammer;
import vazkii.psi.client.gui.widget.PiecePanelWidget;

/**
 * ピース一覧のキーボード操作。検索語を打ったあと、マウスに持ち替えずに候補を選んで置けるようにする。
 * <ul>
 * <li>Tab で次、Shift + Tab で前のピースへ（ページの中で回る）。Psi の「前へ」は Alt + Tab で、Windows に取られて使えない</li>
 * <li>上下キーで 1 段ずつ。ページの端を越えると前後のページへ移る</li>
 * <li>PageUp / PageDown でページ送り</li>
 * <li>Enter でカーソルのピースを置く。Psi はページを数えずに 1 ページ目の同じ位置のピースを置いてしまうので、ここで置く</li>
 * </ul>
 * 左右キーは検索欄の文字の移動に使われるので、取らない。
 */
public final class PiecePanelKeyboard {
    private PiecePanelKeyboard() {
    }

    /** @return このキーを使ったら true（Psi には渡さない） */
    public static boolean handleKeyPressed(GuiProgrammer screen, int keyCode) {
        PiecePanelWidget panel = screen.panelWidget;
        if (panel == null || !panel.panelEnabled || PiecePanelSettings.isSettingsOpen(screen)) {
            return false;
        }

        PiecePanelSize size = PiecePanelSettings.size();
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> pressAtCursor(panel, size);
            case GLFW.GLFW_KEY_TAB -> step(panel, size, Screen.hasShiftDown() || Screen.hasAltDown() ? -1 : 1);
            case GLFW.GLFW_KEY_DOWN -> moveRow(panel, size, 1);
            case GLFW.GLFW_KEY_UP -> moveRow(panel, size, -1);
            case GLFW.GLFW_KEY_PAGE_DOWN -> turnPage(panel, size, 1);
            case GLFW.GLFW_KEY_PAGE_UP -> turnPage(panel, size, -1);
            default -> {
                return false;
            }
        }
        return true;
    }

    /** いまのページに並んでいるピースの数。 */
    private static int countOnPage(PiecePanelWidget panel, PiecePanelSize size) {
        int perPage = size.piecesPerPage();
        return Math.max(0, Math.min(perPage, panel.visibleButtons.size() - panel.page * perPage));
    }

    private static int cursor(PiecePanelWidget panel, int count) {
        return Math.max(0, Math.min(panel.panelCursor, count - 1));
    }

    private static void pressAtCursor(PiecePanelWidget panel, PiecePanelSize size) {
        int count = countOnPage(panel, size);
        if (count > 0) {
            panel.visibleButtons.get(panel.page * size.piecesPerPage() + cursor(panel, count)).onPress();
        }
    }

    /** 同じページの中で前後へ 1 つ。端では反対の端へ回る。 */
    private static void step(PiecePanelWidget panel, PiecePanelSize size, int direction) {
        int count = countOnPage(panel, size);
        if (count > 0) {
            panel.panelCursor = Math.floorMod(cursor(panel, count) + direction, count);
        }
    }

    /** 上下へ 1 段。ページの端を越えたら、前後のページの同じ列へ移る。 */
    private static void moveRow(PiecePanelWidget panel, PiecePanelSize size, int direction) {
        int columns = size.columns();
        int count = countOnPage(panel, size);
        if (count == 0) {
            return;
        }

        int cursor = cursor(panel, count);
        int target = cursor + direction * columns;
        if (target >= 0 && target < count) {
            panel.panelCursor = target;
            return;
        }

        int column = cursor % columns;
        if (direction > 0) {
            // 下の段はあるが、その列までピースが並んでいない（最後の段が半端）ときは、最後のピースへ。
            if (cursor < (count - 1) / columns * columns) {
                panel.panelCursor = count - 1;
            } else if (panel.page < panel.getPageCount() - 1) {
                showPage(panel, panel.page + 1);
                panel.panelCursor = Math.min(column, countOnPage(panel, size) - 1);
            }
        } else if (panel.page > 0) {
            showPage(panel, panel.page - 1);
            int newCount = countOnPage(panel, size);
            panel.panelCursor = Math.min((newCount - 1) / columns * columns + column, newCount - 1);
        }
    }

    /** ページ送り。カーソルはページの中の同じ位置に残す。 */
    private static void turnPage(PiecePanelWidget panel, PiecePanelSize size, int direction) {
        int target = panel.page + direction;
        if (target < 0 || target >= panel.getPageCount()) {
            return;
        }
        int cursor = panel.panelCursor;
        showPage(panel, target);
        panel.panelCursor = Math.max(0, Math.min(cursor, countOnPage(panel, size) - 1));
    }

    /** Psi の並べ直しはカーソルを先頭へ戻すので、呼んだあとで置き直す。 */
    private static void showPage(PiecePanelWidget panel, int page) {
        panel.page = page;
        panel.updatePanelButtons();
    }
}
