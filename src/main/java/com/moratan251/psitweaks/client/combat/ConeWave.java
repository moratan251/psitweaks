package com.moratan251.psitweaks.client.combat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickGramDemolition;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The shock wave of Trick: Gram Demolition, thrown forward through the trick's cone in the CAD colour. A curved front
 * sweeps out to the full reach and opens up to the cone's width, faint inside and bright at its rim; rings stay behind
 * where the rim passed, light streaks fly out with the front, and a flash and a quick ring mark where it starts.
 * <p>
 * The tip of the cone is the caster's eyes, and the wave starts {@link #START} blocks in front of them. Together with
 * the front opening up as it goes, this makes the rim spread out from the middle of the caster's own view.
 */
final class ConeWave {
    /** How far in front of the tip the wave starts. */
    private static final float START = 0.6F;
    /** The part of the effect's life the front takes to reach full length; it fades over the rest. */
    private static final float TRAVEL = 0.45F;
    /** How far the front drifts on past its reach (in parts of the reach) while it fades, so it does not stop dead. */
    private static final float DRIFT = 0.08F;
    /** How wide the front is when it starts, as a part of the cone's half angle. */
    private static final float START_SPREAD = 0.35F;
    /** How much wider than the cone the front opens while it fades, as a part of the cone's half angle. */
    private static final float FADE_SPREAD = 0.15F;
    /** The rings of the front's mesh, in parts of its spread. They crowd towards the rim, where the light changes fast. */
    private static final float[] FRONT_RINGS = {0.0F, 0.35F, 0.6F, 0.76F, 0.87F, 0.94F, 1.0F, 1.05F, 1.1F};
    private static final int SEGMENTS = 40;
    /** The faint light inside the rim of the front. */
    private static final float FILL = 0.07F;
    /** How far the front had come (its progress, 0 to 1) where the rings left behind stand. */
    private static final float[] ECHOES = {0.3F, 0.6F, 0.85F};
    /** How long a ring left behind lasts, as a part of the effect's life. */
    private static final float ECHO_LIFE = 0.4F;
    /** The part of the effect's life the flash at the start lasts. */
    private static final float START_FLASH = 0.3F;
    private static final int STREAKS = 22;

    private final Vector3f axis;
    private final Vector3f side;
    private final Vector3f up;
    private final float reach;
    private final float halfAngle;
    private final int color;
    /** The unit directions square to the axis, one per segment of the front. */
    private final Vector3f[] radials;
    private final float[] frontShades;
    private final int[] frontColors;
    private final Streak[] streaks;

    /**
     * One light streak: how far off the axis it flies (as a part of the front's spread) and to which side, its speed
     * against the front's, its length and its width at the head.
     */
    private record Streak(float polar, Vector3f radial, float speed, float length, float width) {
    }

    /**
     * @param axis   the way the cone opens
     * @param length how far the cone reaches from its tip
     * @param color  the glow colour (already brightened for additive drawing)
     */
    ConeWave(Vector3f axis, float length, int color) {
        this.axis = new Vector3f(axis).normalize();
        Vector3f helper = Math.abs(this.axis.y) < 0.95F ? new Vector3f(0.0F, 1.0F, 0.0F) : new Vector3f(1.0F, 0.0F, 0.0F);
        this.side = new Vector3f(helper).cross(this.axis).normalize();
        this.up = new Vector3f(this.axis).cross(side).normalize();
        this.reach = Math.max(1.0F, length - START);
        this.halfAngle = (float) Math.toRadians(PieceTrickGramDemolition.HALF_ANGLE_DEGREES);
        this.color = color;

        radials = new Vector3f[SEGMENTS];
        for (int i = 0; i < SEGMENTS; i++) {
            radials[i] = radial(Mth.TWO_PI * i / SEGMENTS);
        }
        frontShades = new float[FRONT_RINGS.length];
        frontColors = new int[FRONT_RINGS.length];
        for (int ring = 0; ring < FRONT_RINGS.length; ring++) {
            float part = FRONT_RINGS[ring];
            // Bright at the rim with a white-hot edge, a little light inside, dark just past the rim.
            float fromRim = (part - 1.0F) / (part <= 1.0F ? 0.22F : 0.05F);
            float rim = (float) Math.exp(-fromRim * fromRim);
            frontShades[ring] = (part <= 1.0F ? FILL : 0.0F) + rim;
            frontColors[ring] = GlowDrawing.whiten(color, 0.5F * rim);
        }

        RandomSource random = RandomSource.create();
        streaks = new Streak[STREAKS];
        for (int i = 0; i < STREAKS; i++) {
            streaks[i] = new Streak((float) Math.sqrt(random.nextFloat()), radial(random.nextFloat() * Mth.TWO_PI),
                    0.7F + 0.4F * random.nextFloat(), 1.2F + 1.8F * random.nextFloat(), 0.05F + 0.06F * random.nextFloat());
        }
    }

    /** The unit direction square to the axis, turned {@code azimuth} radians around it. */
    private Vector3f radial(float azimuth) {
        return new Vector3f(side).mul(Mth.cos(azimuth)).add(new Vector3f(up).mul(Mth.sin(azimuth)));
    }

    /** The unit direction {@code polar} radians off the axis towards {@code radial}. */
    private Vector3f direction(float polar, Vector3f radial) {
        return new Vector3f(axis).mul(Mth.cos(polar)).add(new Vector3f(radial).mul(Mth.sin(polar)));
    }

    /** The front's half angle in radians, {@code travel} (0 to 1) of the way out and {@code after} (0 to 1) into the fade. */
    private float spread(float travel, float after) {
        float open = 1.0F - (1.0F - travel) * (1.0F - travel);
        return halfAngle * (START_SPREAD + (1.0F - START_SPREAD) * open + FADE_SPREAD * after);
    }

    /** The front's distance from the start at {@code progress} (0 to 1). */
    private float frontDistance(float progress) {
        return 0.3F + (reach - 0.3F) * progress;
    }

    /** Draws the wave {@code t} (0 to 1) through its life. {@code tip} is the tip of the cone, relative to the camera. */
    void render(VertexConsumer consumer, Matrix4f pose, Vector3f tip, float t, float strength) {
        Vector3f start = new Vector3f(axis).mul(START).add(tip);
        float travel = Math.min(1.0F, t / TRAVEL);
        float progress = 1.0F - (1.0F - travel) * (1.0F - travel) * (1.0F - travel);
        float after = t <= TRAVEL ? 0.0F : (t - TRAVEL) / (1.0F - TRAVEL);
        float front = frontDistance(progress) + reach * DRIFT * after;
        float spread = spread(travel, after);
        float fade = (1.0F - 0.35F * progress) * (1.0F - after) * (1.0F - after) * strength;

        drawStart(consumer, pose, start, tip, t, strength);
        drawFront(consumer, pose, start, front, spread, fade);
        drawEchoes(consumer, pose, start, t, strength);
        drawStreaks(consumer, pose, start, front, spread, progress, fade * (1.0F - after));
    }

    /** A flash and a ring that bursts outward where the wave starts. */
    private void drawStart(VertexConsumer consumer, Matrix4f pose, Vector3f start, Vector3f tip, float t, float strength) {
        if (t >= START_FLASH) {
            return;
        }
        float left = 1.0F - t / START_FLASH;
        // Dimmer seen from the caster's own eyes, where the flash fills the middle of the view.
        float near = tip.lengthSquared() < 4.0F ? 0.5F : 1.0F;
        GlowDrawing.glowDisc(consumer, pose, start, 0.5F, GlowDrawing.whiten(color, 0.5F), 0.8F * left * near * strength, 255);
        float radius = 0.25F + 1.2F * (1.0F - left * left);
        softRing(consumer, pose, start, axis, radius, 0.18F, GlowDrawing.whiten(color, 0.3F), 0.9F * left * left * strength, 32);
    }

    /** The front: a piece of a sphere around the start, {@code spread} radians wide around the axis. */
    private void drawFront(VertexConsumer consumer, Matrix4f pose, Vector3f start, float front, float spread, float fade) {
        if (fade <= 0.01F) {
            return;
        }
        Vector3f[] previous = null;
        for (int ring = 0; ring < FRONT_RINGS.length; ring++) {
            float polar = spread * FRONT_RINGS[ring];
            Vector3f[] points = new Vector3f[SEGMENTS];
            for (int i = 0; i < SEGMENTS; i++) {
                points[i] = direction(polar, radials[i]).mul(front).add(start);
            }
            if (previous != null) {
                int innerColor = frontColors[ring - 1];
                int outerColor = frontColors[ring];
                float inner = frontShades[ring - 1] * fade;
                float outer = frontShades[ring] * fade;
                for (int i = 0; i < SEGMENTS; i++) {
                    int next = (i + 1) % SEGMENTS;
                    GlowDrawing.vertex(consumer, pose, previous[i], innerColor, inner, 255);
                    GlowDrawing.vertex(consumer, pose, points[i], outerColor, outer, 255);
                    GlowDrawing.vertex(consumer, pose, points[next], outerColor, outer, 255);
                    GlowDrawing.vertex(consumer, pose, previous[next], innerColor, inner, 255);
                }
            }
            previous = points;
        }
    }

    /** Rings left standing where the rim of the front passed; they widen a little as they fade. */
    private void drawEchoes(VertexConsumer consumer, Matrix4f pose, Vector3f start, float t, float strength) {
        int echoColor = GlowDrawing.whiten(color, 0.25F);
        for (float progress : ECHOES) {
            // When the front got there: progress = 1 - (1 - travel)^3 solved for travel.
            float travel = 1.0F - (float) Math.cbrt(1.0F - progress);
            float age = (t - TRAVEL * travel) / ECHO_LIFE;
            if (age < 0.0F || age >= 1.0F) {
                continue;
            }
            float distance = frontDistance(progress);
            float spread = spread(travel, 0.0F);
            float left = 1.0F - age;
            Vector3f center = new Vector3f(axis).mul(distance * Mth.cos(spread)).add(start);
            float radius = distance * Mth.sin(spread) * (1.0F + 0.25F * (1.0F - left * left));
            softRing(consumer, pose, center, axis, radius, 0.12F + 0.05F * distance, echoColor,
                    0.7F * left * left * strength, SEGMENTS);
        }
    }

    /** Light streaks flying out with the front, bright at the head and fading to nothing at the tail. */
    private void drawStreaks(VertexConsumer consumer, Matrix4f pose, Vector3f start, float front, float spread,
                             float progress, float fade) {
        if (fade <= 0.01F) {
            return;
        }
        int streakColor = GlowDrawing.whiten(color, 0.45F);
        for (Streak streak : streaks) {
            float head = Math.min(front * streak.speed(), reach * (1.0F + DRIFT));
            float tail = Math.max(0.0F, head - streak.length() * (0.4F + 0.6F * progress));
            if (head - tail < 0.05F) {
                continue;
            }
            Vector3f direction = direction(spread * streak.polar(), streak.radial());
            Vector3f from = new Vector3f(direction).mul(tail).add(start);
            Vector3f to = direction.mul(head).add(start);
            GlowDrawing.ribbon(consumer, pose, from, to, streak.width() * 0.3F, streak.width(), streakColor,
                    0.0F, 1.1F * fade, 255);
        }
    }

    /** A flat ring of the given width, brightest along {@code radius} and fading out to both sides. */
    private static void softRing(VertexConsumer consumer, Matrix4f pose, Vector3f center, Vector3f normal, float radius,
                                 float width, int color, float brightness, int segments) {
        GlowDrawing.ring(consumer, pose, center, normal, Math.max(0.0F, radius - width), radius, segments, color,
                0.0F, brightness, 255);
        GlowDrawing.ring(consumer, pose, center, normal, radius, radius + width, segments, color, brightness, 0.0F, 255);
    }
}
