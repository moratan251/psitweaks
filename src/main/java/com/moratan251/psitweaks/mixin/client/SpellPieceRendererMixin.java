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
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixCuboidRegion;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorMatrixTransformVector;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorRegionFilterBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorListDistanceBase;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtNumberList;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorNbtValue;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorFormatString;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorModeConversionBase;
import com.moratan251.psitweaks.common.spells.spellpiece.operator.PieceOperatorModeListBase;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorStoredValue;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageResourceBase;
import com.moratan251.psitweaks.client.spells.ModeOverlayRenderer;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGravityZone;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickItemTransferBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageResourceBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickShowRegion;

/** Preserves addon overlays after Psi moved rendering out of SpellPiece. */
@Mixin(value = SpellPieceRenderer.class, remap = false)
public abstract class SpellPieceRendererMixin {
    /**
     * Mode badges go on the background plane, before Psi lifts the pose for drawAdditional. The programmer draws
     * the red "!!" compile-error marker at that same depth afterwards, so the marker stays visible over the badge.
     */
    @Inject(
            method = "draw(Lvazkii/psi/api/spell/SpellPiece;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lvazkii/psi/client/render/spell/SpellPieceRenderer;drawBackground(Lvazkii/psi/api/spell/SpellPiece;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void psitweaks$drawModeBadge(SpellPiece piece, PoseStack poses,
                                                MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (piece instanceof PieceMacroCasterAxialOffsetBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorModeConversionBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorModeListBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceSelectorStoredValue custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceTrickItemTransferBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceSelectorIdeaStorageResourceBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceTrickIdeaStorageResourceBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorRegionFilterBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorListDistanceBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceSelectorNbtValue custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceSelectorNbtNumberList custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorMatrixCasterBasis custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorMatrixCuboidRegion custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorMatrixTransformVector custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceTrickShowRegion custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceOperatorBlockResourceBase custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        } else if (piece instanceof PieceTrickGravityZone custom) {
            ModeOverlayRenderer.drawModeOverlay(poses, buffers, light, custom.getModeOption());
        }
    }

    @Inject(method = "drawAdditional", at = @At("TAIL"))
    private static void psitweaks$drawAdditional(SpellPiece piece, PoseStack poses,
                                               MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (piece instanceof PieceConstantString custom) {
            custom.drawAdditional(poses, buffers, light);
        } else if (piece instanceof PieceOperatorFormatString custom) {
            custom.drawAdditional(poses, buffers, light);
        }
    }
}
