package com.moratan251.psitweaksqol.client.gui;

import com.moratan251.psitweaksqol.PsitweaksQol;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.client.gui.GuiProgrammer;

/** プログラマー右下のボタン列 (ヘルプ・書き出し・読み込み) に並べる、術式名を作動式名にするボタン。 */
public final class SpellNameFromTrickButton extends Button {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(PsitweaksQol.MOD_ID, "textures/gui/spell_name_from_trick.png");
    private static final int SIZE = 12;
    private static final int TEXTURE_WIDTH = SIZE * 3;

    private final GuiProgrammer screen;

    public SpellNameFromTrickButton(int x, int y, GuiProgrammer screen) {
        super(x, y, SIZE, SIZE, Component.translatable("psitweaks_qol.gui.spell_name_from_trick"),
                button -> SpellNameFromTrickController.onButtonPressed(screen), DEFAULT_NARRATION);
        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (screen.takingScreenshot) {
            return;
        }

        List<String> names = SpellNameFromTrickController.distinctTrickNames(screen);
        active = !names.isEmpty() && SpellNameFromTrickController.canUse(screen);
        int u = !active ? SIZE * 2 : isHovered() || SpellNameFromTrickController.isPicking(screen) ? SIZE : 0;
        guiGraphics.blit(TEXTURE, getX(), getY(), u, 0, SIZE, SIZE, TEXTURE_WIDTH, SIZE);

        // Psi のボタンと同じく、説明はプログラマーのツールチップ欄に積んで描かせる。
        if (isHovered() && !Screen.hasAltDown()) {
            screen.tooltip.add(getMessage().copy().withStyle(ChatFormatting.GREEN));
            if (names.isEmpty()) {
                screen.tooltip.add(Component.translatable("psitweaks_qol.gui.spell_name_from_trick.none")
                        .withStyle(ChatFormatting.RED));
            } else if (names.size() == 1) {
                screen.tooltip.add(Component.translatable("psitweaks_qol.gui.spell_name_from_trick.single", names.getFirst())
                        .withStyle(ChatFormatting.GRAY));
            } else {
                screen.tooltip.add(Component.translatable("psitweaks_qol.gui.spell_name_from_trick.multiple")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
