package com.moratan251.psitweaks.client.spells;

import com.moratan251.psitweaks.common.spells.SpellWarning;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import vazkii.psi.common.platform.PsiConfig;

/** Shows where a spell warning came from, laid out like Psi's error position line. */
public final class SpellWarningClient {
    private SpellWarningClient() {
    }

    public static void showPosition(int x, int y) {
        String displayX = PsiConfig.client().letterNumberGridCoordinates()
                ? String.valueOf((char) ((x % 27) + 64))
                : String.valueOf(x);
        Minecraft.getInstance().gui.getChat().addMessage(
                Component.translatable(SpellWarning.POSITION, displayX, y).withStyle(ChatFormatting.YELLOW));
    }
}
