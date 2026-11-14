package com.moratan251.psitweaks.client.combat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.client.renderer.combat.MeteorLineNightRenderer;
import com.moratan251.psitweaks.common.entities.SpellGram.marker.EntityMeteorLineNight;
import com.moratan251.psitweaks.common.network.MessageMeteorNightShatter;
import com.moratan251.psitweaks.common.spells.combat.MeteorLineTuning;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The night of Trick: Meteor Line ran out of integrity and shatters like glass (2026-11-14). The dark sphere is cut
 * into irregular shards that first fit together exactly where the night was. A crack runs from the core (the top of
 * the sphere) down to its bottom; each shard's edges flash as the crack reaches it, then the shard flies outward,
 * spins, falls and fades. The stars burst outward with a last flare. The flow is in {@link MeteorLineTuning}
 * ({@code SHATTER_*}).
 */
final class MeteorNightShatterEffect {
    private static final Vector3f UP = new Vector3f(0.0F, 1.0F, 0.0F);
    private static final int FLY_TICKS = Math.max(1, MeteorLineTuning.SHATTER_TICKS - MeteorLineTuning.SHATTER_CRACK_TICKS
            - MeteorLineTuning.SHATTER_HOLD_TICKS);
    /** How quickly the flash on a shard's edges dies down after the crack reaches it (ticks). */
    private static final float FLASH_DECAY = 4.0F;
    /** How long the flash on the core lasts (ticks). */
    private static final float CORE_FLASH_TICKS = 6.0F;

    /**
     * One shard: its corners relative to its centre and its centre relative to the night's centre (blocks), the way
     * it flies and how far, how it spins, and when the crack reaches it (ticks after the night broke).
     */
    private record Shard(Vector3f[] corners, Vector3f center, Vector3f direction, float spread, Vector3f spinAxis,
                         float spin, float crackAt) {
    }

    /** One star as it was when the night broke: its offset from the centre, size and brightness, and crack time. */
    private record Spark(Vector3f offset, float size, float brightness, float crackAt) {
    }

    private final Vec3 center;
    private final float radius;
    private final float strength;
    private final int accent;
    private final int starColor;
    private final int crackColor;
    private final float crackWidth;
    private final Shard[] shards;
    private final Spark[] sparks;
    /** Where each shard is in the frame being drawn (reused every frame). */
    private final Vector3f[][] placed;
    private final Vector3f[] middles;
    private final float[] fades;
    private final float[] scales;
    private int age;

    MeteorNightShatterEffect(MessageMeteorNightShatter message) {
        this.center = message.center();
        this.radius = Math.max(0.5F, message.currentRadius());
        this.strength = Mth.clamp(message.strength(), 0.0F, 1.0F);
        this.accent = GlowDrawing.glowColor(message.color());
        this.starColor = MeteorLineNightRenderer.starColor(accent);
        this.crackColor = GlowDrawing.whiten(GlowDrawing.mix(MeteorLineTuning.DOME_RIM_COLOR, accent, 0.35F), 0.35F);
        this.crackWidth = MeteorLineTuning.SHATTER_CRACK_WIDTH * radius / (float) MeteorLineTuning.MIN_RADIUS;
        RandomSource random = RandomSource.create(message.seed() * 31L + 0x5DEECE66DL);
        this.shards = cut(random, radius);
        this.sparks = sparks(message, radius);
        this.placed = new Vector3f[shards.length][];
        this.middles = new Vector3f[shards.length];
        for (int i = 0; i < shards.length; i++) {
            placed[i] = new Vector3f[] {new Vector3f(), new Vector3f(), new Vector3f()};
            middles[i] = new Vector3f();
        }
        this.fades = new float[shards.length];
        this.scales = new float[shards.length];
    }

    /** Returns false when the shards are gone. */
    boolean tick() {
        return ++age < MeteorLineTuning.SHATTER_TICKS;
    }

    // ---- the shards ----

    /**
     * Cuts the sphere into shards: a geodesic sphere whose corners are nudged off their even places, each of its
     * triangles split once more from a corner to a random point on the opposite side, so the shards are uneven.
     */
    private static Shard[] cut(RandomSource random, float radius) {
        List<Vector3f> vertices = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();
        icosahedron(vertices, faces);
        for (int i = 0; i < MeteorLineTuning.SHATTER_SUBDIVISIONS; i++) {
            faces = subdivide(vertices, faces);
        }
        // The angle between neighbouring corners, to nudge them by a part of it.
        float edgeAngle = 1.1071F / (1 << MeteorLineTuning.SHATTER_SUBDIVISIONS);
        for (Vector3f vertex : vertices) {
            Vector3f nudge = new Vector3f((float) random.nextGaussian(), (float) random.nextGaussian(),
                    (float) random.nextGaussian()).mul(edgeAngle * 0.22F);
            nudge.sub(new Vector3f(vertex).mul(nudge.dot(vertex)));
            vertex.add(nudge).normalize();
        }

        List<Shard> shards = new ArrayList<>(faces.size() * 2);
        for (int[] face : faces) {
            int apex = random.nextInt(3);
            Vector3f top = vertices.get(face[apex]);
            Vector3f left = vertices.get(face[(apex + 1) % 3]);
            Vector3f right = vertices.get(face[(apex + 2) % 3]);
            // On the straight side (not pushed out to the sphere), so the two halves meet the neighbour without a gap.
            Vector3f split = new Vector3f(left).lerp(right, 0.3F + 0.4F * random.nextFloat());
            shards.add(shard(random, radius, top, left, split));
            shards.add(shard(random, radius, top, split, right));
        }
        return shards.toArray(Shard[]::new);
    }

    private static Shard shard(RandomSource random, float radius, Vector3f a, Vector3f b, Vector3f c) {
        Vector3f[] corners = {new Vector3f(a).mul(radius), new Vector3f(b).mul(radius), new Vector3f(c).mul(radius)};
        Vector3f middle = new Vector3f(corners[0]).add(corners[1]).add(corners[2]).div(3.0F);
        for (Vector3f corner : corners) {
            corner.sub(middle);
        }
        Vector3f normal = new Vector3f(middle).normalize();
        Vector3f direction = new Vector3f((float) random.nextGaussian(), (float) random.nextGaussian(),
                (float) random.nextGaussian()).mul(0.25F).add(normal).normalize();
        float spread = Mth.lerp(random.nextFloat(), MeteorLineTuning.SHATTER_MIN_SPREAD, MeteorLineTuning.SHATTER_MAX_SPREAD)
                * radius;
        Vector3f axis = new Vector3f((float) random.nextGaussian(), (float) random.nextGaussian(),
                (float) random.nextGaussian());
        if (axis.lengthSquared() < 1.0E-6F) {
            axis.set(normal);
        }
        axis.normalize();
        float spin = (random.nextFloat() * 2.0F - 1.0F) * MeteorLineTuning.SHATTER_MAX_SPIN;
        return new Shard(corners, middle, direction, spread, axis, spin, crackAt(normal));
    }

    /** When the crack, running down from the core at the top, reaches this direction from the centre. */
    private static float crackAt(Vector3f direction) {
        float angle = (float) Math.acos(Mth.clamp(direction.dot(UP), -1.0F, 1.0F));
        return angle / Mth.PI * MeteorLineTuning.SHATTER_CRACK_TICKS;
    }

    private static void icosahedron(List<Vector3f> vertices, List<int[]> faces) {
        float t = (1.0F + Mth.sqrt(5.0F)) * 0.5F;
        float[][] points = {
                {-1, t, 0}, {1, t, 0}, {-1, -t, 0}, {1, -t, 0}, {0, -1, t}, {0, 1, t},
                {0, -1, -t}, {0, 1, -t}, {t, 0, -1}, {t, 0, 1}, {-t, 0, -1}, {-t, 0, 1}};
        for (float[] point : points) {
            vertices.add(new Vector3f(point[0], point[1], point[2]).normalize());
        }
        int[][] triangles = {
                {0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11}, {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6},
                {7, 1, 8}, {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9}, {4, 9, 5}, {2, 4, 11}, {6, 2, 10},
                {8, 6, 7}, {9, 8, 1}};
        faces.addAll(List.of(triangles));
    }

    /** Splits every triangle into four, with the new corners pushed out onto the sphere. */
    private static List<int[]> subdivide(List<Vector3f> vertices, List<int[]> faces) {
        Map<Long, Integer> middles = new HashMap<>();
        List<int[]> result = new ArrayList<>(faces.size() * 4);
        for (int[] face : faces) {
            int ab = middle(vertices, middles, face[0], face[1]);
            int bc = middle(vertices, middles, face[1], face[2]);
            int ca = middle(vertices, middles, face[2], face[0]);
            result.add(new int[] {face[0], ab, ca});
            result.add(new int[] {face[1], bc, ab});
            result.add(new int[] {face[2], ca, bc});
            result.add(new int[] {ab, bc, ca});
        }
        return result;
    }

    private static int middle(List<Vector3f> vertices, Map<Long, Integer> middles, int a, int b) {
        long key = (long) Math.min(a, b) << 32 | Math.max(a, b);
        return middles.computeIfAbsent(key, ignored -> {
            vertices.add(new Vector3f(vertices.get(a)).add(vertices.get(b)).normalize());
            return vertices.size() - 1;
        });
    }

    // ---- the stars ----

    /** The stars as they looked when the night broke (same layout as the night's, from its seed). */
    private static Spark[] sparks(MessageMeteorNightShatter message, float currentRadius) {
        EntityMeteorLineNight.Star[] stars = EntityMeteorLineNight.makeStars(message.seed(), message.radius());
        double scale = MeteorLineNightRenderer.starScale(message.radius(), currentRadius, message.age());
        float starSize = MeteorLineTuning.starSize(message.radius());
        List<Spark> sparks = new ArrayList<>(stars.length);
        for (EntityMeteorLineNight.Star star : stars) {
            float brightness = MeteorLineNightRenderer.starBrightness(star, message.age());
            if (brightness <= 0.01F) {
                continue;
            }
            Vec3 offset = star.offset().scale(scale);
            Vector3f at = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
            float crackAt = at.lengthSquared() < 1.0E-6F ? 0.0F : crackAt(new Vector3f(at).normalize());
            sparks.add(new Spark(at, starSize * star.scale(), brightness, crackAt));
        }
        return sparks.toArray(Spark[]::new);
    }

    // ---- drawing ----

    void render(MultiBufferSource buffers, Matrix4f pose, Vec3 camera, float partialTick) {
        float time = age + partialTick;
        Vector3f origin = new Vector3f((float) (center.x - camera.x), (float) (center.y - camera.y),
                (float) (center.z - camera.z));

        // Place every shard first: the dark fill and the glowing edges go to two buffers, and a buffer source ends the
        // buffer it handed out last when another one is asked for, so all of one kind must be drawn before the other.
        Quaternionf rotation = new Quaternionf();
        for (int i = 0; i < shards.length; i++) {
            Shard shard = shards[i];
            float flight = Math.max(0.0F, time - shard.crackAt() - MeteorLineTuning.SHATTER_HOLD_TICKS);
            float t = Math.min(1.0F, flight / FLY_TICKS);
            float fade = (float) Math.pow(1.0F - t, 1.5);
            fades[i] = fade;
            if (fade <= 0.005F) {
                continue;
            }
            float out = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
            float fall = 0.5F * MeteorLineTuning.SHATTER_GRAVITY * flight * flight;
            scales[i] = 1.0F - 0.45F * t;
            middles[i].set(shard.direction()).mul(shard.spread() * out).add(shard.center()).add(origin).sub(0.0F, fall, 0.0F);
            rotation.fromAxisAngleRad(shard.spinAxis().x, shard.spinAxis().y, shard.spinAxis().z, shard.spin() * flight);
            for (int k = 0; k < 3; k++) {
                rotation.transform(placed[i][k].set(shard.corners()[k]).mul(scales[i])).add(middles[i]);
            }
        }

        VertexConsumer dark = buffers.getBuffer(PsitweaksRenderTypes.TRANSLUCENT_COLOR);
        for (int i = 0; i < shards.length; i++) {
            int alpha = (int) (MeteorLineTuning.DOME_ALPHA * strength * fades[i]);
            if (fades[i] > 0.005F && alpha > 1) {
                Vector3f[] corners = placed[i];
                GlowDrawing.quad(dark, pose, corners[0], corners[1], corners[2], corners[2], MeteorLineTuning.NIGHT_COLOR,
                        1.0F, alpha);
            }
        }

        VertexConsumer glow = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        for (int i = 0; i < shards.length; i++) {
            float sinceCrack = time - shards[i].crackAt();
            if (fades[i] <= 0.005F || sinceCrack < 0.0F) {
                continue;
            }
            float flash = (float) Math.exp(-sinceCrack / FLASH_DECAY);
            float brightness = strength * (MeteorLineTuning.SHATTER_CRACK_BRIGHTNESS * flash + 0.35F * fades[i]);
            if (brightness > 0.01F) {
                edges(glow, pose, placed[i], middles[i], crackWidth * scales[i], brightness);
            }
        }

        // The core at the top breaks first: a brief flash where the crack starts.
        if (time < CORE_FLASH_TICKS) {
            float flash = 1.0F - time / CORE_FLASH_TICKS;
            Vector3f core = new Vector3f(origin).add(0.0F, radius, 0.0F);
            GlowDrawing.glowDisc(glow, pose, core, radius * 0.3F, crackColor, 0.9F * flash * strength, 255);
            GlowDrawing.glowDisc(glow, pose, core, radius * 0.08F, 0xFFFFFF, flash * strength, 255);
        }

        Vector3f at = new Vector3f();
        for (Spark spark : sparks) {
            float sinceCrack = time - spark.crackAt();
            float flight = Math.max(0.0F, sinceCrack - MeteorLineTuning.SHATTER_HOLD_TICKS);
            float t = Math.min(1.0F, flight / FLY_TICKS);
            float fade = (1.0F - t) * (1.0F - t);
            float flare = sinceCrack < 0.0F ? 1.0F : 1.0F + 1.2F * (float) Math.exp(-sinceCrack / 3.0F);
            float brightness = spark.brightness() * fade * flare;
            if (brightness <= 0.01F) {
                continue;
            }
            float out = 1.0F - (1.0F - t) * (1.0F - t);
            float fall = 0.25F * MeteorLineTuning.SHATTER_GRAVITY * flight * flight;
            at.set(spark.offset()).mul(1.0F + MeteorLineTuning.SHATTER_STAR_SPREAD * out).add(origin).sub(0.0F, fall, 0.0F);
            MeteorLineNightRenderer.drawStar(glow, pose, at, spark.size() * (1.0F - 0.5F * t), accent, starColor, brightness);
        }
    }

    /** The glowing edges of one shard: a thin strip along each side, on the shard, inside its outline. */
    private void edges(VertexConsumer glow, Matrix4f pose, Vector3f[] corners, Vector3f middle, float width,
                       float brightness) {
        Vector3f[] inner = new Vector3f[3];
        for (int k = 0; k < 3; k++) {
            Vector3f toMiddle = new Vector3f(middle).sub(corners[k]);
            float distance = toMiddle.length();
            float pull = distance < 1.0E-5F ? 0.0F : Math.min(0.45F, width * 2.0F / distance);
            inner[k] = new Vector3f(corners[k]).add(toMiddle.mul(pull));
        }
        for (int k = 0; k < 3; k++) {
            int n = (k + 1) % 3;
            GlowDrawing.quad(glow, pose, corners[k], corners[n], inner[n], inner[k], crackColor, brightness, 255);
        }
    }
}
