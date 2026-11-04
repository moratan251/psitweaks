package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.combat.EntityAirBullet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Trick: Air Bullet. A ball of compressed air: a bright core in a bubble-like shell, air streaming back from it in a
 * spiral, and pressure rings that open behind it out to the width its pressure wave strikes.
 */
public class AirBulletRenderer extends CombatGlowRenderer<EntityAirBullet> {
    private static final int STREAKS = 6;
    private static final int RINGS = 3;
    private static final float TRAIL_LENGTH = 3.2F;
    private static final float RING_TRAIL = 2.6F;

    public AirBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityAirBullet entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        float age = entity.tickCount + partialTick;
        float fade = Mth.clamp((age + 0.5F) / 2.0F, 0.0F, 1.0F)
                * Mth.clamp((EntityAirBullet.MAX_LIFETIME + 1.0F - age) / 4.0F, 0.0F, 1.0F);
        if (fade <= 0.0F) {
            return;
        }
        Vector3f center = new Vector3f();
        Matrix4f pose = cameraPose(entity, partialTick, poseStack, center);
        Vec3 direction = entity.getFlightDirection();
        Vec3 side = entity.getSide();
        Vec3 up = side.cross(direction).normalize();
        Vector3f axis = new Vector3f((float) direction.x, (float) direction.y, (float) direction.z);
        float halfWidth = entity.getWidth() * 0.5F;
        float radius = bulletRadius(entity.getWidth());
        // The trail never reaches back past where the bullet was fired.
        float travelled = age * EntityAirBullet.SPEED;
        float trail = Math.min(TRAIL_LENGTH, travelled + 0.3F);
        int color = GlowDrawing.glowColor(entity.getColor());
        int shell = GlowDrawing.whiten(color, 0.45F);
        int core = GlowDrawing.whiten(color, 0.85F);
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);

        // Pressure rings: each one leaves the bullet at its size and opens to the struck width as it falls behind.
        for (int i = 0; i < RINGS; i++) {
            float phase = Mth.frac(age * 0.35F + (float) i / RINGS);
            float distance = phase * RING_TRAIL;
            if (distance > travelled) {
                continue;
            }
            float ringRadius = Mth.lerp(phase, radius * 1.2F, halfWidth);
            float thickness = 0.08F + 0.08F * (1.0F - phase);
            float strength = fade * 0.6F * (1.0F - phase) * Mth.clamp(phase * 5.0F, 0.0F, 1.0F);
            GlowDrawing.ring(consumer, pose, offset(center, direction, -distance), axis,
                    Math.max(0.0F, ringRadius - thickness), ringRadius, 32, color, strength * 0.3F, strength, 255);
        }

        // The wake: a soft channel of disturbed air behind the bullet.
        GlowDrawing.ribbon(consumer, pose, center, offset(center, direction, -trail), radius * 2.2F, radius * 0.6F,
                color, 0.3F * fade, 0.0F, 255);
        // Air streaming off the shell, twisting as it trails away.
        for (int k = 0; k < STREAKS; k++) {
            float angle = Mth.TWO_PI * k / STREAKS + age * 0.6F;
            Vector3f from = offset(offset(center, side, Mth.cos(angle) * radius * 0.9F), up, Mth.sin(angle) * radius * 0.9F);
            float twisted = angle + 0.9F;
            float spread = radius * 0.9F + 0.15F * halfWidth;
            Vector3f to = offset(offset(offset(center, direction, -trail * 0.8F), side, Mth.cos(twisted) * spread),
                    up, Mth.sin(twisted) * spread);
            GlowDrawing.ribbon(consumer, pose, from, to, 0.08F, 0.0F, shell, 0.65F * fade, 0.0F, 255);
        }

        // The bullet itself: a halo, a bubble-like shell bright at its rim and a white-hot core.
        GlowDrawing.glowDisc(consumer, pose, center, radius * 2.6F, color, 0.45F * fade, 255);
        GlowDrawing.rimSphere(consumer, pose, center, radius, shell, 0.06F * fade, 0.9F * fade, 255);
        GlowDrawing.glowDisc(consumer, pose, center, radius * 0.8F, core, 1.0F * fade, 255);
    }

    /** The drawn radius grows a little with the struck width: about 0.35 blocks at the narrowest, 0.54 at the widest. */
    private static float bulletRadius(float width) {
        return 0.22F + 0.08F * width;
    }
}
