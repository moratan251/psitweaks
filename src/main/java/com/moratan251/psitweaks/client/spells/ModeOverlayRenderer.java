package com.moratan251.psitweaks.client.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;
import vazkii.psi.api.ClientPsiAPI;
import org.joml.Matrix4f;
import vazkii.psi.api.spell.SpellPiece;

public final class ModeOverlayRenderer {
    private static final float LEFT = 0.0F;
    private static final float TOP = 0.0F;
    private static final float SIZE = 16.0F;

    private ModeOverlayRenderer() {
    }

    public static void drawModeOverlay(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            PsitweaksModeOption mode
    ) {
        Material material = materialFor(mode);
        VertexConsumer buffer = material.buffer(bufferSource, location -> SpellPiece.getLayer());
        Matrix4f matrix = poseStack.last().pose();
        float right = LEFT + SIZE;
        float bottom = TOP + SIZE;

        buffer.addVertex(matrix, LEFT, bottom, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, 1.0F).setLight(light);
        buffer.addVertex(matrix, right, bottom, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(1.0F, 1.0F).setLight(light);
        buffer.addVertex(matrix, right, TOP, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(1.0F, 0.0F).setLight(light);
        buffer.addVertex(matrix, LEFT, TOP, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, 0.0F).setLight(light);
    }

    private static Material materialFor(PsitweaksModeOption mode) {
        if (mode == null) {
            return PsitweaksClientSpells.MODE_STRING.get();
        }
        return ClientPsiAPI.SPELL_PIECE_MATERIAL_REGISTRY.getOptional(mode.overlayMaterialId())
                .orElse(PsitweaksClientSpells.MODE_STRING.get());
    }
}
