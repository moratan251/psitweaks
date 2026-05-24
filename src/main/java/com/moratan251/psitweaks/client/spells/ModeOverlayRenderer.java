package com.moratan251.psitweaks.client.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;
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
            ListElementMode mode
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

    private static Material materialFor(ListElementMode mode) {
        return switch (mode) {
            case ENTITY -> PsitweaksClientSpells.MODE_ENTITY.get();
            case ITEM -> PsitweaksClientSpells.MODE_ITEM.get();
            case NUMBER -> PsitweaksClientSpells.MODE_NUMBER.get();
            case STRING -> PsitweaksClientSpells.MODE_STRING.get();
            case VECTOR -> PsitweaksClientSpells.MODE_VECTOR.get();
        };
    }
}
