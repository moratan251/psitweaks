package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.gui.GuiProgrammer;

public final class SpellPieceModeButtonOverlay {
    private static final int PANEL_WIDTH = 160;
    private static final int PANEL_HEIGHT = 92;
    private static final int PANEL_MARGIN = 8;
    private static final int OPTION_HEIGHT = 18;
    private static final int OPTION_GAP = 4;
    private static final int OPTION_TOP = 26;
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
    private static int activeX = -1;
    private static int activeY = -1;

    private SpellPieceModeButtonOverlay() {
    }

    public static void render(GuiProgrammer screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ModeConfigurableSpellPiece piece = getSelectedConfigurablePiece(screen);
        if (piece == null) {
            return;
        }

        if (isMenuActiveForSelection()) {
            renderMenu(screen, guiGraphics, Minecraft.getInstance().font, piece, mouseX, mouseY);
        }
    }

    public static boolean handleMousePressedPre(GuiProgrammer screen, double mouseX, double mouseY, int button) {
        ModeConfigurableSpellPiece piece = getSelectedConfigurablePiece(screen);
        if (button != 0) {
            return false;
        }

        if (piece != null && isMenuActiveForSelection()) {
            PanelLayout panel = panelLayoutFor(screen);
            if (panel.contains(mouseX, mouseY)) {
                ListElementMode selectedMode = modeAt(panel, mouseX, mouseY);
                if (selectedMode != null) {
                    ClientGuiSounds.playClick();
                    if (!screen.isSpectator() && selectedMode != piece.getElementMode()) {
                        screen.pushState(true);
                        piece.setElementMode(selectedMode);
                        screen.onSelectedChanged();
                        screen.onSpellChanged(false);
                    }
                    deactivate();
                    return true;
                }

                deactivate();
                return false;
            }
        }

        GridPosition position = gridPositionAt(screen, mouseX, mouseY);
        ModeConfigurableSpellPiece clickedPiece = getConfigurablePiece(screen, position);
        if (clickedPiece != null && isSelectedPosition(position)) {
            ClientGuiSounds.playClick();
            if (isMenuActiveForSelection()) {
                deactivate();
            } else {
                activeX = position.x;
                activeY = position.y;
            }
            return true;
        }

        if (isMenuActiveForSelection()) {
            deactivate();
        }
        return false;
    }

    public static void deactivate() {
        activeX = -1;
        activeY = -1;
    }

    private static void renderMenu(
            GuiProgrammer screen,
            GuiGraphics guiGraphics,
            Font font,
            ModeConfigurableSpellPiece piece,
            int mouseX,
            int mouseY
    ) {
        PanelLayout panel = panelLayoutFor(screen);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 360.0F);
        guiGraphics.fill(panel.x - 1, panel.y - 1, panel.x + panel.width + 1, panel.y + panel.height + 1,
                BORDER_COLOR);
        guiGraphics.fill(panel.x, panel.y, panel.x + panel.width, panel.y + panel.height, OUTER_COLOR);

        Component title = Component.translatable("psitweaks.gui.spell_piece_mode.title");
        guiGraphics.drawString(font, title, panel.x + 8, panel.y + 7, TEXT_COLOR, false);

        for (ListElementMode mode : ListElementMode.values()) {
            renderModeOption(screen, guiGraphics, font, piece, panel, mode, mouseX, mouseY);
        }
        guiGraphics.pose().popPose();
    }

    private static void renderModeOption(
            GuiProgrammer screen,
            GuiGraphics guiGraphics,
            Font font,
            ModeConfigurableSpellPiece piece,
            PanelLayout panel,
            ListElementMode mode,
            int mouseX,
            int mouseY
    ) {
        int x = panel.x + 8;
        int y = optionY(panel, mode);
        int width = panel.width - 16;
        boolean selected = mode == piece.getElementMode();
        boolean hovered = contains(x, y, width, OPTION_HEIGHT, mouseX, mouseY);
        int fillColor = screen.isSpectator() && !selected
                ? DISABLED_COLOR
                : selected ? SELECTED_COLOR : hovered ? HOVER_COLOR : FILL_COLOR;
        int textColor = screen.isSpectator() && !selected ? DISABLED_TEXT_COLOR : TEXT_COLOR;
        Component label = Component.translatable(mode.elementTranslationKey());

        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + OPTION_HEIGHT + 1, selected ? BORDER_COLOR : 0xFF1A141E);
        guiGraphics.fill(x, y, x + width, y + OPTION_HEIGHT, fillColor);
        guiGraphics.drawString(font, label, x + 8, y + 5, textColor, false);
    }

    private static ModeConfigurableSpellPiece getSelectedConfigurablePiece(GuiProgrammer screen) {
        return getConfigurablePiece(screen, new GridPosition(GuiProgrammer.selectedX, GuiProgrammer.selectedY));
    }

    private static ModeConfigurableSpellPiece getConfigurablePiece(GuiProgrammer screen, GridPosition position) {
        if (position == null || screen.spell == null || !SpellGrid.exists(position.x, position.y)) {
            return null;
        }

        SpellPiece piece = screen.spell.grid.gridData[position.x][position.y];
        if (piece instanceof ModeConfigurableSpellPiece configurable) {
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

    private static PanelLayout panelLayoutFor(GuiProgrammer screen) {
        int width = Math.min(PANEL_WIDTH, screen.width - PANEL_MARGIN * 2);
        int height = PANEL_HEIGHT;
        int x = chooseX(screen, width);
        int y = Math.max(PANEL_MARGIN, Math.min(screen.top + 42, screen.height - height - PANEL_MARGIN));
        return new PanelLayout(x, y, width, height);
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

    private static int optionY(PanelLayout panel, ListElementMode mode) {
        return panel.y + OPTION_TOP + mode.ordinal() * (OPTION_HEIGHT + OPTION_GAP);
    }

    private static ListElementMode modeAt(PanelLayout panel, double mouseX, double mouseY) {
        int x = panel.x + 8;
        int width = panel.width - 16;
        for (ListElementMode mode : ListElementMode.values()) {
            int y = optionY(panel, mode);
            if (contains(x, y, width, OPTION_HEIGHT, mouseX, mouseY)) {
                return mode;
            }
        }
        return null;
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
