package com.moratan251.psitweaks.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
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

    private static int anchorX = -1;
    private static int anchorY = -1;
    private static int currentX = -1;
    private static int currentY = -1;
    private static boolean dragging;
    private static Set<GridPosition> selectedPositions = Set.of();
    private static List<ClipboardEntry> clipboard = List.of();

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
        guiGraphics.pose().popPose();
    }

    public static void prepareMousePressed(int button) {
        if (button == LEFT_MOUSE_BUTTON && !Screen.hasShiftDown() && !Screen.hasControlDown()) {
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
            toggleSelection(screen, position);
        }
        return true;
    }

    public static boolean handleMouseDraggedPre(
            GuiProgrammer screen,
            double mouseX,
            double mouseY,
            int button
    ) {
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
                clipboard = List.of();
                return false;
            }
            if (!screen.isSpectator()) {
                copySelection(screen);
            }
            return true;
        }

        if (Screen.isCut(keyCode)) {
            if (selectedPositions.isEmpty()) {
                clipboard = List.of();
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
        List<GridPosition> positions = selectedPositions.stream()
                .filter(position -> getPiece(screen, position) != null)
                .sorted(Comparator.comparingInt(GridPosition::x).thenComparingInt(GridPosition::y))
                .toList();
        if (positions.isEmpty()) {
            clipboard = List.of();
            return false;
        }

        int minX = positions.stream().mapToInt(GridPosition::x).min().orElseThrow();
        int minY = positions.stream().mapToInt(GridPosition::y).min().orElseThrow();
        List<ClipboardEntry> entries = new ArrayList<>(positions.size());
        for (GridPosition position : positions) {
            SpellPiece piece = getPiece(screen, position);
            CompoundTag pieceData = new CompoundTag();
            piece.writeToNBT(pieceData);
            entries.add(new ClipboardEntry(position.x - minX, position.y - minY, pieceData));
        }

        clipboard = List.copyOf(entries);
        GuiProgrammer.clipboard = null;
        return true;
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

        List<PendingPlacement> placements = new ArrayList<>(clipboard.size());
        for (ClipboardEntry entry : clipboard) {
            int targetX = pasteX + entry.offsetX;
            int targetY = pasteY + entry.offsetY;
            if (!SpellGrid.exists(targetX, targetY)) {
                ClientGuiSounds.playError();
                return;
            }

            SpellPiece piece = SpellPiece.createFromNBT(screen.spell, entry.pieceData.copy());
            if (piece == null) {
                return;
            }
            piece.x = targetX;
            piece.y = targetY;
            piece.isInGrid = true;
            placements.add(new PendingPlacement(targetX, targetY, piece));
        }

        if (placements.isEmpty()) {
            return;
        }

        screen.pushState(true);
        for (PendingPlacement placement : placements) {
            screen.spell.grid.gridData[placement.x][placement.y] = placement.piece;
        }
        clearSelection();
        screen.onSpellChanged(false);
    }

    private static boolean canStartSelection(GuiProgrammer screen) {
        return screen.spell != null
                && !screen.commentEnabled
                && (screen.panelWidget == null || !screen.panelWidget.panelEnabled)
                && !isTextInputFocused(screen);
    }

    private static boolean isTextInputFocused(GuiProgrammer screen) {
        return (screen.spellNameField != null && screen.spellNameField.isFocused())
                || (screen.commentField != null && screen.commentField.isFocused());
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

    private record ClipboardEntry(int offsetX, int offsetY, CompoundTag pieceData) {
    }

    private record PendingPlacement(int x, int y, SpellPiece piece) {
    }
}
