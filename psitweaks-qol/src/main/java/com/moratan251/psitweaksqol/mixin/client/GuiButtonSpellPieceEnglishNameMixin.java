package com.moratan251.psitweaksqol.mixin.client;

import com.moratan251.psitweaksqol.client.gui.EnglishTranslations;
import com.moratan251.psitweaksqol.client.gui.PiecePanelSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.client.gui.GuiProgrammer;
import vazkii.psi.client.gui.button.GuiButtonSpellPiece;

/**
 * ピース一覧のツールチップに、英語名を 1 行足す（設定で入り切り）。英語名でも検索できるので、
 * 英語名を覚えていない人が、次から打って探せるようにするため。いまの言語の名前と同じときは足さない。
 */
@Mixin(value = GuiButtonSpellPiece.class, remap = false)
public abstract class GuiButtonSpellPieceEnglishNameMixin {
    @Shadow
    @Final
    GuiProgrammer gui;

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void psitweaks$addEnglishNameToTooltip(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback
    ) {
        GuiButtonSpellPiece self = (GuiButtonSpellPiece) (Object) this;
        // Psi は、カーソルがこのボタンの上にあるときだけ、この描画の中でツールチップを作る。同じ条件で足す。
        if (!self.visible || !self.active
                || mouseX < self.getX() || mouseY < self.getY()
                || mouseX >= self.getX() + self.getWidth() || mouseY >= self.getY() + self.getHeight()
                || gui.tooltip == null || gui.tooltip.isEmpty()
                || !PiecePanelSettings.showEnglishNames()) {
            return;
        }

        String translationKey = self.getPiece().getUnlocalizedName();
        String englishName = EnglishTranslations.get(translationKey);
        if (englishName.equals(translationKey) || englishName.equals(I18n.get(translationKey))) {
            return;
        }

        // 1 行目はピースの名前。そのすぐ下に入れる。
        gui.tooltip.add(1, Component.literal(englishName).withStyle(ChatFormatting.DARK_GRAY));
    }
}
