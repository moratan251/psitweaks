package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.combat.EntityKamaitachiBlade;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Trick: Kamaitachi. A crescent of wind, thick in the middle and sharp at the tips, with fading afterimages. */
public class KamaitachiBladeRenderer extends CombatGlowRenderer<EntityKamaitachiBlade> {
    private static final int POINTS = 16;
    private static final int AFTERIMAGES = 3;

    public KamaitachiBladeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityKamaitachiBlade entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        float age = entity.tickCount + partialTick;
        float fade = Mth.clamp((age + 0.5F) / 2.0F, 0.0F, 1.0F)
                * Mth.clamp((EntityKamaitachiBlade.MAX_LIFETIME + 1.0F - age) / 4.0F, 0.0F, 1.0F);
        if (fade <= 0.0F) {
            return;
        }
        Vector3f origin = new Vector3f();
        Matrix4f pose = cameraPose(entity, partialTick, poseStack, origin);
        Vec3 direction = entity.getFlightDirection();
        Vec3 side = entity.getSide();
        float halfWidth = entity.getWidth() * 0.5F;
        int color = GlowDrawing.glowColor(entity.getColor());
        int core = GlowDrawing.whiten(color, 0.7F);
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);

        for (int image = AFTERIMAGES; image >= 0; image--) {
            float strength = fade * (1.0F - image * 0.27F);
            float scale = 1.0F - image * 0.12F;
            Vector3f center = offset(origin, direction, -image * 0.6);
            Vector3f[] curve = crescent(center, direction, side, halfWidth * scale);
            for (int i = 0; i < POINTS; i++) {
                float w0 = thickness(i) * scale;
                float w1 = thickness(i + 1) * scale;
                GlowDrawing.ribbon(consumer, pose, curve[i], curve[i + 1], w0 * 2.4F, w1 * 2.4F, color,
                        0.32F * strength, 0.32F * strength, 255);
                if (image == 0) {
                    GlowDrawing.ribbon(consumer, pose, curve[i], curve[i + 1], w0 * 0.55F, w1 * 0.55F, core,
                            0.95F * strength, 0.95F * strength, 255);
                }
            }
        }
    }

    /** Points along the crescent: the middle bows forward, the tips trail behind. */
    private static Vector3f[] crescent(Vector3f center, Vec3 direction, Vec3 side, float halfWidth) {
        Vector3f[] points = new Vector3f[POINTS + 1];
        float bow = 0.3F * halfWidth + 0.2F;
        for (int i = 0; i <= POINTS; i++) {
            float s = -1.0F + 2.0F * i / POINTS;
            Vector3f point = offset(center, side, s * halfWidth);
            points[i] = offset(point, direction, bow * (1.0F - s * s) - bow * 0.5F);
        }
        return points;
    }

    private static float thickness(int index) {
        float s = -1.0F + 2.0F * index / POINTS;
        return 0.04F + 0.3F * (1.0F - s * s);
    }
}
