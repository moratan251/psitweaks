package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.combat.EntityGravityWell;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Trick: Gravity Collapse. A black core with a glowing horizon grows while two tilted accretion rings turn and
 * streaks of light spiral into it; a faint ring on the ground plane shows how far the pull reaches.
 */
public class GravityWellRenderer extends CombatGlowRenderer<EntityGravityWell> {
    private static final int STREAKS = 12;
    private static final int STREAK_POINTS = 7;
    private static final float COLLAPSE_TICKS = 4.0F;

    public GravityWellRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityGravityWell entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        float age = entity.tickCount + partialTick;
        float charge = entity.getCharge(partialTick);
        float collapse = Mth.clamp((age - EntityGravityWell.CHARGE_TICKS) / COLLAPSE_TICKS, 0.0F, 1.0F);
        float core = (0.35F + 0.65F * charge) * Mth.clamp(age / 5.0F, 0.0F, 1.0F) * (1.0F - collapse);
        if (core <= 0.01F) {
            return;
        }
        Vector3f center = new Vector3f();
        Matrix4f pose = cameraPose(entity, partialTick, poseStack, center);
        int color = GlowDrawing.glowColor(entity.getColor());
        float radius = entity.getRadius();

        VertexConsumer dark = buffers.getBuffer(PsitweaksRenderTypes.TRANSLUCENT_COLOR);
        GlowDrawing.rimSphere(dark, pose, center, core, 0x040008, 1.0F, 0.0F, 235);

        VertexConsumer glow = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        GlowDrawing.rimSphere(glow, pose, center, core * 1.12F, color, 0.0F, 1.1F, 255);
        float fade = 1.0F - collapse;
        Vector3f tiltA = new Vector3f(Mth.sin(0.38F), Mth.cos(0.38F), 0.0F).rotateY(age * 0.05F);
        Vector3f tiltB = new Vector3f(0.0F, Mth.cos(0.55F), Mth.sin(0.55F)).rotateY(-age * 0.035F);
        float discOuter = core * 1.35F + 0.6F + 1.6F * charge;
        GlowDrawing.ring(glow, pose, center, tiltA, core * 1.3F, discOuter, 56, color, 0.9F * fade, 0.0F, 255);
        GlowDrawing.ring(glow, pose, center, tiltB, core * 1.25F, core * 1.25F + 0.5F + 0.8F * charge, 48,
                GlowDrawing.whiten(color, 0.3F), 0.6F * fade, 0.0F, 255);
        spirals(glow, pose, center, tiltA, radius, core, age, color, Math.min(1.0F, charge * 3.0F) * fade);

        float pulse = 0.7F + 0.3F * Mth.sin(age * 0.4F);
        GlowDrawing.ring(glow, pose, center, UP, radius - 0.15F, radius, 72, color, 0.0F, 0.3F * pulse * fade, 255);
    }

    /** Short streaks running inward along a spiral in the plane of the main accretion ring. */
    private static void spirals(VertexConsumer consumer, Matrix4f pose, Vector3f center, Vector3f normal,
                                float radius, float core, float age, int color, float strength) {
        if (strength <= 0.0F) {
            return;
        }
        Vector3f axis = new Vector3f(normal).normalize();
        Vector3f helper = Math.abs(axis.y) < 0.95F ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
        Vector3f u = new Vector3f(helper).cross(axis).normalize();
        Vector3f v = new Vector3f(axis).cross(u).normalize();
        int bright = GlowDrawing.whiten(color, 0.45F);
        for (int streak = 0; streak < STREAKS; streak++) {
            float head = Mth.frac(age / 26.0F + streak * 0.618F);
            float tail = Math.max(0.0F, head - 0.22F);
            Vector3f previous = null;
            for (int i = 0; i < STREAK_POINTS; i++) {
                float t = Mth.lerp((float) i / (STREAK_POINTS - 1), tail, head);
                float distance = Mth.lerp(t, radius * 0.95F, core * 1.2F);
                float angle = Mth.TWO_PI * streak / STREAKS + t * 5.0F + age * 0.04F;
                Vector3f point = new Vector3f(u).mul(Mth.cos(angle) * distance)
                        .add(new Vector3f(v).mul(Mth.sin(angle) * distance)).add(center);
                if (previous != null) {
                    float brightness = strength * 0.9F * t * ((float) i / (STREAK_POINTS - 1));
                    GlowDrawing.ribbon(consumer, pose, previous, point, 0.09F, bright, brightness, 255);
                }
                previous = point;
            }
        }
    }
}
