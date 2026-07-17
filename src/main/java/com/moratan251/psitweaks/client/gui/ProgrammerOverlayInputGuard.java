package com.moratan251.psitweaks.client.gui;

import vazkii.psi.client.gui.GuiProgrammer;

public final class ProgrammerOverlayInputGuard {
    private static final int LEFT_MOUSE_BUTTON = 0;

    private static boolean leftGestureBlocked;

    private ProgrammerOverlayInputGuard() {
    }

    public static boolean beginMouseGesture(GuiProgrammer screen, int button) {
        if (button != LEFT_MOUSE_BUTTON) {
            return false;
        }

        leftGestureBlocked = isOverlayActive(screen);
        return leftGestureBlocked;
    }

    public static boolean isLeftGestureBlocked() {
        return leftGestureBlocked;
    }

    public static void blockLeftGesture() {
        leftGestureBlocked = true;
    }

    public static void endMouseGesture(int button) {
        if (button == LEFT_MOUSE_BUTTON) {
            leftGestureBlocked = false;
        }
    }

    public static void reset() {
        leftGestureBlocked = false;
    }

    private static boolean isOverlayActive(GuiProgrammer screen) {
        return SpellPieceModeButtonOverlay.isActive(screen)
                || EditableStringInputOverlay.isActive(screen);
    }
}

