package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.GuiProgrammer;

public final class SpellPieceModeButtonOverlay {
    private static final int PANEL_WIDTH = 160;
    private static final int PANEL_MARGIN = 8;
    private static final int OPTION_HEIGHT = 18;
    private static final int OPTION_GAP = 4;
    private static final int OPTION_TOP = 26;
    private static final int MAX_VISIBLE_OPTIONS = 6;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_GAP = 5;
    private static final int GRID_CELL_SIZE = 18;
    private static final int GRID_SIZE = 9;
    private static final int BORDER_COLOR = 0xFF7F1ED4;
    private static final int OUTER_COLOR = 0xF0100018;
    private static final int FILL_COLOR = 0xFF34263D;
    private static final int HOVER_COLOR = 0xFF4B3560;
    private static final int SELECTED_COLOR = 0xFF5E35A5;
    private static final int DISABLED_COLOR = 0xFF242128;
    private static final int TEXT_COLOR = 0xFFEDE8F8;
    private static final int DISABLED_TEXT_COLOR = 0xFFB8ACC8;
    private static final int SCROLLBAR_TRACK_COLOR = 0xFF1A141E;
    private static final int SCROLLBAR_THUMB_COLOR = 0xFF7F1ED4;
    private static int activeX = -1;
    private static int activeY = -1;
    private static int scrollOffset;

    private SpellPieceModeButtonOverlay() {
    }

    public static void render(GuiProgrammer screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PsitweaksModeConfigurable piece = getSelectedConfigurablePiece(screen);
        if (piece == null) {
            return;
        }

        if (isMenuActiveForSelection()) {
            renderMenu(screen, guiGraphics, Minecraft.getInstance().font, piece, mouseX, mouseY);
        }
    }

    public static boolean isActive(GuiProgrammer screen) {
        return getSelectedConfigurablePiece(screen) != null && isMenuActiveForSelection();
    }

    public static boolean handleMousePressedPre(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        PsitweaksModeConfigurable piece = getSelectedConfigurablePiece(screen);
        boolean menuActive = piece != null && isMenuActiveForSelection();
        if (button != 0) {
            return false;
        }

        if ((Screen.hasShiftDown() || Screen.hasControlDown()) && !menuActive) {
            return false;
        }

        if (menuActive) {
            List<PsitweaksModeOption> modes = availableModes(piece);
            PanelLayout panel = panelLayoutFor(screen, modes.size());
            scrollOffset = clampScrollOffset(modes, scrollOffset);
            if (panel.contains(mouseX, mouseY)) {
                if (handleScrollbarClick(panel, modes, mouseX, mouseY)) {
                    return true;
                }

                PsitweaksModeOption selectedMode = modeAt(panel, modes, mouseX, mouseY);
                if (selectedMode != null) {
                    ClientGuiSounds.playClick();
                    if (!screen.isSpectator() && !sameMode(selectedMode, piece.getModeOption())) {
                        screen.pushState(true);
                        piece.setModeOption(selectedMode);
                        screen.onSelectedChanged();
                        screen.onSpellChanged(false);
                    }
                    deactivate();
                    return true;
                }

                deactivate();
                return true;
            }
            if (Screen.hasControlDown()) {
                deactivate();
                return false;
            }
        }

        GridPosition position = gridPositionAt(screen, mouseX, mouseY);
        PsitweaksModeConfigurable clickedPiece = getConfigurablePiece(screen, position);
        if (clickedPiece != null && isSelectedPosition(position)) {
            ClientGuiSounds.playClick();
            if (isMenuActiveForSelection()) {
                deactivate();
            } else {
                activate(position, clickedPiece);
            }
            return true;
        }

        if (isMenuActiveForSelection()) {
            deactivate();
            return true;
        }
        return false;
    }

    public static boolean handleMouseScrolledPre(GuiProgrammer screen, double mouseX, double mouseY, double scrollDeltaY) {
        PsitweaksModeConfigurable piece = getSelectedConfigurablePiece(screen);
        if (piece == null || !isMenuActiveForSelection()) {
            return false;
        }

        List<PsitweaksModeOption> modes = availableModes(piece);
        PanelLayout panel = panelLayoutFor(screen, modes.size());
        if (!panel.contains(mouseX, mouseY)) {
            return false;
        }

        scrollBy(modes, scrollDeltaY > 0.0D ? -1 : 1);
        return true;
    }

    public static boolean handleKeyPressedPre(GuiProgrammer screen, int keyCode) {
        PsitweaksModeConfigurable piece = getSelectedConfigurablePiece(screen);
        if (piece == null || !isMenuActiveForSelection()) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            scrollBy(availableModes(piece), keyCode == GLFW.GLFW_KEY_UP ? -1 : 1);
            return true;
        }
        return false;
    }

    public static void deactivate() {
        activeX = -1;
        activeY = -1;
        scrollOffset = 0;
    }

    private static void renderMenu(
            GuiProgrammer screen,
            GuiGraphics guiGraphics,
            Font font,
            PsitweaksModeConfigurable piece,
            int mouseX,
            int mouseY
    ) {
        List<PsitweaksModeOption> modes = availableModes(piece);
        PanelLayout panel = panelLayoutFor(screen, modes.size());
        scrollOffset = clampScrollOffset(modes, scrollOffset);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 360.0F);
        guiGraphics.fill(panel.x - 1, panel.y - 1, panel.x + panel.width + 1, panel.y + panel.height + 1,
                BORDER_COLOR);
        guiGraphics.fill(panel.x, panel.y, panel.x + panel.width, panel.y + panel.height, OUTER_COLOR);

        Component title = Component.translatable("psitweaks.gui.spell_piece_mode.title");
        guiGraphics.drawString(font, title, panel.x + 8, panel.y + 7, TEXT_COLOR, false);

        int visibleCount = visibleCount(modes.size());
        for (int row = 0; row < visibleCount; row++) {
            int modeIndex = scrollOffset + row;
            if (modeIndex >= modes.size()) {
                break;
            }
            renderModeOption(screen, guiGraphics, font, piece, panel, modes.get(modeIndex), row, mouseX, mouseY);
        }
        if (needsScroll(modes)) {
            renderScrollbar(guiGraphics, panel, modes);
        }
        guiGraphics.pose().popPose();
    }

    private static void renderModeOption(
            GuiProgrammer screen,
            GuiGraphics guiGraphics,
            Font font,
            PsitweaksModeConfigurable piece,
            PanelLayout panel,
            PsitweaksModeOption mode,
            int row,
            int mouseX,
            int mouseY
    ) {
        int x = panel.x + 8;
        int y = optionY(panel, row);
        int width = optionWidth(panel, needsScroll(availableModes(piece)));
        boolean selected = sameMode(mode, piece.getModeOption());
        boolean hovered = contains(x, y, width, OPTION_HEIGHT, mouseX, mouseY);
        int fillColor = screen.isSpectator() && !selected
                ? DISABLED_COLOR
                : selected ? SELECTED_COLOR : hovered ? HOVER_COLOR : FILL_COLOR;
        int textColor = screen.isSpectator() && !selected ? DISABLED_TEXT_COLOR : TEXT_COLOR;
        Component label = Component.translatable(mode.elementTranslationKey());

        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + OPTION_HEIGHT + 1, selected ? BORDER_COLOR : 0xFF1A141E);
        guiGraphics.fill(x, y, x + width, y + OPTION_HEIGHT, fillColor);
        drawClippedString(guiGraphics, font, label, x + 8, y + 5, width - 16, textColor);
    }

    private static PsitweaksModeConfigurable getSelectedConfigurablePiece(GuiProgrammer screen) {
        return getConfigurablePiece(screen, new GridPosition(GuiProgrammer.selectedX, GuiProgrammer.selectedY));
    }

    private static PsitweaksModeConfigurable getConfigurablePiece(GuiProgrammer screen, GridPosition position) {
        if (position == null || screen.spell == null || !SpellGrid.exists(position.x, position.y)) {
            return null;
        }

        SpellPiece piece = screen.spell.grid.gridData[position.x][position.y];
        if (piece instanceof PsitweaksModeConfigurable configurable) {
            return configurable;
        }
        return null;
    }

    private static boolean isMenuActiveForSelection() {
        return activeX == GuiProgrammer.selectedX && activeY == GuiProgrammer.selectedY;
    }

    private static boolean isSelectedPosition(GridPosition position) {
        return position != null && position.x == GuiProgrammer.selectedX && position.y == GuiProgrammer.selectedY;
    }

    private static GridPosition gridPositionAt(GuiProgrammer screen, double mouseX, double mouseY) {
        if (mouseX < screen.gridLeft || mouseY < screen.gridTop) {
            return null;
        }

        int x = (int) ((mouseX - screen.gridLeft) / GRID_CELL_SIZE);
        int y = (int) ((mouseY - screen.gridTop) / GRID_CELL_SIZE);
        if (x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE) {
            return null;
        }

        return new GridPosition(x, y);
    }

    private static void renderScrollbar(GuiGraphics guiGraphics, PanelLayout panel, List<PsitweaksModeOption> modes) {
        int trackX = panel.x + panel.width - PANEL_MARGIN - SCROLLBAR_WIDTH;
        int trackY = panel.y + OPTION_TOP;
        int trackHeight = optionsHeight(visibleCount(modes.size()));
        int maxScroll = maxScrollOffset(modes);
        int thumbHeight = Math.max(10, trackHeight * visibleCount(modes.size()) / modes.size());
        int maxThumbOffset = Math.max(0, trackHeight - thumbHeight);
        int thumbY = trackY + (maxScroll == 0 ? 0 : maxThumbOffset * scrollOffset / maxScroll);

        guiGraphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackHeight, SCROLLBAR_TRACK_COLOR);
        guiGraphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, SCROLLBAR_THUMB_COLOR);
    }

    private static void drawClippedString(
            GuiGraphics guiGraphics,
            Font font,
            Component label,
            int x,
            int y,
            int maxWidth,
            int color
    ) {
        String text = label.getString();
        if (font.width(text) > maxWidth) {
            String ellipsis = "...";
            text = font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width(ellipsis))) + ellipsis;
        }
        guiGraphics.drawString(font, text, x, y, color, false);
    }

    private static void activate(GridPosition position, PsitweaksModeConfigurable piece) {
        activeX = position.x;
        activeY = position.y;
        List<PsitweaksModeOption> modes = availableModes(piece);
        scrollOffset = scrollOffsetFor(modes, piece.getModeOption());
    }

    private static PanelLayout panelLayoutFor(GuiProgrammer screen, int optionCount) {
        int width = Math.min(PANEL_WIDTH, screen.width - PANEL_MARGIN * 2);
        int height = menuHeight(visibleCount(optionCount));
        int x = chooseX(screen, width);
        int y = Math.max(PANEL_MARGIN, Math.min(screen.top + 42, screen.height - height - PANEL_MARGIN));
        return new PanelLayout(x, y, width, height);
    }

    private static int menuHeight(int visibleOptionCount) {
        if (visibleOptionCount <= 0) {
            return OPTION_TOP + PANEL_MARGIN;
        }
        return OPTION_TOP + optionsHeight(visibleOptionCount) + PANEL_MARGIN;
    }

    private static int optionsHeight(int visibleOptionCount) {
        return visibleOptionCount * OPTION_HEIGHT + Math.max(0, visibleOptionCount - 1) * OPTION_GAP;
    }

    private static int chooseX(GuiProgrammer screen, int width) {
        int rightX = screen.left + screen.xSize + 10;
        if (rightX + width <= screen.width - PANEL_MARGIN) {
            return rightX;
        }

        int leftX = screen.left - width - 10;
        if (leftX >= PANEL_MARGIN) {
            return leftX;
        }

        return Math.max(PANEL_MARGIN, (screen.width - width) / 2);
    }

    private static int optionY(PanelLayout panel, int index) {
        return panel.y + OPTION_TOP + index * (OPTION_HEIGHT + OPTION_GAP);
    }

    private static PsitweaksModeOption modeAt(
            PanelLayout panel,
            List<PsitweaksModeOption> modes,
            double mouseX,
            double mouseY
    ) {
        int x = panel.x + 8;
        int width = optionWidth(panel, needsScroll(modes));
        int visibleCount = visibleCount(modes.size());
        for (int row = 0; row < visibleCount; row++) {
            int modeIndex = scrollOffset + row;
            if (modeIndex >= modes.size()) {
                break;
            }
            int y = optionY(panel, row);
            if (contains(x, y, width, OPTION_HEIGHT, mouseX, mouseY)) {
                return modes.get(modeIndex);
            }
        }
        return null;
    }

    private static boolean handleScrollbarClick(
            PanelLayout panel,
            List<PsitweaksModeOption> modes,
            double mouseX,
            double mouseY
    ) {
        if (!needsScroll(modes) || !scrollbarContains(panel, modes, mouseX, mouseY)) {
            return false;
        }

        int trackY = panel.y + OPTION_TOP;
        int trackHeight = optionsHeight(visibleCount(modes.size()));
        double ratio = Math.max(0.0D, Math.min(1.0D, (mouseY - trackY) / trackHeight));
        scrollOffset = clampScrollOffset(modes, (int) Math.round(ratio * maxScrollOffset(modes)));
        return true;
    }

    private static boolean scrollbarContains(
            PanelLayout panel,
            List<PsitweaksModeOption> modes,
            double mouseX,
            double mouseY
    ) {
        int trackX = panel.x + panel.width - PANEL_MARGIN - SCROLLBAR_WIDTH;
        int trackY = panel.y + OPTION_TOP;
        return contains(trackX, trackY, SCROLLBAR_WIDTH, optionsHeight(visibleCount(modes.size())), mouseX, mouseY);
    }

    private static List<PsitweaksModeOption> availableModes(PsitweaksModeConfigurable piece) {
        List<PsitweaksModeOption> modes = piece.getAvailableModeOptions();
        return modes == null ? List.of() : modes;
    }

    private static int optionWidth(PanelLayout panel, boolean hasScrollbar) {
        return panel.width - 16 - (hasScrollbar ? SCROLLBAR_WIDTH + SCROLLBAR_GAP : 0);
    }

    private static boolean needsScroll(List<PsitweaksModeOption> modes) {
        return modes.size() > MAX_VISIBLE_OPTIONS;
    }

    private static int visibleCount(int optionCount) {
        return Math.min(optionCount, MAX_VISIBLE_OPTIONS);
    }

    private static int maxScrollOffset(List<PsitweaksModeOption> modes) {
        return Math.max(0, modes.size() - MAX_VISIBLE_OPTIONS);
    }

    private static int clampScrollOffset(List<PsitweaksModeOption> modes, int offset) {
        return Math.max(0, Math.min(offset, maxScrollOffset(modes)));
    }

    private static void scrollBy(List<PsitweaksModeOption> modes, int amount) {
        scrollOffset = clampScrollOffset(modes, scrollOffset + amount);
    }

    private static int scrollOffsetFor(List<PsitweaksModeOption> modes, PsitweaksModeOption selectedMode) {
        int index = indexOf(modes, selectedMode);
        if (index < 0) {
            return 0;
        }
        if (index < MAX_VISIBLE_OPTIONS) {
            return 0;
        }
        return clampScrollOffset(modes, index - MAX_VISIBLE_OPTIONS + 1);
    }

    private static int indexOf(List<PsitweaksModeOption> modes, PsitweaksModeOption mode) {
        if (mode == null) {
            return -1;
        }

        for (int i = 0; i < modes.size(); i++) {
            if (sameMode(modes.get(i), mode)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean sameMode(PsitweaksModeOption left, PsitweaksModeOption right) {
        return left != null && right != null && left.id().equals(right.id());
    }

    private static boolean contains(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private record GridPosition(int x, int y) {
    }

    private record PanelLayout(int x, int y, int width, int height) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
