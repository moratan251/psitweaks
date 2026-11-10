package com.moratan251.psitweaks.client.renderer.circle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * 数式で組み立てた魔法陣（{@link CircleDesign}）を、加算合成の色だけの面（{@link PsitweaksRenderTypes#ADDITIVE_COLOR}）で
 * 描く。テクスチャもシェーダーも使わないので、シェーダーパックと一緒でも崩れにくい。
 * <p>
 * 魔法陣は {@code poseStack} の原点を中心に、x-z 平面（水平）に寝かせて描く。層は少しずつ上にずらし、層ごとに
 * {@link CircleDesign.Layer#spin} の速さで回す。
 */
public final class VectorCircleRenderer {
    /** 層どうしの高さの間隔（ブロック）。深度を書かないので重なりのちらつきはないが、真横から見たときに厚みが出ない程度。 */
    private static final float LAYER_GAP = 0.003F;

    private VectorCircleRenderer() {
    }

    /**
     * @param radius     外周の半径（ブロック）。四角い図案では一辺の半分。
     * @param lineScale  線の太さの倍率。図案ごとに形を取っておくので、決まった値（定数）だけを渡す。
     * @param time       回転に使う経過 tick（partialTick 込み）
     * @param color      0xRRGGBB。暗い色は加算で見えなくなるので、明るさを持ち上げてから使う。
     * @param brightness 全体の明るさ（0〜1）
     */
    public static void render(CircleDesign design, float lineScale, PoseStack poseStack, MultiBufferSource buffers,
                              float radius, float time, int color, float brightness) {
        if (radius <= 0.0F || brightness <= 0.0F) {
            return;
        }
        int glow = GlowDrawing.glowColor(color & 0xFFFFFF);
        float red = GlowDrawing.red(glow) * brightness;
        float green = GlowDrawing.green(glow) * brightness;
        float blue = GlowDrawing.blue(glow) * brightness;
        float scale = radius / 64.0F;
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        CircleMesh[] meshes = design.meshes(lineScale);
        for (int layer = 0; layer < meshes.length; layer++) {
            CircleMesh mesh = meshes[layer];
            float angle = design.layer(layer).spin() * time % 360.0F * Mth.DEG_TO_RAD;
            float cos = Mth.cos(angle) * scale;
            float sin = Mth.sin(angle) * scale;
            float y = layer * LAYER_GAP;
            int count = mesh.vertexCount();
            for (int i = 0; i < count; i++) {
                float x = mesh.x(i);
                float z = mesh.y(i);
                float b = mesh.brightness(i);
                consumer.addVertex(pose, x * cos - z * sin, y, x * sin + z * cos)
                        .setColor(Math.min(255, (int) (red * b)), Math.min(255, (int) (green * b)),
                                Math.min(255, (int) (blue * b)), 255);
            }
        }
    }
}
