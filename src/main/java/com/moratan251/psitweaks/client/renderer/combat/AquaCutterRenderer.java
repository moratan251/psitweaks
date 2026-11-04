package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.EntityAquaCutterProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Trick: Aqua Cutter. An upright crescent of water with its middle bowed forward: a see-through blue body that
 * ripples, a bright cutting edge along its front, spray streaming from its tips and two fading afterimages behind it.
 * The caster's CAD colour tints the glow ({@link EntityAquaCutterProjectile#tint}); the drops falling along its path
 * are particles the entity sheds itself.
 */
public class AquaCutterRenderer extends CombatGlowRenderer<EntityAquaCutterProjectile> {
    private static final int POINTS = 12;
    private static final int AFTERIMAGES = 2;
    private static final float AFTERIMAGE_GAP = 0.45F;
    /** Half the blade's height, from its middle to a tip. */
    private static final float HALF_SPAN = 0.42F;
    /** How far the middle of the blade bows ahead of its tips. */
    private static final float BOW = 0.24F;
    /** The body's thickness in its middle; it thins to a point at the tips. */
    private static final float THICKNESS = 0.17F;
    private static final float TRAIL_LENGTH = 2.2F;
    /** The blade fades in over this distance from the camera, so it does not fill the caster's view as it leaves. */
    private static final float NEAR_FADE_START = 0.7F;
    private static final float NEAR_FADE_LENGTH = 1.3F;
    private static final int BODY_COLOR = 0x2C7FD6;
    private static final int BODY_ALPHA = 150;
    private static final int EDGE_COLOR = 0xE2F7FF;

    public AquaCutterRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(@NotNull EntityAquaCutterProjectile entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        Vector3f origin = new Vector3f();
        Matrix4f pose = cameraPose(entity, partialTick, poseStack, origin);
        Vector3f center = new Vector3f(origin).add(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        float near = Mth.clamp((center.length() - NEAR_FADE_START) / NEAR_FADE_LENGTH, 0.0F, 1.0F);
        if (near <= 0.0F) {
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        Vec3 direction = flightDirection(entity, motion);
        Vec3 up = bladeUp(direction);
        float age = entity.tickCount + partialTick;
        // The wake never reaches back past where the blade was cast.
        float trail = Math.min(TRAIL_LENGTH, age * (float) motion.length());
        int glow = GlowDrawing.glowColor(EntityAquaCutterProjectile.tint(entity.getColor()));
        int spray = GlowDrawing.whiten(glow, 0.4F);

        // The water first (alpha-blended): the afterimages behind, then the blade itself.
        VertexConsumer water = buffers.getBuffer(PsitweaksRenderTypes.TRANSLUCENT_COLOR);
        for (int image = AFTERIMAGES; image >= 0; image--) {
            float back = image * AFTERIMAGE_GAP;
            if (image > 0 && back > trail) {
                continue;
            }
            float scale = 1.0F - image * 0.15F;
            int alpha = (int) (BODY_ALPHA * near * (image == 0 ? 1.0F : 0.4F / image));
            int color = image == 0 ? BODY_COLOR : GlowDrawing.whiten(BODY_COLOR, 0.35F);
            Vector3f[] curve = crescent(offset(center, direction, -back), direction, up, scale);
            for (int i = 0; i < POINTS; i++) {
                GlowDrawing.ribbon(water, pose, curve[i], curve[i + 1], thickness(i) * scale, thickness(i + 1) * scale,
                        color, ripple(i, age), ripple(i + 1, age), alpha);
            }
        }

        // Then the light on top of it (additive).
        VertexConsumer light = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        if (trail > 0.05F) {
            GlowDrawing.ribbon(light, pose, center, offset(center, direction, -trail), THICKNESS * 3.0F, THICKNESS,
                    glow, 0.25F * near, 0.0F, 255);
        }
        Vector3f[] curve = crescent(center, direction, up, 1.0F);
        // Spray streaming off the tips, opening up and down a little as it falls behind.
        for (int tip = 0; tip <= 1; tip++) {
            Vector3f from = curve[tip == 0 ? 0 : POINTS];
            Vector3f to = offset(offset(from, direction, -trail * 0.75F), up, (tip == 0 ? -0.12F : 0.12F) * trail / TRAIL_LENGTH);
            GlowDrawing.ribbon(light, pose, from, to, 0.07F, 0.0F, spray, 0.5F * near, 0.0F, 255);
        }
        GlowDrawing.glowDisc(light, pose, offset(center, direction, BOW * 0.25F), HALF_SPAN * 1.1F, glow, 0.18F * near, 255);
        for (int i = 0; i < POINTS; i++) {
            GlowDrawing.ribbon(light, pose, curve[i], curve[i + 1], thickness(i) * 2.6F, thickness(i + 1) * 2.6F,
                    glow, 0.2F * near, 0.2F * near, 255);
        }
        Vector3f[] edge = cuttingEdge(curve, direction, up);
        for (int i = 0; i < POINTS; i++) {
            GlowDrawing.ribbon(light, pose, edge[i], edge[i + 1], edgeWidth(i), edgeWidth(i + 1),
                    EDGE_COLOR, 0.95F * near, 0.95F * near, 255);
        }
    }

    /** Where the blade is heading: its motion, or its facing while it barely moves. */
    private static Vec3 flightDirection(EntityAquaCutterProjectile entity, Vec3 motion) {
        if (motion.lengthSqr() > 1.0E-6) {
            return motion.normalize();
        }
        // Projectiles face along atan2(x, z) and atan2(y, horizontal) (see Projectile#updateRotation).
        float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
        float pitch = entity.getXRot() * Mth.DEG_TO_RAD;
        return new Vec3(Mth.sin(yaw) * Mth.cos(pitch), Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch));
    }

    /** The blade stands upright: "up" is the world's up, tilted to be square to the flight. */
    private static Vec3 bladeUp(Vec3 direction) {
        Vec3 up = new Vec3(0.0, 1.0, 0.0).subtract(direction.scale(direction.y));
        return up.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : up.normalize();
    }

    /** Points along the middle of the crescent, tip to tip: the middle bows forward, the tips trail behind. */
    private static Vector3f[] crescent(Vector3f center, Vec3 direction, Vec3 up, float scale) {
        Vector3f[] points = new Vector3f[POINTS + 1];
        float bow = BOW * scale;
        for (int i = 0; i <= POINTS; i++) {
            float s = position(i);
            points[i] = offset(offset(center, up, s * HALF_SPAN * scale), direction, bow * (1.0F - s * s) - bow * 0.5F);
        }
        return points;
    }

    /** The cutting edge: the crescent's points moved forward, square to its curve, near the front of the body. */
    private static Vector3f[] cuttingEdge(Vector3f[] curve, Vec3 direction, Vec3 up) {
        Vector3f[] edge = new Vector3f[POINTS + 1];
        for (int i = 0; i <= POINTS; i++) {
            float s = position(i);
            Vec3 normal = direction.scale(HALF_SPAN).add(up.scale(2.0F * BOW * s)).normalize();
            edge[i] = offset(curve[i], normal, thickness(i) * 0.35F);
        }
        return edge;
    }

    /** -1 at the lower tip, 0 in the middle and 1 at the upper tip. */
    private static float position(int index) {
        return -1.0F + 2.0F * index / POINTS;
    }

    private static float thickness(int index) {
        float s = position(index);
        return 0.01F + THICKNESS * (1.0F - s * s);
    }

    private static float edgeWidth(int index) {
        float s = position(index);
        return 0.01F + 0.05F * Mth.sqrt(1.0F - s * s);
    }

    /** The body's brightness, rippling along the blade as the water churns. */
    private static float ripple(int index, float age) {
        return 0.85F + 0.15F * Mth.sin(age * 0.9F + position(index) * 5.0F);
    }
}
