package com.moratan251.psitweaksqol.mixin.client;

import com.moratan251.psitweaksqol.client.gui.ProgrammerOverlayInputGuard;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.client.gui.GuiProgrammer;

@Mixin(value = GuiProgrammer.class, priority = 900, remap = false)
public abstract class GuiProgrammerOverlayInputMixin {
    // Psi's Forge distribution reobfuscates this inherited Minecraft method to its SRG name.
    @Inject(method = { "mouseMoved", "m_94757_" }, at = @At("HEAD"), cancellable = true)
    private void psitweaks$blockOverlayMouseGesture(double mouseX, double mouseY, CallbackInfo callback) {
        if (Screen.hasShiftDown() || ProgrammerOverlayInputGuard.isProgrammerMouseMovedSuppressed()) {
            callback.cancel();
        }
    }
}
