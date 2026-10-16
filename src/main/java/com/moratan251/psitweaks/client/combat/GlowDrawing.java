package com.moratan251.psitweaks.client.combat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Small geometry helpers for the glowing combat effects. All of them write POSITION_COLOR quads; positions are
 * relative to the camera (the camera sits at the origin), which camera-facing shapes rely on.
 */
public final class GlowDrawing {
    private GlowDrawing() {
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    /** Mixes the colour towards white; 0 keeps it, 1 is pure white. */
    public static int whiten(int color, float amount) {
        float t = Mth.clamp(amount, 0.0F, 1.0F);
        int r = (int) Mth.lerp(t, red(color), 255);
        int g = (int) Mth.lerp(t, green(color), 255);
        int b = (int) Mth.lerp(t, blue(color), 255);
        return r << 16 | g << 8 | b;
    }

    /** Brightens a dark CAD colour so additive glows stay visible; keeps the hue. */
    public static int glowColor(int color) {
        int max = Math.max(red(color), Math.max(green(color), blue(color)));
        if (max >= 160) {
            return color;
        }
        if (max <= 0) {
            return 0xA0A0A0;
        }
        float scale = 160.0F / max;
        return Math.min(255, (int) (red(color) * scale)) << 16
                | Math.min(255, (int) (green(color) * scale)) << 8
                | Math.min(255, (int) (blue(color) * scale));
    }

    public static void vertex(VertexConsumer consumer, Matrix4f pose, Vector3f position, int color, float brightness, int alpha) {
        float b = Math.max(0.0F, brightness);
        consumer.addVertex(pose, position.x, position.y, position.z)
                .setColor(Math.min(255, (int) (red(color) * b)), Math.min(255, (int) (green(color) * b)),
                        Math.min(255, (int) (blue(color) * b)), Mth.clamp(alpha, 0, 255));
    }

    public static void quad(VertexConsumer consumer, Matrix4f pose, Vector3f a, Vector3f b, Vector3f c, Vector3f d,
                            int color, float brightness, int alpha) {
        vertex(consumer, pose, a, color, brightness, alpha);
        vertex(consumer, pose, b, color, brightness, alpha);
        vertex(consumer, pose, c, color, brightness, alpha);
        vertex(consumer, pose, d, color, brightness, alpha);
    }

    /** A flat strip from {@code from} to {@code to} that always faces the camera. */
    public static void ribbon(VertexConsumer consumer, Matrix4f pose, Vector3f from, Vector3f to, float width,
                              int color, float brightness, int alpha) {
        ribbon(consumer, pose, from, to, width, width, color, brightness, brightness, alpha);
    }

    /** A camera-facing strip whose width and brightness change from one end to the other (for tapering trails). */
    public static void ribbon(VertexConsumer consumer, Matrix4f pose, Vector3f from, Vector3f to,
                              float fromWidth, float toWidth, int color, float fromBrightness, float toBrightness, int alpha) {
        Vector3f direction = new Vector3f(to).sub(from);
        Vector3f middle = new Vector3f(from).add(to).mul(0.5F);
        Vector3f side = direction.cross(middle);
        float length = side.length();
        if (length < 1.0E-6F) {
            return;
        }
        side.div(length);
        Vector3f fromSide = new Vector3f(side).mul(fromWidth * 0.5F);
        Vector3f toSide = new Vector3f(side).mul(toWidth * 0.5F);
        vertex(consumer, pose, new Vector3f(from).add(fromSide), color, fromBrightness, alpha);
        vertex(consumer, pose, new Vector3f(from).sub(fromSide), color, fromBrightness, alpha);
        vertex(consumer, pose, new Vector3f(to).sub(toSide), color, toBrightness, alpha);
        vertex(consumer, pose, new Vector3f(to).add(toSide), color, toBrightness, alpha);
    }

    /** A camera-facing disc that is brightest in the middle and fades to nothing at its edge: a soft glow. */
    public static void glowDisc(VertexConsumer consumer, Matrix4f pose, Vector3f center, float radius,
                                int color, float brightness, int alpha) {
        Vector3f forward = new Vector3f(center);
        if (forward.lengthSquared() < 1.0E-8F || radius <= 0.0F) {
            return;
        }
        forward.normalize();
        Vector3f helper = Math.abs(forward.y) < 0.95F ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
        Vector3f right = new Vector3f(forward).cross(helper).normalize();
        Vector3f up = new Vector3f(right).cross(forward).normalize();
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            Vector3f p0 = new Vector3f(right).mul(Mth.cos(a0) * radius).add(new Vector3f(up).mul(Mth.sin(a0) * radius)).add(center);
            Vector3f p1 = new Vector3f(right).mul(Mth.cos(a1) * radius).add(new Vector3f(up).mul(Mth.sin(a1) * radius)).add(center);
            // A quad with two corners on the centre is a triangle; the colour fades out towards the rim.
            vertex(consumer, pose, center, color, brightness, alpha);
            vertex(consumer, pose, center, color, brightness, alpha);
            vertex(consumer, pose, p0, color, 0.0F, alpha);
            vertex(consumer, pose, p1, color, 0.0F, alpha);
        }
    }

    /** A strip lying flat on a horizontal plane, from {@code from} to {@code to} (cracks on the ground). */
    public static void flatStrip(VertexConsumer consumer, Matrix4f pose, Vector3f from, Vector3f to, float width,
                                 int color, float brightness, int alpha) {
        Vector3f side = new Vector3f(-(to.z - from.z), 0.0F, to.x - from.x);
        float length = side.length();
        if (length < 1.0E-6F) {
            return;
        }
        side.mul(width * 0.5F / length);
        quad(consumer, pose, new Vector3f(from).add(side), new Vector3f(from).sub(side),
                new Vector3f(to).sub(side), new Vector3f(to).add(side), color, brightness, alpha);
    }

    /**
     * A flat ring around {@code center} in the plane perpendicular to {@code normal}. The inner and outer edges get
     * their own brightness, so a ring can fade out towards one side.
     */
    public static void ring(VertexConsumer consumer, Matrix4f pose, Vector3f center, Vector3f normal,
                            float innerRadius, float outerRadius, int segments, int color,
                            float innerBrightness, float outerBrightness, int alpha) {
        Vector3f axis = new Vector3f(normal).normalize();
        Vector3f helper = Math.abs(axis.y) < 0.95F ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
        Vector3f u = new Vector3f(helper).cross(axis).normalize();
        Vector3f v = new Vector3f(axis).cross(u).normalize();
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            Vector3f d0 = new Vector3f(u).mul(Mth.cos(a0)).add(new Vector3f(v).mul(Mth.sin(a0)));
            Vector3f d1 = new Vector3f(u).mul(Mth.cos(a1)).add(new Vector3f(v).mul(Mth.sin(a1)));
            vertex(consumer, pose, new Vector3f(d0).mul(innerRadius).add(center), color, innerBrightness, alpha);
            vertex(consumer, pose, new Vector3f(d0).mul(outerRadius).add(center), color, outerBrightness, alpha);
            vertex(consumer, pose, new Vector3f(d1).mul(outerRadius).add(center), color, outerBrightness, alpha);
            vertex(consumer, pose, new Vector3f(d1).mul(innerRadius).add(center), color, innerBrightness, alpha);
        }
    }

    /**
     * A smooth sphere whose surface is brightest at its silhouette (a soap-bubble rim). {@code core} is the
     * brightness facing the camera, {@code rim} the extra brightness at the edge.
     */
    public static void rimSphere(VertexConsumer consumer, Matrix4f pose, Vector3f center, float radius,
                                 int color, float core, float rim, int alpha) {
        // Finer for large spheres, so the facets of a wide flash do not show.
        int stacks = Mth.clamp(Mth.ceil(radius * 6.0F), 14, 64);
        int slices = stacks * 2;
        Vector3f[] previous = null;
        float[] previousBrightness = null;
        for (int stack = 0; stack <= stacks; stack++) {
            float phi = Mth.PI * stack / stacks;
            Vector3f[] ring = new Vector3f[slices + 1];
            float[] brightness = new float[slices + 1];
            for (int slice = 0; slice <= slices; slice++) {
                float theta = Mth.TWO_PI * slice / slices;
                Vector3f normal = new Vector3f(Mth.sin(phi) * Mth.cos(theta), Mth.cos(phi), Mth.sin(phi) * Mth.sin(theta));
                Vector3f position = new Vector3f(normal).mul(radius).add(center);
                ring[slice] = position;
                Vector3f view = new Vector3f(position);
                float facing = view.lengthSquared() < 1.0E-6F ? 0.0F : Math.abs(normal.dot(view.normalize()));
                float edge = 1.0F - facing;
                brightness[slice] = core + rim * edge * edge;
            }
            if (previous != null) {
                for (int slice = 0; slice < slices; slice++) {
                    vertex(consumer, pose, previous[slice], color, previousBrightness[slice], alpha);
                    vertex(consumer, pose, ring[slice], color, brightness[slice], alpha);
                    vertex(consumer, pose, ring[slice + 1], color, brightness[slice + 1], alpha);
                    vertex(consumer, pose, previous[slice + 1], color, previousBrightness[slice + 1], alpha);
                }
            }
            previous = ring;
            previousBrightness = brightness;
        }
    }
}
