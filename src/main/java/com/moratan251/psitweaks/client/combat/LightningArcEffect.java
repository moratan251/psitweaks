package com.moratan251.psitweaks.client.combat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.common.entities.combat.EntityChainLightning;
import com.moratan251.psitweaks.common.network.MessageLightningArcs;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** The arcs of one Trick: Chain Lightning cast. Each jump lights up a little after the previous one. */
final class LightningArcEffect {
    /** Ticks between one jump and the next, the same pace as the hits on the server. */
    static final int STAGGER = EntityChainLightning.STAGGER;
    /** How long each arc stays visible. */
    private static final int ARC_LIFETIME = 9;
    /** The jagged path is re-rolled this often, which makes the arc crackle. */
    private static final int FLICKER_TICKS = 2;

    private final List<Vec3> points;
    private final int color;
    private final long seed;
    private final int lifetime;
    private int age;

    LightningArcEffect(MessageLightningArcs message) {
        this.points = message.points();
        this.color = GlowDrawing.glowColor(message.color());
        this.seed = message.seed();
        this.lifetime = Math.max(0, points.size() - 2) * STAGGER + ARC_LIFETIME;
    }

    /** Returns false when every arc has faded. */
    boolean tick(ClientLevel level) {
        for (int jump = 0; jump + 1 < points.size(); jump++) {
            if (age == jump * STAGGER) {
                sparks(level, points.get(jump + 1), jump);
            }
        }
        return ++age < lifetime;
    }

    private void sparks(ClientLevel level, Vec3 at, int jump) {
        RandomSource random = RandomSource.create(seed * 7919L + jump);
        for (int i = 0; i < 10; i++) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z,
                    (random.nextDouble() - 0.5) * 0.6, random.nextDouble() * 0.4, (random.nextDouble() - 0.5) * 0.6);
        }
    }

    void render(MultiBufferSource buffers, Matrix4f pose, Vec3 camera, float partialTick) {
        VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        float time = age + partialTick;
        for (int jump = 0; jump + 1 < points.size(); jump++) {
            float local = time - jump * STAGGER;
            if (local < 0.0F || local >= ARC_LIFETIME) {
                continue;
            }
            float brightness = local < 2.0F ? 1.0F : 1.0F - (local - 2.0F) / (ARC_LIFETIME - 2.0F);
            Vector3f from = relative(points.get(jump), camera);
            Vector3f to = relative(points.get(jump + 1), camera);
            long frame = (long) (local / FLICKER_TICKS);
            RandomSource random = RandomSource.create(seed * 31L + jump * 977L + frame * 104_729L);
            drawBolt(consumer, pose, from, to, random, color, brightness, 1.0F);
        }
    }

    private static Vector3f relative(Vec3 point, Vec3 camera) {
        return new Vector3f((float) (point.x - camera.x), (float) (point.y - camera.y), (float) (point.z - camera.z));
    }

    /**
     * Draws a jagged bolt with a soft coloured glow, a white-hot core and a couple of short forks.
     *
     * @param scale thickness multiplier
     */
    static void drawBolt(VertexConsumer consumer, Matrix4f pose, Vector3f from, Vector3f to, RandomSource random,
                         int color, float brightness, float scale) {
        Vector3f[] path = jaggedPath(from, to, random, 0.16F);
        int core = GlowDrawing.whiten(color, 0.75F);
        for (int i = 0; i + 1 < path.length; i++) {
            GlowDrawing.ribbon(consumer, pose, path[i], path[i + 1], 0.32F * scale, color, 0.28F * brightness, 255);
            GlowDrawing.ribbon(consumer, pose, path[i], path[i + 1], 0.07F * scale, core, 1.0F * brightness, 255);
        }
        int forks = 1 + random.nextInt(2);
        float length = from.distance(to);
        for (int f = 0; f < forks && path.length > 4; f++) {
            Vector3f start = path[1 + random.nextInt(path.length - 3)];
            Vector3f direction = new Vector3f(to).sub(from).normalize()
                    .add((random.nextFloat() - 0.5F) * 1.6F, (random.nextFloat() - 0.5F) * 1.6F, (random.nextFloat() - 0.5F) * 1.6F)
                    .normalize();
            Vector3f end = new Vector3f(direction).mul(length * (0.15F + random.nextFloat() * 0.2F)).add(start);
            Vector3f[] fork = jaggedPath(start, end, random, 0.2F);
            for (int i = 0; i + 1 < fork.length; i++) {
                float fade = 1.0F - (float) i / fork.length;
                GlowDrawing.ribbon(consumer, pose, fork[i], fork[i + 1], 0.04F * scale, core, 0.7F * brightness * fade, 255);
            }
        }
    }

    /** Midpoint displacement: each pass splits every segment and pushes the new point sideways. */
    private static Vector3f[] jaggedPath(Vector3f from, Vector3f to, RandomSource random, float roughness) {
        float length = from.distance(to);
        int passes = Mth.clamp(Mth.ceil(Math.log(Math.max(1.0F, length * 2.0F)) / Math.log(2.0)), 2, 5);
        Vector3f axis = new Vector3f(to).sub(from);
        if (axis.lengthSquared() < 1.0E-6F) {
            return new Vector3f[]{from, to};
        }
        axis.normalize();
        Vector3f helper = Math.abs(axis.y) < 0.95F ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
        Vector3f u = new Vector3f(axis).cross(helper).normalize();
        Vector3f v = new Vector3f(axis).cross(u).normalize();

        Vector3f[] path = {new Vector3f(from), new Vector3f(to)};
        float amplitude = length * roughness;
        for (int pass = 0; pass < passes; pass++) {
            Vector3f[] next = new Vector3f[path.length * 2 - 1];
            for (int i = 0; i + 1 < path.length; i++) {
                next[i * 2] = path[i];
                Vector3f middle = new Vector3f(path[i]).add(path[i + 1]).mul(0.5F);
                middle.add(new Vector3f(u).mul((random.nextFloat() - 0.5F) * 2.0F * amplitude));
                middle.add(new Vector3f(v).mul((random.nextFloat() - 0.5F) * 2.0F * amplitude));
                next[i * 2 + 1] = middle;
            }
            next[next.length - 1] = path[path.length - 1];
            path = next;
            amplitude *= 0.55F;
        }
        return path;
    }
}
