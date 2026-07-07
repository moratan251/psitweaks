package com.moratan251.psitweaks.client.event;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.EditableStringInputOverlay;
import com.moratan251.psitweaks.client.gui.SpellPieceModeButtonOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.psi.client.gui.GuiProgrammer;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, value = Dist.CLIENT)
public final class PsitweaksClientGuiEvents {
    private PsitweaksClientGuiEvents() {
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            SpellPieceModeButtonOverlay.render(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
            EditableStringInputOverlay.render(screen, event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onKeyPressedPre(ScreenEvent.KeyPressed.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && (SpellPieceModeButtonOverlay.handleKeyPressedPre(screen, event.getKeyCode())
                || EditableStringInputOverlay.handleKeyPressedPre(screen, event.getKeyCode(), event.getScanCode()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCharacterTypedPre(ScreenEvent.CharacterTyped.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && EditableStringInputOverlay.handleCharacterTypedPre(screen, event.getCodePoint(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseButtonPressedPre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && (SpellPieceModeButtonOverlay.handleMousePressedPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getButton())
                || EditableStringInputOverlay.handleMousePressedPre(screen, event.getMouseX(), event.getMouseY(), event.getButton()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseButtonPressedPost(ScreenEvent.MouseButtonPressed.Post event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            EditableStringInputOverlay.handleMousePressedPost(screen, event.getMouseX(), event.getMouseY(), event.getButton());
        }
    }

    @SubscribeEvent
    public static void onMouseDraggedPre(ScreenEvent.MouseDragged.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && EditableStringInputOverlay.handleMouseDraggedPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getMouseButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseButtonReleasedPre(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && EditableStringInputOverlay.handleMouseReleasedPre(screen, event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScrolledPre(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() instanceof GuiProgrammer screen
                && (SpellPieceModeButtonOverlay.handleMouseScrolledPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getScrollDelta())
                || EditableStringInputOverlay.handleMouseScrolledPre(screen,
                        event.getMouseX(),
                        event.getMouseY(),
                        event.getScrollDelta()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof GuiProgrammer screen) {
            SpellPieceModeButtonOverlay.deactivate();
            EditableStringInputOverlay.deactivate(screen);
        }
    }
}