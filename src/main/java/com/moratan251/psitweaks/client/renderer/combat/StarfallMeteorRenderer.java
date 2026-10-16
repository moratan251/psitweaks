package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.combat.EntityStarfallMeteor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Trick: Starfall. A target ring marks the landing spot, with an inner ring closing in as the meteor approaches;
 * the meteor is a white-hot head with a fiery trail edged in the caster's colour.
 */
public class StarfallMeteorRenderer extends CombatGlowRenderer<EntityStarfallMeteor> {
    private static final int FIRE = 0xFF8A2A;
    private static final int WHITE_HOT = 0xFFF2CC;
    private static final float MAX_TRAIL = 7.0F;

    public StarfallMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityStarfallMeteor entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        Matrix4f pose = cameraPose(entity, partialTick, poseStack);
        float age = entity.getAge(partialTick);
        float total = entity.getDelay() + EntityStarfallMeteor.FLIGHT_TICKS;
        int color = GlowDrawing.glowColor(entity.getColor());
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);

        Vector3f target = relative(entity.getTarget()).add(0.0F, 0.06F, 0.0F);
        float radius = EntityStarfallMeteor.IMPACT_RADIUS;
        float appear = Mth.clamp(age / 6.0F, 0.0F, 1.0F);
        float pulse = 0.75F + 0.25F * Mth.sin(age * 0.9F);
        GlowDrawing.ring(consumer, pose, target, UP, 0.0F, radius - 0.2F, 32, color, 0.12F * appear, 0.05F * appear, 255);
        GlowDrawing.ring(consumer, pose, target, UP, radius - 0.2F, radius, 48, color, 0.25F * appear, 0.85F * appear * pulse, 255);
        float closing = radius * (1.0F - Mth.clamp(age / total, 0.0F, 1.0F));
        if (closing > 0.12F) {
            GlowDrawing.ring(consumer, pose, target, UP, closing - 0.12F, closing, 40, GlowDrawing.whiten(color, 0.4F),
                    0.7F * appear, 0.7F * appear, 255);
        }

        if (!entity.isFalling(partialTick)) {
            return;
        }
        Vector3f head = relative(entity.getFlightPosition(partialTick));
        Vec3 back = entity.getStart().subtract(entity.getTarget()).normalize();
        float travelled = (float) entity.getStart().distanceTo(entity.getTarget()) * entity.getProgress(partialTick);
        float trail = Math.min(MAX_TRAIL, travelled + 0.5F);
        GlowDrawing.ribbon(consumer, pose, head, offset(head, back, trail), 2.4F, 0.2F, color, 0.35F, 0.0F, 255);
        GlowDrawing.ribbon(consumer, pose, head, offset(head, back, trail * 0.85F), 1.2F, 0.0F, FIRE, 0.9F, 0.0F, 255);
        GlowDrawing.ribbon(consumer, pose, head, offset(head, back, trail * 0.45F), 0.45F, 0.0F, WHITE_HOT, 1.0F, 0.0F, 255);
        GlowDrawing.glowDisc(consumer, pose, head, 1.8F, color, 0.5F, 255);
        GlowDrawing.glowDisc(consumer, pose, head, 1.0F, FIRE, 0.9F, 255);
        GlowDrawing.glowDisc(consumer, pose, head, 0.45F, WHITE_HOT, 1.0F, 255);
    }
}
