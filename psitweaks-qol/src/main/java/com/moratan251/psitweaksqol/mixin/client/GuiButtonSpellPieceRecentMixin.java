package com.moratan251.psitweaksqol.mixin.client;

import com.moratan251.psitweaksqol.client.gui.PieceRecentManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import vazkii.psi.client.gui.button.GuiButtonSpellPiece;

/**
 * ピース一覧のボタンが押されたこと（クリックでも Enter でも {@code onPress} を通る）を、最近使ったピースとして覚える。
 * Psi 110 の {@code GuiButtonSpellPiece} は {@code onPress} を持たないので、上書きではなく継承先への追加になる。
 */
@Mixin(value = GuiButtonSpellPiece.class, remap = false)
public abstract class GuiButtonSpellPieceRecentMixin extends Button {
    private GuiButtonSpellPieceRecentMixin(int x, int y, int width, int height, Component message,
                                           OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    @Override
    public void onPress() {
        PieceRecentManager.recordPlaced(((GuiButtonSpellPiece) (Object) this).getPiece());
        super.onPress();
    }
}
