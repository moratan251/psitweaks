package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.client.render.entity.RenderSpellCircle;

/**
 * 領域型の魔法式オブジェクトの「核」: 範囲の中心に出す小さな魔法陣（Psi の魔法陣）。サークル系と流星群の夜には出さない。見ている人の方を向く 1 枚と、縦の軸で
 * ゆっくり回る 1 枚を重ねて、どの向きからでも見えるようにする。オブジェクトのある場所が分かり、後で術式解体などを当てる
 * ときの的にもなる。耐久値が減るほど暗くなる。
 */
public final class SpellGramCoreRenderer {
    /** 向かい合う魔法陣の大きさ（Psi の魔法陣の半径はおよそ 2 × これ）。 */
    private static final float FACING_SCALE = 0.33F;
    /** 回る魔法陣の大きさ。 */
    private static final float SPINNING_SCALE = 0.24F;
    private static final float SPIN_DEGREES_PER_TICK = 3.0F;

    private SpellGramCoreRenderer() {
    }

    /**
     * {@code poseStack} はオブジェクトの位置にある前提（エンティティの描画の中で呼ぶ）。{@code height} はその位置から核までの
     * 高さ（床に置くサークルは少し浮かせる）。
     */
    public static void render(SpellGramObject object, int color, float height, float partialTicks, Camera camera,
                              PoseStack poseStack, MultiBufferSource buffer) {
        float max = object.getMaxIntegrity();
        float fraction = max > 0.0F ? Mth.clamp(object.getIntegrity() / max, 0.0F, 1.0F) : 1.0F;
        int shaded = shade(color, 0.4F + 0.6F * fraction);
        float time = object.tickCount + partialTicks;

        poseStack.pushPose();
        poseStack.translate(0.0, height, 0.0);

        Vec3 at = object.getPosition(partialTicks).add(0.0, height, 0.0);
        Vec3 toCamera = camera.getPosition().subtract(at);
        if (toCamera.lengthSqr() < 1.0E-6) {
            toCamera = new Vec3(0.0, 1.0, 0.0);
        }
        toCamera = toCamera.normalize();
        RenderSpellCircle.renderSpellCircle(time, 1.0F, FACING_SCALE,
                (float) toCamera.x, (float) toCamera.y, (float) toCamera.z, shaded, poseStack, buffer);

        float spin = (time * SPIN_DEGREES_PER_TICK) * Mth.DEG_TO_RAD;
        RenderSpellCircle.renderSpellCircle(time, 1.0F, SPINNING_SCALE,
                Mth.cos(spin), 0.0F, Mth.sin(spin), shaded, poseStack, buffer);

        poseStack.popPose();
    }

    private static int shade(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (r << 16) | (g << 8) | b;
    }
}
