package com.moratan251.psitweaks.mixin.client;

import com.moratan251.psitweaks.client.gui.ProgrammerOverlayInputGuard;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.client.gui.GuiProgrammer;

@Mixin(value = GuiProgrammer.class, priority = 900, remap = false)
public abstract class GuiProgrammerOverlayInputMixin {
    @Inject(method = "mouseMoved", at = @At("HEAD"), cancellable = true)
    private void psitweaks$blockOverlayMouseGesture(double mouseX, double mouseY, CallbackInfo callback) {
        if (Screen.hasShiftDown() || ProgrammerOverlayInputGuard.isLeftGestureBlocked()) {
            callback.cancel();
        }
    }
}

