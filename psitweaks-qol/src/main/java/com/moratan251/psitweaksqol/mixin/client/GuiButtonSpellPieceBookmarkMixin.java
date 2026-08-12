package com.moratan251.psitweaksqol.mixin.client;

import com.moratan251.psitweaksqol.client.gui.PieceBookmarkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.client.gui.button.GuiButtonSpellPiece;

@Mixin(value = GuiButtonSpellPiece.class, remap = false)
public abstract class GuiButtonSpellPieceBookmarkMixin {
    @Unique
    private static final float PSITWEAKS$BOOKMARK_STAR_SCALE = 0.65F;
    @Unique
    private static final int PSITWEAKS$BOOKMARK_STAR_COLOR = 0xFFFF55;

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void psitweaks$renderBookmarkStar(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback
    ) {
        GuiButtonSpellPiece self = (GuiButtonSpellPiece) (Object) this;
        if (!self.visible || !self.active || !PieceBookmarkManager.isBookmarked(self.getPiece())) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(self.getX(), self.getY() - 1, 300.0F);
        guiGraphics.pose().scale(
                PSITWEAKS$BOOKMARK_STAR_SCALE,
                PSITWEAKS$BOOKMARK_STAR_SCALE,
                1.0F
        );
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                "★",
                0,
                0,
                PSITWEAKS$BOOKMARK_STAR_COLOR,
                true
        );
        guiGraphics.pose().popPose();
    }
}
