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
        int starColor = starColor(accent);
        double scale = starScale(entity.getRadius(), radius, age);
        for (EntityMeteorLineNight.Star star : entity.getStars()) {
            float brightness = starBrightness(star, age);
            if (brightness <= 0.01F) {
                continue;
            }
            Vec3 offset = star.offset().scale(scale);
            Vector3f at = new Vector3f(center).add((float) offset.x, (float) offset.y, (float) offset.z);
            drawStar(glow, pose, at, starSize * star.scale(), accent, starColor, brightness);
        }
    }

    /** How far the night has cleared at {@code age}: 0 before it starts to clear, 1 when it is gone. */
    public static float clearProgress(float age) {
        return Mth.clamp((age - MeteorLineTuning.CLEAR_START) / MeteorLineTuning.CLEAR_TICKS, 0.0F, 1.0F);
    }

    /** Stars ride the dome while it spreads, and scatter outward as the night clears. */
    public static double starScale(double fullRadius, double currentRadius, float age) {
        return currentRadius / fullRadius * (1.0 + MeteorLineTuning.STAR_SCATTER * clearProgress(age));
    }

    /** A star lights up at its own time, twinkles, and fades as the night clears. */
    public static float starBrightness(EntityMeteorLineNight.Star star, float age) {
        float lit = Mth.clamp((age - star.appear()) / STAR_LIGHT_TICKS, 0.0F, 1.0F);
        float twinkle = 0.65F + 0.35F * Mth.sin(age * star.speed() + star.phase());
        return lit * twinkle * (1.0F - clearProgress(age)) * star.brightness();
    }

    public static int starColor(int accent) {
        return GlowDrawing.mix(MeteorLineTuning.STAR_COLOR, accent, MeteorLineTuning.STAR_ACCENT);
    }

    /** One star: a faint halo of the CAD colour, the star's glow and a white core. */
    public static void drawStar(VertexConsumer glow, Matrix4f pose, Vector3f at, float size, int accent, int starColor,
                                float brightness) {
        GlowDrawing.glowDisc(glow, pose, at, size * MeteorLineTuning.STAR_HALO_SCALE, accent,
                brightness * MeteorLineTuning.STAR_HALO_BRIGHTNESS, 255);
        GlowDrawing.glowDisc(glow, pose, at, size, starColor, brightness, 255);
        GlowDrawing.glowDisc(glow, pose, at, size * 0.35F, 0xFFFFFF, brightness, 255);
    }
}
