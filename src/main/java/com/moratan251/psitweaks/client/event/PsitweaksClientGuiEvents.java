package com.moratan251.psitweaks.client.event;

import com.moratan251.psitweaks.client.gui.EditableStringInputOverlay;
import com.moratan251.psitweaks.client.gui.ProgrammerOverlayInputGuard;
import com.moratan251.psitweaks.client.gui.SpellGridMultiSelectionController;
import com.moratan251.psitweaks.client.gui.SpellPieceModeButtonOverlay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ScreenEvent;
import vazkii.psi.client.gui.GuiProgrammer;

public final class PsitweaksClientGuiEvents {
    private PsitweaksClientGuiEvents() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(PsitweaksClientGuiEvents::onScreenRenderPost);
        eventBus.addListener(PsitweaksClientGuiEvents::onKeyPressedPre);
        eventBus.addListener(PsitweaksClientGuiEvents::onCharacterTypedPre);
        eventBus.addListener(PsitweaksClientGuiEvents::onMouseButtonPressedPre);
        eventBus.addListener(PsitweaksClientGuiEvents::onMouseButtonPressedPost);
        eventBus.addListener(PsitweaksClientGuiEvents::onMouseDraggedPre);
        eventBus.addListener(PsitweaksClientGuiEvents::onMouseButtonReleasedPre);
        eventBus.addListener(PsitweaksClientGuiEvents::onMouseScrolledPre);
        eventBus.addListener(PsitweaksClientGuiEvents::onScreenClosing);
    }

    private static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            SpellGridMultiSelectionController.render(screen, event.getGuiGraphics());
            SpellPieceModeButtonOverlay.render(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
            EditableStringInputOverlay.render(screen, event.getGuiGraphics());
        }
    }

    private static void onKeyPressedPre(ScreenEvent.KeyPressed.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && (SpellPieceModeButtonOverlay.handleKeyPressedPre(screen, event.getKeyCode())
                || EditableStringInputOverlay.handleKeyPressedPre(screen, event.getKeyCode(), event.getScanCode())
                || SpellGridMultiSelectionController.handleKeyPressedPre(screen, event.getKeyCode()))) {
            event.setCanceled(true);
        }
    }

    private static void onCharacterTypedPre(ScreenEvent.CharacterTyped.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && EditableStringInputOverlay.handleCharacterTypedPre(screen, event.getCodePoint(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }

    private static void onMouseButtonPressedPre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            boolean blockedGesture = ProgrammerOverlayInputGuard.beginMouseGesture(screen, event.getButton());
            SpellGridMultiSelectionController.prepareMousePressed(event.getButton());
            boolean handled = SpellPieceModeButtonOverlay.handleMousePressedPre(screen,
                    event.getMouseX(),
                    event.getMouseY(),
                    event.getButton());
            if (!handled) {
                handled = EditableStringInputOverlay.handleMousePressedPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getButton());
            }
            if (!handled) {
                handled = SpellGridMultiSelectionController.handleMousePressedPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getButton());
                if (handled) {
                    SpellGridMultiSelectionController.blockCurrentLeftGesture();
                }
            }
            if (blockedGesture || handled) {
                event.setCanceled(true);
            }
        }
    }

    private static void onMouseButtonPressedPost(ScreenEvent.MouseButtonPressed.Post event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            EditableStringInputOverlay.handleMousePressedPost(screen, event.getMouseX(), event.getMouseY(), event.getButton());
        }
    }

    private static void onMouseDraggedPre(ScreenEvent.MouseDragged.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            boolean handled = EditableStringInputOverlay.handleMouseDraggedPre(screen,
                    event.getMouseX(),
                    event.getMouseY(),
                    event.getMouseButton());
            if (!handled) {
                handled = SpellGridMultiSelectionController.handleMouseDraggedPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getMouseButton());
            }
            if (ProgrammerOverlayInputGuard.isLeftGestureBlocked() || handled) {
                event.setCanceled(true);
            }
        }
    }

    private static void onMouseButtonReleasedPre(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            boolean blockedGesture = ProgrammerOverlayInputGuard.isLeftGestureBlocked();
            boolean handled = EditableStringInputOverlay.handleMouseReleasedPre(screen, event.getButton());
            if (!handled) {
                handled = SpellGridMultiSelectionController.handleMouseReleasedPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getButton());
            }
            ProgrammerOverlayInputGuard.endMouseGesture(event.getButton());
            if (blockedGesture || handled) {
                event.setCanceled(true);
            }
        }
    }

    private static void onMouseScrolledPre(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && (SpellPieceModeButtonOverlay.handleMouseScrolledPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getScrollDeltaY())
                || EditableStringInputOverlay.handleMouseScrolledPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getScrollDeltaY()))) {
            event.setCanceled(true);
        }
    }

    private static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            SpellPieceModeButtonOverlay.deactivate();
            EditableStringInputOverlay.deactivate(screen);
            SpellGridMultiSelectionController.resetScreenState();
            ProgrammerOverlayInputGuard.reset();
        }
    }
}
