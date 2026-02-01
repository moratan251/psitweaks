package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.common.entities.EntityPhononMaserBeam;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Mekanismレーザー風の緑色ビームレンダラー
 */
public class PhononMaserBeamRenderer extends EntityRenderer<EntityPhononMaserBeam> {

    // ビームの色（緑色）
    private static final int RED = 50;
    private static final int GREEN = 255;
    private static final int BLUE = 50;
    private static final int ALPHA = 200;

    // ビームのコア色（明るい緑）
    private static final int CORE_RED = 150;
    private static final int CORE_GREEN = 255;
    private static final int CORE_BLUE = 150;
    private static final int CORE_ALPHA = 255;

    public PhononMaserBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityPhononMaserBeam entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        Vec3 start = entity.position();
        Vec3 end = entity.getEndPos();
        Vec3 direction = end.subtract(start);
        double length = direction.length();

        if (length < 0.01) {
            return;
        }

        poseStack.pushPose();

        // エンティティ位置からの相対描画のため、原点に移動
        poseStack.translate(-start.x + entity.getX(), -start.y + entity.getY(), -start.z + entity.getZ());

        // 外側のグロー（太いビーム）
        renderBeam(poseStack, buffer, start, end, 0.15f, RED, GREEN, BLUE, ALPHA);

        // 内側のコア（細いビーム、より明るい）
        renderBeam(poseStack, buffer, start, end, 0.05f, CORE_RED, CORE_GREEN, CORE_BLUE, CORE_ALPHA);

        poseStack.popPose();
    }

    private void renderBeam(PoseStack poseStack, MultiBufferSource buffer,
                            Vec3 start, Vec3 end, float width,
                            int r, int g, int b, int a) {

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        Vec3 direction = end.subtract(start).normalize();

        // カメラに向かう直交ベクトルを計算
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(direction.dot(up)) > 0.99) {
            up = new Vec3(1, 0, 0);
        }
        Vec3 perpendicular1 = direction.cross(up).normalize().scale(width);
        Vec3 perpendicular2 = direction.cross(perpendicular1).normalize().scale(width);

        // 4面のビームを描画（ビルボードのような効果）
        drawQuad(vertexConsumer, matrix, normal, start, end, perpendicular1, r, g, b, a);
        drawQuad(vertexConsumer, matrix, normal, start, end, perpendicular1.scale(-1), r, g, b, a);
        drawQuad(vertexConsumer, matrix, normal, start, end, perpendicular2, r, g, b, a);
        drawQuad(vertexConsumer, matrix, normal, start, end, perpendicular2.scale(-1), r, g, b, a);
    }

    private void drawQuad(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                          Vec3 start, Vec3 end, Vec3 offset,
                          int r, int g, int b, int a) {

        // 四角形の4頂点
        Vec3 p1 = start.add(offset);
        Vec3 p2 = start.subtract(offset);
        Vec3 p3 = end.subtract(offset);
        Vec3 p4 = end.add(offset);

        // 三角形1
        addVertex(consumer, matrix, normal, p1, r, g, b, a);
        addVertex(consumer, matrix, normal, p2, r, g, b, a);
        addVertex(consumer, matrix, normal, p3, r, g, b, a);
        addVertex(consumer, matrix, normal, p4, r, g, b, a);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                           Vec3 pos, int r, int g, int b, int a) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880) // フルブライト
                .normal(normal, 0, 1, 0)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPhononMaserBeam entity) {
        return null; // テクスチャは使用しない
    }
}