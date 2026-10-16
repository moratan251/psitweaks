package com.moratan251.psitweaks.client.combat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.common.network.MessageSphereFlash;
import com.moratan251.psitweaks.common.network.MessageSphereFlash.Style;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** One running sphere flash: the honeycomb shield of Trick: Barrier, or a plain shock sphere. */
final class SphereFlashEffect {
    /** How far the cell edges reach into each cell; the inner part is the dim fill. */
    private static final float EDGE_INSET = 0.80F;

    private final MessageSphereFlash message;
    private final int duration;
    private final int color;
    private final Vector3f impact;
    private int age;

    SphereFlashEffect(MessageSphereFlash message) {
        this.message = message;
        this.duration = durationOf(message.style());
        this.color = GlowDrawing.glowColor(message.color());
        Vec3 direction = message.impactDirection();
        this.impact = direction.lengthSqr() > 1.0E-6
                ? new Vector3f((float) direction.x, (float) direction.y, (float) direction.z).normalize()
                : null;
    }

    private static int durationOf(Style style) {
        return switch (style) {
            case BARRIER_RAISE -> 14;
            case BARRIER_HIT -> 10;
            case BARRIER_BREAK -> 16;
            case BARRIER_FADE -> 12;
            case SHOCKWAVE -> 10;
            case IMPLOSION -> 16;
        };
    }

    /** Returns false when the flash is over. */
    boolean tick() {
        return ++age < duration;
    }

    void render(ClientLevel level, MultiBufferSource buffers, Matrix4f pose, Vec3 camera, float partialTick) {
        float t = Mth.clamp((age + partialTick) / duration, 0.0F, 1.0F);
        Vec3 worldCenter = center(level, partialTick);
        Vector3f center = new Vector3f((float) (worldCenter.x - camera.x), (float) (worldCenter.y - camera.y),
                (float) (worldCenter.z - camera.z));
        float strength = Mth.clamp(message.strength(), 0.2F, 2.0F);
        switch (message.style()) {
            case SHOCKWAVE -> renderShockwave(buffers, pose, center, t, strength);
            case IMPLOSION -> renderImplosion(buffers, pose, center, t, strength);
            default -> renderHoneycomb(buffers, pose, center, t, strength);
        }
    }

    private Vec3 center(ClientLevel level, float partialTick) {
        if (message.entityId() != MessageSphereFlash.NO_ENTITY) {
            Entity entity = level.getEntity(message.entityId());
            if (entity != null) {
                return entity.getPosition(partialTick).add(0.0, entity.getBbHeight() * 0.5, 0.0);
            }
        }
        return message.position();
    }

    private void renderShockwave(MultiBufferSource buffers, Matrix4f pose, Vector3f center, float t, float strength) {
        float eased = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
        float radius = message.radius() * (0.15F + 0.85F * eased);
        float fade = (1.0F - t) * (1.0F - t);
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        GlowDrawing.rimSphere(consumer, pose, center, radius, color, 0.08F * fade * strength, 0.9F * fade * strength, 255);
        if (t < 0.35F) {
            float flash = 1.0F - t / 0.35F;
            GlowDrawing.rimSphere(consumer, pose, center, Math.max(0.2F, radius * 0.35F), GlowDrawing.whiten(color, 0.6F),
                    0.5F * flash * strength, 0.5F * flash * strength, 255);
        }
    }

    private void renderImplosion(MultiBufferSource buffers, Matrix4f pose, Vector3f center, float t, float strength) {
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        if (t < 0.7F) {
            float phase = t / 0.7F;
            float radius = message.radius() * (1.0F - phase * phase) + 0.15F;
            float brightness = (0.25F + phase) * strength;
            GlowDrawing.rimSphere(consumer, pose, center, radius, color, 0.05F * brightness, 0.8F * brightness, 255);
        } else {
            float phase = (t - 0.7F) / 0.3F;
            float radius = 0.2F + message.radius() * 0.6F * phase;
            float fade = (1.0F - phase) * strength;
            int white = GlowDrawing.whiten(color, 0.7F);
            GlowDrawing.rimSphere(consumer, pose, center, radius, white, 0.6F * fade, 1.2F * fade, 255);
        }
    }

    private void renderHoneycomb(MultiBufferSource buffers, Matrix4f pose, Vector3f center, float t, float strength) {
        HoneycombSphereMesh mesh = HoneycombSphereMesh.INSTANCE;
        Style style = message.style();
        float radius = message.radius();
        if (style == Style.BARRIER_RAISE) {
            float grow = Math.min(1.0F, t * 2.0F);
            radius *= 0.8F + 0.2F * (1.0F - (1.0F - grow) * (1.0F - grow));
        }
        boolean inside = center.lengthSquared() < radius * radius;

        VertexConsumer fill = buffers.getBuffer(PsitweaksRenderTypes.TRANSLUCENT_COLOR);
        int[] cellAlpha = new int[mesh.cellCount()];
        float[] cellBrightness = new float[mesh.cellCount()];
        Vector3f[] cellOffset = new Vector3f[mesh.cellCount()];
        float[] cellScale = new float[mesh.cellCount()];

        for (int i = 0; i < mesh.cellCount(); i++) {
            Vector3f normal = mesh.centers[i];
            float brightness = cellBrightness(style, normal, mesh.seeds[i], t) * strength;
            Vector3f offset = new Vector3f();
            float scale = 1.0F;
            if (style == Style.BARRIER_BREAK) {
                float fly = 1.0F - (1.0F - t) * (1.0F - t);
                offset.set(normal).mul(radius * 0.7F * fly * (0.6F + 0.8F * mesh.seeds[i]));
                scale = 1.0F - 0.6F * t;
            }
            Vector3f cellCenter = new Vector3f(normal).mul(radius).add(center).add(offset);
            float facing = facing(normal, cellCenter);
            float rim = 1.0F - Math.abs(facing);
            float side = inside ? 0.45F : (facing > 0.0F ? 1.0F : 0.5F);
            brightness *= side * (0.75F + 0.6F * rim * rim);
            cellBrightness[i] = brightness;
            cellOffset[i] = offset;
            cellScale[i] = scale;
            cellAlpha[i] = Mth.clamp((int) (255 * Math.min(0.35F, 0.12F * brightness)), 0, 255);
        }

        for (int i = 0; i < mesh.cellCount(); i++) {
            if (cellAlpha[i] <= 2) {
                continue;
            }
            Vector3f cellCenter = new Vector3f(mesh.centers[i]).mul(radius).add(center).add(cellOffset[i]);
            Vector3f[] inner = innerCorners(mesh, i, radius, center, cellOffset[i], cellScale[i], EDGE_INSET);
            for (int k = 0; k < inner.length; k++) {
                Vector3f next = inner[(k + 1) % inner.length];
                GlowDrawing.quad(fill, pose, cellCenter, inner[k], next, next, color, 1.0F, cellAlpha[i]);
            }
        }

        VertexConsumer edges = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        for (int i = 0; i < mesh.cellCount(); i++) {
            float brightness = cellBrightness[i];
            if (brightness <= 0.01F) {
                continue;
            }
            Vector3f[] outer = innerCorners(mesh, i, radius, center, cellOffset[i], cellScale[i], 1.0F);
            Vector3f[] inner = innerCorners(mesh, i, radius, center, cellOffset[i], cellScale[i], EDGE_INSET);
            int edgeColor = GlowDrawing.whiten(color, Math.min(0.6F, brightness * 0.25F));
            float edgeBrightness = Math.min(1.6F, brightness) * 0.85F;
            for (int k = 0; k < outer.length; k++) {
                int n = (k + 1) % outer.length;
                GlowDrawing.quad(edges, pose, outer[k], outer[n], inner[n], inner[k], edgeColor, edgeBrightness, 255);
            }
        }
    }

    /** Corners of a cell pulled towards its centre by {@code inset} (1 = the real corners), on the sphere. */
    private static Vector3f[] innerCorners(HoneycombSphereMesh mesh, int cell, float radius, Vector3f center,
                                           Vector3f offset, float scale, float inset) {
        Vector3f cellNormal = mesh.centers[cell];
        Vector3f[] corners = mesh.corners[cell];
        Vector3f[] result = new Vector3f[corners.length];
        float factor = inset * scale;
        for (int k = 0; k < corners.length; k++) {
            Vector3f direction = new Vector3f(corners[k]).sub(cellNormal).mul(factor).add(cellNormal).normalize();
            result[k] = direction.mul(radius).add(center).add(offset);
        }
        return result;
    }

    /** Positive when the outer side of the surface at this point faces the camera (at the origin). */
    private static float facing(Vector3f normal, Vector3f position) {
        float length = position.length();
        if (length < 1.0E-5F) {
            return 0.0F;
        }
        return -normal.dot(position) / length;
    }

    private float cellBrightness(Style style, Vector3f normal, float seed, float t) {
        return switch (style) {
            case BARRIER_RAISE -> {
                float height = (normal.y + 1.0F) * 0.5F;
                float sweep = t * 1.6F;
                if (height > sweep) {
                    yield 0.0F;
                }
                float front = (height - sweep) / 0.12F;
                float envelope = t < 0.6F ? 1.0F : 1.0F - (t - 0.6F) / 0.4F;
                yield envelope * (0.45F + 1.2F * (float) Math.exp(-front * front));
            }
            case BARRIER_HIT -> {
                float envelope = (float) Math.pow(1.0F - t, 1.5);
                if (impact == null) {
                    yield envelope * 0.8F;
                }
                float alignment = normal.dot(impact);
                float spot = (float) Math.pow(Math.max(0.0F, alignment), 3.0);
                float angle = (float) Math.acos(Mth.clamp(alignment, -1.0F, 1.0F));
                float ripple = (angle - t * 2.4F) / 0.3F;
                yield envelope * (0.3F + 1.5F * spot + 0.9F * (float) Math.exp(-ripple * ripple));
            }
            case BARRIER_BREAK -> {
                float flash = t < 0.15F ? 1.0F - t / 0.15F : 0.0F;
                yield (float) Math.pow(1.0F - t, 1.2) * (0.8F + 0.6F * seed) + flash;
            }
            case BARRIER_FADE -> seed > t ? 0.45F * (1.0F - t) : 0.0F;
            default -> 0.0F;
        };
    }
}
