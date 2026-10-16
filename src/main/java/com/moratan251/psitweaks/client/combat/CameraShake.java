package com.moratan251.psitweaks.client.combat;

import com.moratan251.psitweaks.client.config.PsitweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Camera shake for heavy impacts. Strength is in degrees of jitter and decays every tick. */
public final class CameraShake {
    private static final float MAX_TRAUMA = 1.5F;
    private static final float DECAY_PER_TICK = 0.08F;
    private static final float MAX_DEGREES = 3.0F;

    private static float trauma;
    private static float sustained;

    private CameraShake() {
    }

    /** A one-off kick, e.g. a meteor impact. */
    public static void kick(float amount) {
        trauma = Math.min(MAX_TRAUMA, trauma + amount);
    }

    /** A kick that falls off with distance from the source; nothing beyond {@code range}. */
    public static void kickFrom(Vec3 source, double range, float amount) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        double distance = minecraft.player.position().distanceTo(source);
        if (distance >= range) {
            return;
        }
        kick((float) (amount * (1.0 - distance / range)));
    }

    /** Keeps the camera shaking at least this much during the current tick (a rumbling shockwave). */
    public static void sustain(float amount) {
        sustained = Math.max(sustained, Math.min(MAX_TRAUMA, amount));
    }

    static void tick() {
        trauma = Math.max(0.0F, trauma - DECAY_PER_TICK);
        sustained = 0.0F;
    }

    static void clear() {
        trauma = 0.0F;
        sustained = 0.0F;
    }

    static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float level = Math.max(trauma, sustained);
        if (level <= 0.0F || !PsitweaksClientConfig.CLIENT.cameraShake.get()) {
            return;
        }
        float strength = (float) (level * level * MAX_DEGREES * PsitweaksClientConfig.CLIENT.cameraShakeStrength.get());
        if (strength <= 0.0F) {
            return;
        }
        double time = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L)
                + event.getPartialTick();
        event.setYaw(event.getYaw() + strength * noise(time, 1.7));
        event.setPitch(event.getPitch() + strength * noise(time, 5.3));
        event.setRoll(event.getRoll() + strength * 0.6F * noise(time, 9.1));
    }

    /** Smooth pseudo-random wobble in [-1, 1]. */
    private static float noise(double time, double seed) {
        double t = time * 1.9 + seed * 13.0;
        return (float) (0.6 * Math.sin(t * 2.3) + 0.4 * Math.sin(t * 5.7 + seed));
    }
}
