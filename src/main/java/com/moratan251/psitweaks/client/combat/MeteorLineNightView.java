package com.moratan251.psitweaks.client.combat;

import com.moratan251.psitweaks.client.config.PsitweaksClientConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.marker.EntityMeteorLineNight;
import com.moratan251.psitweaks.common.spells.combat.MeteorLineTuning;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Darkens the view while the camera is inside the night of Trick: Meteor Line: the fog turns to the night's colour
 * and closes in, and the sky goes dark. It only changes how things look (no Darkness or Blindness) and can be turned
 * off in the client config. The strength is in {@link MeteorLineTuning#SCREEN_DARKNESS}.
 */
public final class MeteorLineNightView {
    private static final double SEARCH = MeteorLineTuning.MAX_RADIUS + 2.0;
    /** How much of the way to the target darkness is covered per tick, so walking in and out does not snap. */
    private static final float EASE = 0.3F;
    private static final float SKY_FOG_END = 1.0F;

    private static float darkness;
    private static float previousDarkness;

    private MeteorLineNightView() {
    }

    static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(MeteorLineNightView::onComputeFogColor);
        NeoForge.EVENT_BUS.addListener(MeteorLineNightView::onRenderFog);
    }

    /** Follows the nights around the camera; called once per client tick. */
    static void tick(ClientLevel level, Entity camera) {
        previousDarkness = darkness;
        float target = 0.0F;
        if (PsitweaksClientConfig.CLIENT.meteorLineNight.get()) {
            Vec3 eye = camera.getEyePosition();
            for (EntityMeteorLineNight night : level.getEntitiesOfClass(EntityMeteorLineNight.class,
                    camera.getBoundingBox().inflate(SEARCH))) {
                if (night.contains(eye, night.getCurrentRadius(0.0F))) {
                    target = Math.max(target, night.getStrength(0.0F));
                }
            }
        }
        darkness += (target - darkness) * EASE;
        if (target == 0.0F && darkness < 0.001F) {
            darkness = 0.0F;
        }
    }

    static void clear() {
        darkness = 0.0F;
        previousDarkness = 0.0F;
    }

    /** How dark the view is now, 0..{@link MeteorLineTuning#SCREEN_DARKNESS}. */
    private static float darkness(double partialTick) {
        return Mth.lerp((float) partialTick, previousDarkness, darkness) * MeteorLineTuning.SCREEN_DARKNESS;
    }

    private static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        float amount = darkness(event.getPartialTick());
        if (amount <= 0.0F) {
            return;
        }
        int night = MeteorLineTuning.NIGHT_COLOR;
        event.setRed(Mth.lerp(amount, event.getRed(), GlowDrawing.red(night) / 255.0F));
        event.setGreen(Mth.lerp(amount, event.getGreen(), GlowDrawing.green(night) / 255.0F));
        event.setBlue(Mth.lerp(amount, event.getBlue(), GlowDrawing.blue(night) / 255.0F));
    }

    private static void onRenderFog(ViewportEvent.RenderFog event) {
        float amount = darkness(event.getPartialTick());
        if (amount <= 0.0F || event.getType() != FogType.NONE) {
            return;
        }
        float near = event.getNearPlaneDistance();
        float far = event.getFarPlaneDistance();
        if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            event.setNearPlaneDistance(Mth.lerp(amount, near, 0.0F));
            event.setFarPlaneDistance(Mth.lerp(amount, far, SKY_FOG_END));
        } else {
            event.setNearPlaneDistance(Math.min(near, Mth.lerp(amount, near, MeteorLineTuning.FOG_START)));
            event.setFarPlaneDistance(Math.min(far, Mth.lerp(amount, far, MeteorLineTuning.FOG_END)));
        }
        event.setCanceled(true);
    }
}
