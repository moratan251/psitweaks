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
 * 断面がX字型になるよう2枚の長方形で描画
 */
public class PhononMaserBeamRenderer extends EntityRenderer<EntityPhononMaserBeam> {

    // ビームの色（緑色）
    private static final int RED = 0;
    private static final int GREEN = 255;
    private static final int BLUE = 0;
    private static final int ALPHA = 180;

    // ビームの幅
    private static final float BEAM_WIDTH = 0.15f;

    public PhononMaserBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityPhononMaserBeam entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        Vec3 start = entity.position();
        Vec3 end = entity.getEndPos();
        Vec3 diff = end.subtract(start);
        double length = diff.length();

        if (length < 0.01) {
            return;
        }

        Vec3 direction = diff.normalize();

        poseStack.pushPose();

        // レンダー用のバッファを取得（半透明、発光、両面描画）
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lightning());

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // X字型になるよう2つの直交する軸を計算
        Vec3 perpendicular1 = getPerpendicular(direction);
        Vec3 perpendicular2 = direction.cross(perpendicular1).normalize();

        // 斜め45度のオフセットを計算
        Vec3 offset1 = perpendicular1.add(perpendicular2).normalize().scale(BEAM_WIDTH);
        Vec3 offset2 = perpendicular1.subtract(perpendicular2).normalize().scale(BEAM_WIDTH);

        // 開始・終了位置（エンティティからの相対座標）
        Vec3 relStart = Vec3.ZERO;
        Vec3 relEnd = diff;

        // 1枚目の長方形（表面と裏面）
        drawQuadBothSides(vertexConsumer, matrix, normal, relStart, relEnd, offset1);

        // 2枚目の長方形（表面と裏面）
        drawQuadBothSides(vertexConsumer, matrix, normal, relStart, relEnd, offset2);

        poseStack.popPose();
    }

    /**
     * 方向に対して垂直なベクトルを取得
     */
    private Vec3 getPerpendicular(Vec3 direction) {
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(direction.dot(up)) > 0.9) {
            up = new Vec3(1, 0, 0);
        }
        return direction.cross(up).normalize();
    }

    /**
     * 両面の長方形を描画
     */
    private void drawQuadBothSides(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                   Vec3 start, Vec3 end, Vec3 offset) {
        // 4頂点を計算
        Vec3 v1 = start.add(offset);
        Vec3 v2 = start.subtract(offset);
        Vec3 v3 = end.subtract(offset);
        Vec3 v4 = end.add(offset);

        // 表面
        addVertex(consumer, matrix, normal, v1);
        addVertex(consumer, matrix, normal, v2);
        addVertex(consumer, matrix, normal, v3);
        addVertex(consumer, matrix, normal, v4);

        // 裏面（逆順）
        addVertex(consumer, matrix, normal, v4);
        addVertex(consumer, matrix, normal, v3);
        addVertex(consumer, matrix, normal, v2);
        addVertex(consumer, matrix, normal, v1);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Vec3 pos) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(RED, GREEN, BLUE, ALPHA)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPhononMaserBeam entity) {
        return null;
    }
}