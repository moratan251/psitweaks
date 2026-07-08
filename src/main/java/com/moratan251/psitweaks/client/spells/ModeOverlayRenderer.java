package com.moratan251.psitweaks.client.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class ModeOverlayRenderer {
    private static final float LEFT = 0.0F;
    private static final float TOP = 0.0F;
    private static final float SIZE = 16.0F;
    private static final ResourceLocation FALLBACK_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Psitweaks.MOD_ID, "textures/spell/mode/string.png");

    private ModeOverlayRenderer() {
    }

    public static void drawModeOverlay(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            PsitweaksModeOption mode
    ) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(textureFor(mode)));
        Matrix4f matrix = poseStack.last().pose();
        float right = LEFT + SIZE;
        float bottom = TOP + SIZE;

        vertex(buffer, matrix, LEFT, bottom, 0.0F, 0.0F, 1.0F, light);
        vertex(buffer, matrix, right, bottom, 0.0F, 1.0F, 1.0F, light);
        vertex(buffer, matrix, right, TOP, 0.0F, 1.0F, 0.0F, light);
        vertex(buffer, matrix, LEFT, TOP, 0.0F, 0.0F, 0.0F, light);
    }

    private static ResourceLocation textureFor(PsitweaksModeOption mode) {
        if (mode == null) {
            return FALLBACK_TEXTURE;
        }
        ResourceLocation overlayMaterialId = mode.overlayMaterialId();
        return ResourceLocation.fromNamespaceAndPath(
                overlayMaterialId.getNamespace(), "textures/spell/" + overlayMaterialId.getPath() + ".png");
    }

    private static void vertex(
            VertexConsumer buffer,
            Matrix4f matrix,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light
    ) {
        buffer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .uv2(light)
                .endVertex();
    }
}
