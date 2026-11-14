package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.combat.EntityPsiBullet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Trick: Psi Bullet. A small, hard bead of psionic light in the caster's CAD colour: a white-hot core in a bright
 * shell, a halo, and a thin streak trailing behind it.
 */
public class PsiBulletRenderer extends CombatGlowRenderer<EntityPsiBullet> {
    private static final float TRAIL_LENGTH = 2.4F;

    public PsiBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityPsiBullet entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        float age = entity.tickCount + partialTick;
        float fade = Mth.clamp((age + 0.5F) / 2.0F, 0.0F, 1.0F)
                * Mth.clamp((EntityPsiBullet.MAX_LIFETIME + 1.0F - age) / 4.0F, 0.0F, 1.0F);
        if (fade <= 0.0F) {
            return;
        }
        Vector3f center = new Vector3f();
        Matrix4f pose = cameraPose(entity, partialTick, poseStack, center);
        Vec3 direction = entity.getFlightDirection();
        float radius = EntityPsiBullet.REACH * 0.6F;
        // The streak never reaches back past where the bullet was fired.
        float trail = Math.min(TRAIL_LENGTH, age * EntityPsiBullet.SPEED + 0.2F);
        int color = GlowDrawing.glowColor(entity.getColor());
        int shell = GlowDrawing.whiten(color, 0.4F);
        int core = GlowDrawing.whiten(color, 0.9F);
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);

        Vector3f tail = offset(center, direction, -trail);
        GlowDrawing.ribbon(consumer, pose, center, tail, radius * 2.2F, 0.0F, color, 0.55F * fade, 0.0F, 255);
        GlowDrawing.ribbon(consumer, pose, center, offset(center, direction, -trail * 0.6F), radius * 0.8F, 0.0F, core,
                0.8F * fade, 0.0F, 255);
        GlowDrawing.glowDisc(consumer, pose, center, radius * 3.2F, color, 0.5F * fade, 255);
        GlowDrawing.rimSphere(consumer, pose, center, radius, shell, 0.2F * fade, 0.9F * fade, 255);
        GlowDrawing.glowDisc(consumer, pose, center, radius * 0.9F, core, 1.0F * fade, 255);
    }
}
