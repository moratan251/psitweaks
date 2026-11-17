package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.client.snippet.SpellPieceLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import org.lwjgl.glfw.GLFW;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.GuiProgrammer;

public final class SpellGridMultiSelectionController {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int GRID_SIZE = 9;
    private static final int CELL_SIZE = 18;
    private static final int HIGHLIGHT_FILL_COLOR = 0x604A90E2;
    private static final int HIGHLIGHT_BORDER_COLOR = 0xFF5CB3FF;
    private static final int COPY_ORIGIN_COLOR = 0xFF9AD9FF;
    private static final int COPY_ORIGIN_MARKER_LENGTH = 6;
    private static final int COPY_ORIGIN_MARKER_THICKNESS = 2;

    private static int anchorX = -1;
    private static int anchorY = -1;
    private static int currentX = -1;
    private static int currentY = -1;
    private static boolean dragging;
    private static GridPosition pendingControlClick;
    private static Set<GridPosition> selectedPositions = Set.of();
    private static SpellPieceLayout clipboard = SpellPieceLayout.EMPTY;

    private SpellGridMultiSelectionController() {
    }

    public static void render(GuiProgrammer screen, GuiGraphics guiGraphics) {
        Set<GridPosition> positions = dragging ? positionsInCurrentRectangle(screen) : selectedPositions;
        if (positions.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
        for (GridPosition position : positions) {
            if (getPiece(screen, position) != null) {
                drawHighlight(guiGraphics, screen, position);
            }
        }
        drawSelectionBounds(guiGraphics, screen, positions);
        guiGraphics.pose().popPose();
    }

    public static void prepareMousePressed(int button) {
        if (button != LEFT_MOUSE_BUTTON) {
            return;
        }

        pendingControlClick = null;
        if (!Screen.hasShiftDown() && !Screen.hasControlDown()) {
            clearSelection();
        }
    }

    public static boolean handleMousePressedPre(
            GuiProgrammer screen,
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != LEFT_MOUSE_BUTTON) {
            return false;
        }

        boolean shiftDown = Screen.hasShiftDown();
        boolean controlDown = Screen.hasControlDown();
        if (!shiftDown && !controlDown) {
            return false;
        }

        if (!canStartSelection(screen)) {
            return false;
        }

        GridPosition position = gridPositionAt(screen, mouseX, mouseY);
        if (position == null) {
            return false;
        }

        if (shiftDown) {
            anchorX = position.x;
            anchorY = position.y;
            currentX = position.x;
            currentY = position.y;
            dragging = true;
            selectedPositions = Set.of();
        } else {
            pendingControlClick = position;
            return false;
        }
        return true;
    }

    public static boolean handleMouseDraggedPre(
            GuiProgrammer screen,
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == LEFT_MOUSE_BUTTON && pendingControlClick != null) {
            GridPosition position = gridPositionAt(screen, mouseX, mouseY);
            if (!pendingControlClick.equals(position)) {
                clearSelection();
            }
            return false;
        }

        if (button != LEFT_MOUSE_BUTTON || !dragging) {
            return false;
        }

        GridPosition position = clampedGridPositionAt(screen, mouseX, mouseY);
        currentX = position.x;
        currentY = position.y;
        return true;
    }

    public static boolean handleMouseReleasedPre(
            GuiProgrammer screen,
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == LEFT_MOUSE_BUTTON && pendingControlClick != null) {
            GridPosition pressedPosition = pendingControlClick;
            pendingControlClick = null;
            GridPosition releasedPosition = gridPositionAt(screen, mouseX, mouseY);
            if (pressedPosition.equals(releasedPosition)) {
                toggleSelection(screen, pressedPosition);
                return true;
            }

            clearSelection();
            return false;
        }

        if (button != LEFT_MOUSE_BUTTON || !dragging) {
            return false;
        }

        GridPosition position = clampedGridPositionAt(screen, mouseX, mouseY);
        currentX = position.x;
        currentY = position.y;
        selectedPositions = positionsInCurrentRectangle(screen);
        dragging = false;

        GuiProgrammer.selectedX = anchorX;
        GuiProgrammer.selectedY = anchorY;
        screen.onSelectedChanged();
        return true;
    }

    public static boolean handleKeyPressedPre(GuiProgrammer screen, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && (dragging || !selectedPositions.isEmpty())) {
            clearSelection();
            return true;
        }

        if (isTextInputFocused(screen)) {
            return false;
        }

        if (Screen.hasControlDown()
                && (keyCode == GLFW.GLFW_KEY_Z || keyCode == GLFW.GLFW_KEY_Y)) {
            clearSelection();
            return false;
        }

        if (Screen.isCopy(keyCode)) {
            if (selectedPositions.isEmpty()) {
                clipboard = SpellPieceLayout.EMPTY;
                return false;
            }
            if (!screen.isSpectator()) {
                copySelection(screen);
            }
            return true;
        }

        if (Screen.isCut(keyCode)) {
            if (selectedPositions.isEmpty()) {
                clipboard = SpellPieceLayout.EMPTY;
                return false;
            }
            if (!screen.isSpectator()) {
                cutSelection(screen);
            }
            return true;
        }

        if (Screen.isPaste(keyCode) && !clipboard.isEmpty()) {
            if (!screen.isSpectator()) {
                pasteClipboard(screen);
            }
            return true;
        }

        return false;
    }

    public static void blockCurrentLeftGesture() {
        ProgrammerOverlayInputGuard.blockLeftGesture();
    }

    public static void clearSelection() {
        anchorX = -1;
        anchorY = -1;
        currentX = -1;
        currentY = -1;
        dragging = false;
        pendingControlClick = null;
        selectedPositions = Set.of();
    }

    private static void toggleSelection(GuiProgrammer screen, GridPosition position) {
        if (getPiece(screen, position) == null) {
            return;
        }

        Set<GridPosition> updatedPositions = new LinkedHashSet<>(selectedPositions);
        if (!updatedPositions.add(position)) {
            updatedPositions.remove(position);
        }
        selectedPositions = Set.copyOf(updatedPositions);
    }

    public static void resetScreenState() {
        clearSelection();
    }

    private static boolean copySelection(GuiProgrammer screen) {
        SpellPieceLayout layout = selectionLayout(screen);
        clipboard = layout;
        if (layout.isEmpty()) {
            return false;
        }
        GuiProgrammer.clipboard = null;
        return true;
    }

    private static SpellPieceLayout selectionLayout(GuiProgrammer screen) {
        return snapshotSelection(screen).layout();
    }

    /** 選んでいるピースの並びと、その左上のマスの位置。選んでいなければ並びは空、位置は (-1, -1)。 */
    public static SelectionSnapshot snapshotSelection(GuiProgrammer screen) {
        List<SpellPieceLayout.Entry> entries = new ArrayList<>(selectedPositions.size());
        int originX = Integer.MAX_VALUE;
        int originY = Integer.MAX_VALUE;
        for (GridPosition position : selectedPositions) {
            SpellPiece piece = getPiece(screen, position);
            if (piece != null) {
                CompoundTag pieceData = new CompoundTag();
                piece.writeToNBT(pieceData);
                entries.add(new SpellPieceLayout.Entry(position.x, position.y, pieceData));
                originX = Math.min(originX, position.x);
                originY = Math.min(originY, position.y);
            }
        }
        if (entries.isEmpty()) {
            return new SelectionSnapshot(SpellPieceLayout.EMPTY, -1, -1);
        }
        return new SelectionSnapshot(SpellPieceLayout.of(entries), originX, originY);
    }

    public static boolean hasSelection() {
        return !selectedPositions.isEmpty();
    }

    /** Ctrl+V で貼る並びを差し替える。Psi 本体の 1 ピースのクリップボードは空にする。 */
    public static void setClipboard(SpellPieceLayout layout) {
        clipboard = layout;
        if (!layout.isEmpty()) {
            GuiProgrammer.clipboard = null;
        }
    }

    private static void cutSelection(GuiProgrammer screen) {
        if (!copySelection(screen)) {
            clearSelection();
            return;
        }

        screen.pushState(true);
        for (GridPosition position : selectedPositions) {
            if (SpellGrid.exists(position.x, position.y)) {
                screen.spell.grid.gridData[position.x][position.y] = null;
            }
        }
        clearSelection();
        screen.onSpellChanged(false);
    }

    private static void pasteClipboard(GuiProgrammer screen) {
        int pasteX = GuiProgrammer.selectedX;
        int pasteY = GuiProgrammer.selectedY;
        if (!SpellGrid.exists(pasteX, pasteY)) {
            return;
        }

        if (SpellPieceLayoutPlacer.place(screen, clipboard, pasteX, pasteY)) {
            clearSelection();
        }
    }

    private static boolean canStartSelection(GuiProgrammer screen) {
        return screen.spell != null
                && !screen.commentEnabled
                && (screen.panelWidget == null || !screen.panelWidget.panelEnabled)
                && !isTextInputFocused(screen);
    }

    private static boolean isTextInputFocused(GuiProgrammer screen) {
        // ピース一覧（の検索欄）を開いている間のキーは一覧のもの。見ないと、検索欄での Ctrl+V・Ctrl+X・Ctrl+C が盤面の
        // 貼り付け・切り取り・写しになっていた。
        return (screen.spellNameField != null && screen.spellNameField.isFocused())
                || (screen.commentField != null && screen.commentField.isFocused())
                || (screen.panelWidget != null && (screen.panelWidget.panelEnabled
                        || (screen.panelWidget.searchField != null && screen.panelWidget.searchField.isFocused())));
    }

    private static Set<GridPosition> positionsInCurrentRectangle(GuiProgrammer screen) {
        if (anchorX < 0 || anchorY < 0 || currentX < 0 || currentY < 0) {
            return Set.of();
        }

        int minX = Math.min(anchorX, currentX);
        int maxX = Math.max(anchorX, currentX);
        int minY = Math.min(anchorY, currentY);
        int maxY = Math.max(anchorY, currentY);
        Set<GridPosition> positions = new LinkedHashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                GridPosition position = new GridPosition(x, y);
                if (getPiece(screen, position) != null) {
                    positions.add(position);
                }
            }
        }
        return Set.copyOf(positions);
    }

    private static SpellPiece getPiece(GuiProgrammer screen, GridPosition position) {
        if (screen.spell == null || !SpellGrid.exists(position.x, position.y)) {
            return null;
        }
        return screen.spell.grid.gridData[position.x][position.y];
    }

    private static GridPosition gridPositionAt(GuiProgrammer screen, double mouseX, double mouseY) {
        int x = (int) Math.floor((mouseX - screen.gridLeft) / CELL_SIZE);
        int y = (int) Math.floor((mouseY - screen.gridTop) / CELL_SIZE);
        if (!SpellGrid.exists(x, y)) {
            return null;
        }
        return new GridPosition(x, y);
    }

    private static GridPosition clampedGridPositionAt(GuiProgrammer screen, double mouseX, double mouseY) {
        int x = (int) Math.floor((mouseX - screen.gridLeft) / CELL_SIZE);
        int y = (int) Math.floor((mouseY - screen.gridTop) / CELL_SIZE);
        return new GridPosition(clampToGrid(x), clampToGrid(y));
    }

    private static int clampToGrid(int coordinate) {
        return Math.max(0, Math.min(GRID_SIZE - 1, coordinate));
    }

    private static void drawSelectionBounds(
            GuiGraphics guiGraphics,
            GuiProgrammer screen,
            Set<GridPosition> positions
    ) {
        int minX = GRID_SIZE;
        int minY = GRID_SIZE;
        int maxX = -1;
        int maxY = -1;
        for (GridPosition position : positions) {
            minX = Math.min(minX, position.x);
            minY = Math.min(minY, position.y);
            maxX = Math.max(maxX, position.x);
            maxY = Math.max(maxY, position.y);
        }

        int left = screen.gridLeft + minX * CELL_SIZE - 1;
        int top = screen.gridTop + minY * CELL_SIZE - 1;
        int right = screen.gridLeft + (maxX + 1) * CELL_SIZE - 1;
        int bottom = screen.gridTop + (maxY + 1) * CELL_SIZE - 1;

        guiGraphics.fill(left, top, right, top + 1, HIGHLIGHT_BORDER_COLOR);
        guiGraphics.fill(left, bottom - 1, right, bottom, HIGHLIGHT_BORDER_COLOR);
        guiGraphics.fill(left, top, left + 1, bottom, HIGHLIGHT_BORDER_COLOR);
        guiGraphics.fill(right - 1, top, right, bottom, HIGHLIGHT_BORDER_COLOR);

        guiGraphics.fill(left, top,
                left + COPY_ORIGIN_MARKER_LENGTH, top + COPY_ORIGIN_MARKER_THICKNESS, COPY_ORIGIN_COLOR);
        guiGraphics.fill(left, top,
                left + COPY_ORIGIN_MARKER_THICKNESS, top + COPY_ORIGIN_MARKER_LENGTH, COPY_ORIGIN_COLOR);
    }

    private static void drawHighlight(GuiGraphics guiGraphics, GuiProgrammer screen, GridPosition position) {
        int x = screen.gridLeft + position.x * CELL_SIZE - 1;
        int y = screen.gridTop + position.y * CELL_SIZE - 1;
        guiGraphics.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, HIGHLIGHT_FILL_COLOR);
        guiGraphics.fill(x, y, x + CELL_SIZE, y + 1, HIGHLIGHT_BORDER_COLOR);
        guiGraphics.fill(x, y + CELL_SIZE - 1, x + CELL_SIZE, y + CELL_SIZE, HIGHLIGHT_BORDER_COLOR);
        guiGraphics.fill(x, y, x + 1, y + CELL_SIZE, HIGHLIGHT_BORDER_COLOR);
        guiGraphics.fill(x + CELL_SIZE - 1, y, x + CELL_SIZE, y + CELL_SIZE, HIGHLIGHT_BORDER_COLOR);
    }

    private record GridPosition(int x, int y) {
    }

    public record SelectionSnapshot(SpellPieceLayout layout, int originX, int originY) {
    }

}
