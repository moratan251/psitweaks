package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.combat.EntityGroundShockwave;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Trick: Mine Genesis. A bright ring runs over the ground with a low wall of light on its front, and glowing
 * cracks open behind it.
 */
public class GroundShockwaveRenderer extends CombatGlowRenderer<EntityGroundShockwave> {
    private static final int SEGMENTS = 72;
    private static final int CRACKS = 11;
    private static final int CRACK_STEPS = 9;

    public GroundShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityGroundShockwave entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        float age = entity.tickCount + partialTick;
        int expandTicks = entity.getExpandTicks();
        float fade = age <= expandTicks ? 1.0F : 1.0F - (age - expandTicks) / EntityGroundShockwave.FADE_TICKS;
        if (fade <= 0.0F) {
            return;
        }
        Vector3f origin = new Vector3f();
        Matrix4f pose = cameraPose(entity, partialTick, poseStack, origin);
        float radius = entity.getRadius(partialTick);
        float progress = radius / Math.max(0.1F, entity.getMaxRadius());
        int color = GlowDrawing.glowColor(entity.getColor());
        int hot = GlowDrawing.whiten(color, 0.55F);
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        Vector3f ground = new Vector3f(origin).add(0.0F, 0.05F, 0.0F);

        // The wake behind the front, the front itself, and a soft edge ahead of it.
        GlowDrawing.ring(consumer, pose, ground, UP, Math.max(0.0F, radius - 2.0F), radius - 0.15F, SEGMENTS,
                color, 0.0F, 0.55F * fade, 255);
        GlowDrawing.ring(consumer, pose, ground, UP, Math.max(0.0F, radius - 0.15F), radius + 0.15F, SEGMENTS,
                hot, 1.0F * fade, 1.0F * fade, 255);
        GlowDrawing.ring(consumer, pose, ground, UP, radius + 0.15F, radius + 0.6F, SEGMENTS,
                color, 0.6F * fade, 0.0F, 255);
        wall(consumer, pose, ground, radius, Mth.lerp(progress, 1.4F, 0.5F) * fade, color, 0.75F * fade);
        cracks(consumer, pose, ground, radius, entity.getId(), color, hot, fade);
    }

    /** A ring of vertical light standing on the front, bright at the ground and fading upward. */
    private static void wall(VertexConsumer consumer, Matrix4f pose, Vector3f ground, float radius, float height,
                             int color, float brightness) {
        if (radius <= 0.05F || height <= 0.0F) {
            return;
        }
        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = Mth.TWO_PI * i / SEGMENTS;
            float a1 = Mth.TWO_PI * (i + 1) / SEGMENTS;
            // A little ripple along the front so it does not look like a perfect cylinder.
            float h0 = height * (0.8F + 0.2F * Mth.sin(a0 * 7.0F + radius * 2.0F));
            float h1 = height * (0.8F + 0.2F * Mth.sin(a1 * 7.0F + radius * 2.0F));
            Vector3f b0 = new Vector3f(Mth.cos(a0) * radius, 0.0F, Mth.sin(a0) * radius).add(ground);
            Vector3f b1 = new Vector3f(Mth.cos(a1) * radius, 0.0F, Mth.sin(a1) * radius).add(ground);
            GlowDrawing.vertex(consumer, pose, b0, color, brightness, 255);
            GlowDrawing.vertex(consumer, pose, b1, color, brightness, 255);
            GlowDrawing.vertex(consumer, pose, new Vector3f(b1).add(0.0F, h1, 0.0F), color, 0.0F, 255);
            GlowDrawing.vertex(consumer, pose, new Vector3f(b0).add(0.0F, h0, 0.0F), color, 0.0F, 255);
        }
    }

    /** Jagged glowing cracks from the centre out to the front; their shape is fixed per shockwave. */
    private static void cracks(VertexConsumer consumer, Matrix4f pose, Vector3f ground, float radius, int seed,
                               int color, int hot, float fade) {
        if (radius < 0.6F) {
            return;
        }
        RandomSource random = RandomSource.create(seed * 341_873_128_712L);
        Vector3f lifted = new Vector3f(ground).add(0.0F, 0.01F, 0.0F);
        for (int crack = 0; crack < CRACKS; crack++) {
            float angle = Mth.TWO_PI * crack / CRACKS + (random.nextFloat() - 0.5F) * 0.45F;
            float reach = radius * (0.65F + random.nextFloat() * 0.3F);
            Vector3f previous = null;
            float wobble = 0.0F;
            for (int step = 0; step <= CRACK_STEPS; step++) {
                float t = (float) step / CRACK_STEPS;
                wobble += (random.nextFloat() - 0.5F) * 0.35F;
                float distance = 0.4F + (reach - 0.4F) * t;
                float bend = angle + wobble / Math.max(1.0F, distance);
                Vector3f point = new Vector3f(Mth.cos(bend) * distance, 0.0F, Mth.sin(bend) * distance).add(lifted);
                if (previous != null) {
                    float glow = (1.0F - t * 0.6F) * fade;
                    GlowDrawing.flatStrip(consumer, pose, previous, point, 0.32F * (1.0F - t * 0.5F), color, 0.3F * glow, 255);
                    GlowDrawing.flatStrip(consumer, pose, previous, point, 0.08F * (1.0F - t * 0.5F), hot, 0.8F * glow, 255);
                }
                previous = point;
            }
        }
    }
}
