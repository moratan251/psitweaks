package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.SpellGram.marker.EntityMeteorLineNight;
import com.moratan251.psitweaks.common.spells.combat.MeteorLineTuning;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Trick: Meteor Line. A dark, see-through dome with a faint rim spreads from the centre, stars light up one after
 * another on the inside of its upper part and twinkle, and when the night clears they drift outward and fade. The
 * caster's CAD colour tints the glow around each star.
 */
public class MeteorLineNightRenderer extends CombatGlowRenderer<EntityMeteorLineNight> {
    private static final float STAR_LIGHT_TICKS = 6.0F;

    public MeteorLineNightRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull EntityMeteorLineNight entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        float strength = entity.getStrength(partialTick);
        if (strength <= 0.01F) {
            return;
        }
        Vector3f center = new Vector3f();
        Matrix4f pose = cameraPose(entity, partialTick, poseStack, center);
        // エンティティ（核）は球のてっぺんにあるので、球の中心はそこから半径ぶん下。
        center.sub(0.0F, (float) entity.getRadius(), 0.0F);
        float radius = (float) entity.getCurrentRadius(partialTick);
        float age = entity.getAge(partialTick);

        VertexConsumer dark = buffers.getBuffer(PsitweaksRenderTypes.TRANSLUCENT_COLOR);
        GlowDrawing.rimGlobe(dark, pose, center, radius, MeteorLineTuning.NIGHT_COLOR, 1.0F, 0.0F,
                (int) (MeteorLineTuning.DOME_ALPHA * strength));

        VertexConsumer glow = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        GlowDrawing.rimGlobe(glow, pose, center, radius * 1.005F, MeteorLineTuning.DOME_RIM_COLOR, 0.0F,
                MeteorLineTuning.DOME_RIM_BRIGHTNESS * strength, 255);
        GlowDrawing.ring(glow, pose, new Vector3f(center).add(0.0F, 0.05F, 0.0F), UP, Math.max(0.0F, radius - 0.12F), radius,
                96, MeteorLineTuning.DOME_RIM_COLOR, 0.0F, 0.6F * strength, 255);

        float starSize = MeteorLineTuning.starSize(entity.getRadius());
        int accent = GlowDrawing.glowColor(entity.getColor());
        int starColor = GlowDrawing.mix(MeteorLineTuning.STAR_COLOR, accent, MeteorLineTuning.STAR_ACCENT);
        float clear = Mth.clamp((age - MeteorLineTuning.CLEAR_START) / MeteorLineTuning.CLEAR_TICKS, 0.0F, 1.0F);
        // Stars ride the dome while it spreads, and scatter outward as the night clears.
        double scale = radius / entity.getRadius() * (1.0 + MeteorLineTuning.STAR_SCATTER * clear);
        for (EntityMeteorLineNight.Star star : entity.getStars()) {
            float lit = Mth.clamp((age - star.appear()) / STAR_LIGHT_TICKS, 0.0F, 1.0F);
            float twinkle = 0.65F + 0.35F * Mth.sin(age * star.speed() + star.phase());
            float brightness = lit * twinkle * (1.0F - clear) * star.brightness();
            if (brightness <= 0.01F) {
                continue;
            }
            Vec3 offset = star.offset().scale(scale);
            Vector3f at = new Vector3f(center).add((float) offset.x, (float) offset.y, (float) offset.z);
            float size = starSize * star.scale();
            GlowDrawing.glowDisc(glow, pose, at, size * MeteorLineTuning.STAR_HALO_SCALE, accent,
                    brightness * MeteorLineTuning.STAR_HALO_BRIGHTNESS, 255);
            GlowDrawing.glowDisc(glow, pose, at, size, starColor, brightness, 255);
            GlowDrawing.glowDisc(glow, pose, at, size * 0.35F, 0xFFFFFF, brightness, 255);
        }
    }
}
