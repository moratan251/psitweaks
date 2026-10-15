package com.moratan251.psitweaks.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.render.spell.SpellPieceRenderer;
import com.moratan251.psitweaks.common.spells.spellpiece.constant.PieceConstantString;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceMacroCasterAxialOffsetBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorBlockResourceBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixCasterBasis;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixTransformVector;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorRegionFilterBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorFormatString;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorModeConversionBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorModeListBase;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorStoredValue;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageResourceBase;
import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickItemTransferBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageResourceBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickShowRegion;

/** Preserves addon overlays after Psi moved rendering out of SpellPiece. */
@Mixin(value = SpellPieceRenderer.class, remap = false)
public abstract class SpellPieceRendererMixin {
    @Inject(method = "drawAdditional", at = @At("TAIL"))
    private static void psitweaks$drawAdditional(SpellPiece piece, PoseStack poses,
                                               MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (piece instanceof PieceConstantString custom) {
            custom.drawAdditional(poses, buffers, light);
        } else if (piece instanceof PieceMacroCasterAxialOffsetBase custom) {
            custom.drawAdditional(poses, buffers, light);
        } else if (piece instanceof PieceOperatorFormatString custom) {
            custom.drawAdditional(poses, buffers, light);
        } else if (piece instanceof PieceOperatorModeConversionBase custom) {
            custom.drawAdditional(poses, buffers, light);
        } else if (piece instanceof PieceOperatorModeListBase custom) {
            custom.drawAdditional(poses, buffers, light);
        } else if (piece instanceof PieceSelectorStoredValue custom) {
            custom.drawAdditional(poses, buffers, light);
        } else if (piece instanceof PieceTrickItemTransferBase custom) {
            custom.drawAdditional(poses, buffers, light);
        } else if (piece instanceof PieceSelectorIdeaStorageResourceBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceTrickIdeaStorageResourceBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorRegionFilterBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorMatrixCasterBasis custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorMatrixTransformVector custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceTrickShowRegion custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorBlockResourceBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        }
    }
}
