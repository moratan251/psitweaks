package com.moratan251.psitweaks.client.event;

import com.moratan251.psitweaks.client.gui.StringConstantInputOverlay;
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
        eventBus.addListener(PsitweaksClientGuiEvents::onScreenClosing);
    }

    private static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            StringConstantInputOverlay.render(screen, event.getGuiGraphics());
        }
    }

    private static void onKeyPressedPre(ScreenEvent.KeyPressed.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && StringConstantInputOverlay.handleKeyPressedPre(screen, event.getKeyCode(), event.getScanCode())) {
            event.setCanceled(true);
        }
    }

    private static void onCharacterTypedPre(ScreenEvent.CharacterTyped.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && StringConstantInputOverlay.handleCharacterTypedPre(screen, event.getCodePoint(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }

    private static void onMouseButtonPressedPre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && StringConstantInputOverlay.handleMousePressedPre(screen, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    private static void onMouseButtonPressedPost(ScreenEvent.MouseButtonPressed.Post event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            StringConstantInputOverlay.handleMousePressedPost(screen, event.getMouseX(), event.getMouseY(), event.getButton());
        }
    }

    private static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            StringConstantInputOverlay.deactivate(screen);
        }
    }
}
