package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.common.entities.EntityMolecularDivider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 分子ディバイダーの三角形領域を描画するレンダラー
 */
public class MolecularDividerRenderer extends EntityRenderer<EntityMolecularDivider> {

    // 三角形の色（シアン系）
    private static final int RED = 0;
    private static final int GREEN = 200;
    private static final int BLUE = 255;

    // 辺の色（より明るい）
    private static final int EDGE_RED = 100;
    private static final int EDGE_GREEN = 255;
    private static final int EDGE_BLUE = 255;

    // 辺の幅
    private static final float EDGE_WIDTH = 0.08f;

    public MolecularDividerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityMolecularDivider entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        Vec3 v1 = entity.getVertex1();
        Vec3 v2 = entity.getVertex2();
        Vec3 v3 = entity.getVertex3();

        // 座標が初期化されていない場合はスキップ
        if (v1.lengthSqr() < 0.001 && v2.lengthSqr() < 0.001 && v3.lengthSqr() < 0.001) {
            return;
        }

        // フェードアウト用のアルファ値
        float alpha = entity.getAlphaProgress();
        int fillAlpha = (int) (80 * alpha);   // 内部の透明度
        int edgeAlpha = (int) (220 * alpha);  // 辺の透明度

        poseStack.pushPose();

        // エンティティ位置からの相対座標で描画
        Vec3 entityPos = entity.position();
        Vec3 relV1 = v1.subtract(entityPos);
        Vec3 relV2 = v2.subtract(entityPos);
        Vec3 relV3 = v3.subtract(entityPos);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // 三角形の内部を描画（半透明）
        drawTriangleFill(vertexConsumer, matrix, normal, relV1, relV2, relV3, fillAlpha);

        // 三角形の辺を描画（明るい線）
        drawEdge(vertexConsumer, matrix, normal, relV1, relV2, edgeAlpha);
        drawEdge(vertexConsumer, matrix, normal, relV2, relV3, edgeAlpha);
        drawEdge(vertexConsumer, matrix, normal, relV3, relV1, edgeAlpha);

        poseStack.popPose();
    }

    /**
     * 三角形の内部を塗りつぶし描画（両面）
     */
    private void drawTriangleFill(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                  Vec3 v1, Vec3 v2, Vec3 v3, int alpha) {
        // 表面
        addVertex(consumer, matrix, normal, v1, RED, GREEN, BLUE, alpha);
        addVertex(consumer, matrix, normal, v2, RED, GREEN, BLUE, alpha);
        addVertex(consumer, matrix, normal, v3, RED, GREEN, BLUE, alpha);
        addVertex(consumer, matrix, normal, v3, RED, GREEN, BLUE, alpha); // 4頂点必要なので重複

        // 裏面（逆順）
        addVertex(consumer, matrix, normal, v3, RED, GREEN, BLUE, alpha);
        addVertex(consumer, matrix, normal, v2, RED, GREEN, BLUE, alpha);
        addVertex(consumer, matrix, normal, v1, RED, GREEN, BLUE, alpha);
        addVertex(consumer, matrix, normal, v1, RED, GREEN, BLUE, alpha);
    }

    /**
     * 辺を描画（X字型の太い線）
     */
    private void drawEdge(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                          Vec3 start, Vec3 end, int alpha) {
        Vec3 direction = end.subtract(start);
        if (direction.lengthSqr() < 0.001) {
            return;
        }
        direction = direction.normalize();

        // 2つの直交するオフセットを計算
        Vec3 perpendicular1 = getPerpendicular(direction);
        Vec3 perpendicular2 = direction.cross(perpendicular1).normalize();

        Vec3 offset1 = perpendicular1.add(perpendicular2).normalize().scale(EDGE_WIDTH);
        Vec3 offset2 = perpendicular1.subtract(perpendicular2).normalize().scale(EDGE_WIDTH);

        // 1枚目
        drawQuadBothSides(consumer, matrix, normal, start, end, offset1, alpha);
        // 2枚目
        drawQuadBothSides(consumer, matrix, normal, start, end, offset2, alpha);
    }

    /**
     * 方向ベクトルに垂直なベクトルを取得
     */
    private Vec3 getPerpendicular(Vec3 direction) {
        if (Math.abs(direction.y) < 0.99) {
            return direction.cross(new Vec3(0, 1, 0)).normalize();
        } else {
            return direction.cross(new Vec3(1, 0, 0)).normalize();
        }
    }

    /**
     * 両面の長方形を描画
     */
    private void drawQuadBothSides(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                   Vec3 start, Vec3 end, Vec3 offset, int alpha) {
        Vec3 v1 = start.add(offset);
        Vec3 v2 = start.subtract(offset);
        Vec3 v3 = end.subtract(offset);
        Vec3 v4 = end.add(offset);

        // 表面
        addVertex(consumer, matrix, normal, v1, EDGE_RED, EDGE_GREEN, EDGE_BLUE, alpha);
        addVertex(consumer, matrix, normal, v2, EDGE_RED, EDGE_GREEN, EDGE_BLUE, alpha);
        addVertex(consumer, matrix, normal, v3, EDGE_RED, EDGE_GREEN, EDGE_BLUE, alpha);
        addVertex(consumer, matrix, normal, v4, EDGE_RED, EDGE_GREEN, EDGE_BLUE, alpha);

        // 裏面
        addVertex(consumer, matrix, normal, v4, EDGE_RED, EDGE_GREEN, EDGE_BLUE, alpha);
        addVertex(consumer, matrix, normal, v3, EDGE_RED, EDGE_GREEN, EDGE_BLUE, alpha);
        addVertex(consumer, matrix, normal, v2, EDGE_RED, EDGE_GREEN, EDGE_BLUE, alpha);
        addVertex(consumer, matrix, normal, v1, EDGE_RED, EDGE_GREEN, EDGE_BLUE, alpha);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                           Vec3 pos, int r, int g, int b, int a) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)  // フルブライト
                .normal(normal, 0, 1, 0)
                .endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityMolecularDivider entity) {
        return null;
    }
}