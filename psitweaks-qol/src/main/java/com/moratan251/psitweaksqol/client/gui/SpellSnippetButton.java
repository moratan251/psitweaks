package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.PsitweaksQol;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.client.gui.GuiProgrammer;

/** 魔法構築機の右下のボタン列に並べるスニペットのボタン。範囲を選んでいれば保存、選んでいなければ一覧を開く。 */
public final class SpellSnippetButton extends Button {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(PsitweaksQol.MOD_ID, "textures/gui/spell_snippet.png");
    private static final int SIZE = 12;
    private static final int TEXTURE_WIDTH = SIZE * 3;

    private final GuiProgrammer screen;

    public SpellSnippetButton(int x, int y, GuiProgrammer screen) {
        super(x, y, SIZE, SIZE, Component.translatable("psitweaks_qol.snippet.button"),
                button -> SpellSnippetController.onButtonPressed(screen), DEFAULT_NARRATION);
        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (screen.takingScreenshot) {
            return;
        }
        // ピース一覧を開いている間は描かない。右寄りのマスで開いた一覧と重なり、一覧のピースを隠してしまう。
        if (screen.panelWidget != null && screen.panelWidget.panelEnabled) {
            active = false;
            return;
        }

        active = SpellSnippetController.canOpen(screen) || SpellSnippetController.isOpen();
        int u = !active ? SIZE * 2 : isHovered() || SpellSnippetController.isOpen() ? SIZE : 0;
        guiGraphics.blit(TEXTURE, getX(), getY(), u, 0, SIZE, SIZE, TEXTURE_WIDTH, SIZE);

        // Psi のボタンと同じく、説明はプログラマーのツールチップ欄に積んで描かせる。
        if (isHovered() && !Screen.hasAltDown() && !SpellSnippetController.isOpen()) {
            screen.tooltip.add(getMessage().copy().withStyle(ChatFormatting.GREEN));
            if (SpellGridMultiSelectionController.hasSelection()) {
                screen.tooltip.add(Component.translatable("psitweaks_qol.snippet.button.save")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                screen.tooltip.add(Component.translatable("psitweaks_qol.snippet.button.list")
                        .withStyle(ChatFormatting.GRAY));
                screen.tooltip.add(Component.translatable("psitweaks_qol.snippet.button.select_hint")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
