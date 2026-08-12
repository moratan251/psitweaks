package com.moratan251.psitweaksqol.client.gui;

import net.neoforged.fml.ModList;
import vazkii.psi.client.gui.GuiProgrammer;

public final class ProgrammerOverlayInputGuard {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final String PSIONIC_UTILITIES_MOD_ID = "psionicutilities";

    private static boolean leftGestureBlocked;
    private static boolean programmerMouseMovedSuppressed;

    private ProgrammerOverlayInputGuard() {
    }

    public static boolean beginMouseGesture(
            GuiProgrammer screen,
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != LEFT_MOUSE_BUTTON) {
            return false;
        }

        leftGestureBlocked = isPsitweaksOverlayActive(screen);
        programmerMouseMovedSuppressed = leftGestureBlocked
                || (isPsionicUtilitiesLoaded()
                && isMouseOverPsiPanel(screen, mouseX, mouseY));
        return leftGestureBlocked;
    }

    public static boolean isLeftGestureBlocked() {
        return leftGestureBlocked;
    }

    public static boolean isProgrammerMouseMovedSuppressed() {
        return programmerMouseMovedSuppressed;
    }

    public static void resetPsionicUtilitiesConnectorStart(GuiProgrammer screen) {
        if (isPsionicUtilitiesLoaded()
                && screen instanceof PsionicUtilitiesProgrammerExtension extension) {
            extension.psitweaks$resetPsionicUtilitiesConnectorStart();
        }
    }

    public static void beginPsionicUtilitiesDragHistory(GuiProgrammer screen) {
        if (isPsionicUtilitiesLoaded()
                && screen instanceof PsionicUtilitiesProgrammerExtension extension) {
            extension.psitweaks$beginPsionicUtilitiesDragHistory();
        }
    }

    public static void finishPsionicUtilitiesDragHistory(GuiProgrammer screen) {
        if (isPsionicUtilitiesLoaded()
                && screen instanceof PsionicUtilitiesProgrammerExtension extension) {
            extension.psitweaks$finishPsionicUtilitiesDragHistory();
        }
    }

    public static void blockLeftGesture() {
        leftGestureBlocked = true;
        programmerMouseMovedSuppressed = true;
    }

    public static void endMouseGesture(int button) {
        if (button == LEFT_MOUSE_BUTTON) {
            leftGestureBlocked = false;
            programmerMouseMovedSuppressed = false;
        }
    }

    public static void reset() {
        leftGestureBlocked = false;
        programmerMouseMovedSuppressed = false;
    }

    private static boolean isPsitweaksOverlayActive(GuiProgrammer screen) {
        return SpellPieceModeButtonOverlay.isActive(screen)
                || EditableStringInputOverlay.isActive(screen);
    }

    private static boolean isMouseOverPsiPanel(GuiProgrammer screen, double mouseX, double mouseY) {
        return (screen.panelWidget != null
                && screen.panelWidget.panelEnabled
                && screen.panelWidget.isMouseOver(mouseX, mouseY))
                || (screen.configWidget != null
                && screen.configWidget.configEnabled
                && screen.configWidget.isMouseOver(mouseX, mouseY));
    }

    private static boolean isPsionicUtilitiesLoaded() {
        return ModList.get().isLoaded(PSIONIC_UTILITIES_MOD_ID);
    }
}
