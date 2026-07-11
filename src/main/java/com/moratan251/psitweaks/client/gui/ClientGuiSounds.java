package com.moratan251.psitweaks.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import vazkii.psi.common.core.handler.PsiSoundHandler;

public final class ClientGuiSounds {
    private ClientGuiSounds() {
    }

    public static void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static void playError() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(PsiSoundHandler.compileError, 1.0F));
    }
}
