package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.common.entities.EntityMeteorLineBeam;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MeteorLineBeamRenderer extends EntityRenderer<EntityMeteorLineBeam> {
    private static final int RED = 80;
    private static final int GREEN = 240;
    private static final int BLUE = 255;
    private static final int ALPHA = 210;
    private static final float BEAM_WIDTH = 0.06F;

    public MeteorLineBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityMeteorLineBeam entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Vec3 start = entity.getStartPos();
        Vec3 end = entity.getEndPos();
        Vec3 diff = end.subtract(start);
        double length = diff.length();

        if (length < 0.01D) {
            return;
        }

        Vec3 direction = diff.normalize();

        poseStack.pushPose();

        Vec3 entityPos = entity.position();
        Vec3 relStart = start.subtract(entityPos);
        Vec3 relEnd = end.subtract(entityPos);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        Vec3 perpendicular1 = getPerpendicular(direction);
        Vec3 perpendicular2 = direction.cross(perpendicular1).normalize();
        Vec3 offset1 = perpendicular1.add(perpendicular2).normalize().scale(BEAM_WIDTH);
        Vec3 offset2 = perpendicular1.subtract(perpendicular2).normalize().scale(BEAM_WIDTH);

        drawQuadBothSides(vertexConsumer, matrix, normal, relStart, relEnd, offset1);
        drawQuadBothSides(vertexConsumer, matrix, normal, relStart, relEnd, offset2);

        poseStack.popPose();
    }

    private Vec3 getPerpendicular(Vec3 direction) {
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        if (Math.abs(direction.dot(up)) > 0.9D) {
            up = new Vec3(1.0D, 0.0D, 0.0D);
        }
        return direction.cross(up).normalize();
    }

    private void drawQuadBothSides(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                   Vec3 start, Vec3 end, Vec3 offset) {
        Vec3 v1 = start.add(offset);
        Vec3 v2 = start.subtract(offset);
        Vec3 v3 = end.subtract(offset);
        Vec3 v4 = end.add(offset);

        addVertex(consumer, matrix, normal, v1);
        addVertex(consumer, matrix, normal, v2);
        addVertex(consumer, matrix, normal, v3);
        addVertex(consumer, matrix, normal, v4);

        addVertex(consumer, matrix, normal, v4);
        addVertex(consumer, matrix, normal, v3);
        addVertex(consumer, matrix, normal, v2);
        addVertex(consumer, matrix, normal, v1);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Vec3 pos) {
        consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(RED, GREEN, BLUE, ALPHA)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMeteorLineBeam entity) {
        return null;
    }
}
