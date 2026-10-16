package com.moratan251.psitweaks.client.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

/**
 * A unit sphere tiled with hexagonal cells (twelve of them pentagons), the dual of a subdivided icosahedron.
 * Unlike a texture on a UV sphere the cells keep their shape everywhere, including at the poles.
 */
final class HoneycombSphereMesh {
    private static final float GOLDEN = (float) ((1.0 + Math.sqrt(5.0)) / 2.0);
    private static final float[][] ICOSAHEDRON_VERTICES = {
            {-1, GOLDEN, 0}, {1, GOLDEN, 0}, {-1, -GOLDEN, 0}, {1, -GOLDEN, 0},
            {0, -1, GOLDEN}, {0, 1, GOLDEN}, {0, -1, -GOLDEN}, {0, 1, -GOLDEN},
            {GOLDEN, 0, -1}, {GOLDEN, 0, 1}, {-GOLDEN, 0, -1}, {-GOLDEN, 0, 1}
    };
    private static final int[][] ICOSAHEDRON_FACES = {
            {0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11},
            {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8},
            {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9},
            {4, 9, 5}, {2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}
    };
    // Declared after the tables above: static fields initialise in order, and the constructor reads them.
    static final HoneycombSphereMesh INSTANCE = new HoneycombSphereMesh(4);

    /** Unit cell centres. */
    final Vector3f[] centers;
    /** Unit cell corners, ordered around each centre. */
    final Vector3f[][] corners;
    /** A fixed random value in [0, 1) per cell, for staggered animations. */
    final float[] seeds;

    private HoneycombSphereMesh(int frequency) {
        List<Vector3f> vertices = new ArrayList<>();
        Map<Long, Integer> lookup = new HashMap<>();
        List<int[]> triangles = new ArrayList<>();

        for (int[] face : ICOSAHEDRON_FACES) {
            Vector3f a = vertex(face[0]);
            Vector3f b = vertex(face[1]);
            Vector3f c = vertex(face[2]);
            int[][] grid = new int[frequency + 1][];
            for (int i = 0; i <= frequency; i++) {
                grid[i] = new int[frequency + 1 - i];
                for (int j = 0; j <= frequency - i; j++) {
                    Vector3f point = new Vector3f(a)
                            .add(new Vector3f(b).sub(a).mul((float) i / frequency))
                            .add(new Vector3f(c).sub(a).mul((float) j / frequency))
                            .normalize();
                    grid[i][j] = indexOf(point, vertices, lookup);
                }
            }
            for (int i = 0; i < frequency; i++) {
                for (int j = 0; j < frequency - i; j++) {
                    triangles.add(new int[]{grid[i][j], grid[i + 1][j], grid[i][j + 1]});
                    if (i + j < frequency - 1) {
                        triangles.add(new int[]{grid[i + 1][j], grid[i + 1][j + 1], grid[i][j + 1]});
                    }
                }
            }
        }

        List<List<Vector3f>> cellCorners = new ArrayList<>(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            cellCorners.add(new ArrayList<>(6));
        }
        for (int[] triangle : triangles) {
            Vector3f centroid = new Vector3f(vertices.get(triangle[0]))
                    .add(vertices.get(triangle[1]))
                    .add(vertices.get(triangle[2]))
                    .normalize();
            for (int index : triangle) {
                cellCorners.get(index).add(centroid);
            }
        }

        centers = vertices.toArray(Vector3f[]::new);
        corners = new Vector3f[centers.length][];
        seeds = new float[centers.length];
        RandomSource random = RandomSource.create(0x5EED_CE11L);
        for (int i = 0; i < centers.length; i++) {
            Vector3f normal = centers[i];
            Vector3f helper = Math.abs(normal.y) < 0.9F ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
            Vector3f tangent = new Vector3f(helper).cross(normal).normalize();
            Vector3f bitangent = new Vector3f(normal).cross(tangent);
            List<Vector3f> around = cellCorners.get(i);
            around.sort(Comparator.comparingDouble(corner -> Math.atan2(corner.dot(bitangent), corner.dot(tangent))));
            corners[i] = around.toArray(Vector3f[]::new);
            seeds[i] = random.nextFloat();
        }
    }

    int cellCount() {
        return centers.length;
    }

    private static Vector3f vertex(int index) {
        float[] v = ICOSAHEDRON_VERTICES[index];
        return new Vector3f(v[0], v[1], v[2]).normalize();
    }

    private static int indexOf(Vector3f point, List<Vector3f> vertices, Map<Long, Integer> lookup) {
        // Each coordinate becomes an integer in [10000, 30000]; base 40000 keeps the three fields apart.
        long key = (Math.round(point.x * 10_000.0) + 20_000L) * 1_600_000_000L
                + (Math.round(point.y * 10_000.0) + 20_000L) * 40_000L
                + (Math.round(point.z * 10_000.0) + 20_000L);
        return lookup.computeIfAbsent(key, k -> {
            vertices.add(point);
            return vertices.size() - 1;
        });
    }
}
