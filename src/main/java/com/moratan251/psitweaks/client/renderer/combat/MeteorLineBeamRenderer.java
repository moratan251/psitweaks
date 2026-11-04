package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.EntityMeteorLineBeam;
import com.moratan251.psitweaks.common.spells.combat.MeteorLineTuning;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Trick: Meteor Line. A shooting star runs from its star to the end of the line in a few ticks and leaves a white beam
 * with a pale cyan edge, which thins out and fades; the caster's CAD colour is mixed into the edge and the glows.
 * Where the line went into the ground or a wall, a small glowing mark stays for a few seconds once the head has passed
 * it.
 */
public class MeteorLineBeamRenderer extends CombatGlowRenderer<EntityMeteorLineBeam> {
    private static final float FLASH_TICKS = 5.0F;
    /** How far a mark is lifted out of the block towards the star, so the block face does not cut it. */
    private static final double MARK_LIFT = 0.08;

    public MeteorLineBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityMeteorLineBeam entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        Matrix4f pose = cameraPose(entity, partialTick, poseStack);
        VertexConsumer glow = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        float age = entity.getAge(partialTick);
        Vec3 start = entity.getStartPos();
        Vec3 end = entity.getEndPos();
        float run = Mth.clamp(age / MeteorLineTuning.HEAD_TICKS, 0.0F, 1.0F);
        Vector3f from = relative(start);
        Vector3f head = relative(start.lerp(end, run));
        int accent = GlowDrawing.glowColor(entity.getColor());
        int edgeColor = GlowDrawing.mix(MeteorLineTuning.BEAM_EDGE_COLOR, accent, MeteorLineTuning.BEAM_ACCENT);

        float fade = 1.0F - Mth.clamp((age - (MeteorLineTuning.BEAM_TICKS - MeteorLineTuning.BEAM_FADE_TICKS))
                / MeteorLineTuning.BEAM_FADE_TICKS, 0.0F, 1.0F);
        if (fade > 0.0F) {
            GlowDrawing.ribbon(glow, pose, from, head, MeteorLineTuning.BEAM_EDGE_WIDTH * fade, edgeColor, 0.55F * fade, 255);
            GlowDrawing.ribbon(glow, pose, from, head, MeteorLineTuning.BEAM_CORE_WIDTH * fade, MeteorLineTuning.BEAM_CORE_COLOR,
                    fade, 255);
            if (run < 1.0F) {
                GlowDrawing.glowDisc(glow, pose, head, 0.6F, edgeColor, 0.9F, 255);
                GlowDrawing.glowDisc(glow, pose, head, 0.25F, MeteorLineTuning.BEAM_CORE_COLOR, 1.0F, 255);
            }
        }
        float flash = 1.0F - Mth.clamp(age / FLASH_TICKS, 0.0F, 1.0F);
        if (flash > 0.0F) {
            GlowDrawing.glowDisc(glow, pose, from, 0.8F,
                    GlowDrawing.mix(MeteorLineTuning.STAR_COLOR, accent, MeteorLineTuning.STAR_ACCENT), flash, 255);
        }

        // A mark shows once the head has reached it, stays bright for a while and then fades out.
        float markLight = Mth.clamp((MeteorLineTuning.MARK_TICKS - age) / (MeteorLineTuning.MARK_TICKS * 0.6F), 0.0F, 1.0F);
        if (markLight <= 0.0F) {
            return;
        }
        double reached = start.distanceTo(end) * run;
        for (Vec3 mark : entity.getMarks()) {
            if (mark.distanceTo(start) > reached) {
                continue;
            }
            Vec3 lift = start.subtract(mark).normalize().scale(MARK_LIFT);
            Vector3f at = relative(mark.add(lift));
            GlowDrawing.glowDisc(glow, pose, at, MeteorLineTuning.MARK_SIZE, edgeColor, markLight, 255);
            GlowDrawing.glowDisc(glow, pose, at, MeteorLineTuning.MARK_SIZE * 0.4F, MeteorLineTuning.BEAM_CORE_COLOR, markLight, 255);
        }
    }
}
