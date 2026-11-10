package com.moratan251.psitweaks.client.renderer.circle;

import static com.moratan251.psitweaks.client.renderer.circle.CirclePen.DIM;
import static com.moratan251.psitweaks.client.renderer.circle.CirclePen.FULL;
import static com.moratan251.psitweaks.client.renderer.circle.CirclePen.MID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 領域などに使う魔法陣の図案（A〜G）。白の濃淡で描き、ゲームでは CAD の色を掛ける。座標は「外周の半径が 64」の図面の単位で、
 * ドット絵の候補（128px、G だけ 256px）と同じ寸法で描き直したもの。
 */
public final class CircleDesigns {
    private static final float SQRT3 = (float) Math.sqrt(3.0);

    /** A 計器環: 目盛りの付いた輪と照準のような十字。 */
    public static final CircleDesign GAUGE = CircleDesign.builder("gauge")
            .layer(0.25F, CircleDesigns::gaugeOuter)
            .layer(-0.25F, CircleDesigns::gaugeMiddle)
            .layer(0.5F, CircleDesigns::gaugeInner)
            .build();

    /** B 六角連鎖: 六角形をつないだ輪とハニカムの格子。 */
    public static final CircleDesign HEX_CHAIN = CircleDesign.builder("hex_chain")
            .layer(0.2F, CircleDesigns::hexChainOuter)
            .layer(-0.15F, CircleDesigns::hexChainLattice)
            .layer(0.4F, CircleDesigns::hexChainInner)
            .build();

    /** C 方陣: 四角い枠と回路の線（固定）、真ん中の印だけが回る。 */
    public static final CircleDesign SQUARE_SEAL = CircleDesign.builder("square_seal")
            .square()
            .layer(0.0F, CircleDesigns::squareSealFrame)
            .layer(0.25F, CircleDesigns::squareSealSeal)
            .layer(0.0F, CircleDesigns::squareSealGrid)
            .build();

    /** D 符号環: 架空の文字の帯が 2 重、真ん中は七芒星。 */
    public static final CircleDesign CODE_RING = CircleDesign.builder("code_ring")
            .layer(0.15F, CircleDesigns::codeRingOuter)
            .layer(-0.22F, CircleDesigns::codeRingMiddle)
            .layer(0.4F, CircleDesigns::codeRingInner)
            .build();

    /** E 軌道環: 傾いた楕円の軌道が 3 本重なったジャイロ。 */
    public static final CircleDesign ORBITAL = CircleDesign.builder("orbital")
            .layer(0.15F, CircleDesigns::orbitalOuter)
            .layer(-0.2F, CircleDesigns::orbitalOrbits)
            .layer(0.45F, CircleDesigns::orbitalInner)
            .build();

    /** F 作図陣: 斜めに交差する光の帯の格子と円の図形（固定）、中心の輪と作図が逆向きに回る。 */
    public static final CircleDesign CONSTRUCTION = CircleDesign.builder("construction")
            .square()
            .layer(0.0F, CircleDesigns::constructionFloor)
            .layer(0.25F, CircleDesigns::constructionGuide)
            .layer(-0.4F, CircleDesigns::constructionCenter)
            .build();

    /** G 多連六角環: 六角形を 2 つつないだタイルの輪が 2 重、真ん中はハニカムの小さな集まり。 */
    public static final CircleDesign POLYHEX_RING = CircleDesign.builder("polyhex_ring")
            .layer(0.12F, CircleDesigns::polyhexOuter)
            .layer(-0.2F, CircleDesigns::polyhexMiddle)
            .layer(0.35F, CircleDesigns::polyhexInner)
            .build();

    public static final List<CircleDesign> ALL = List.of(GAUGE, HEX_CHAIN, SQUARE_SEAL, CODE_RING, ORBITAL,
            CONSTRUCTION, POLYHEX_RING);

    private CircleDesigns() {
    }

    // ================================================================== A 計器環

    private static void gaugeOuter(CirclePen p) {
        p.ring(62.5F, FULL);
        p.ring(55.5F, FULL);
        for (int k = 0; k < 72; k++) {
            float deg = k * 5.0F;
            if (k % 9 == 0) {
                p.radial(55.9F, 62.1F, deg, FULL);
            } else if (k % 3 == 0) {
                p.radial(55.9F, 59.6F, deg, FULL);
            } else {
                p.radial(55.9F, 57.6F, deg, MID);
            }
        }
        for (int k = 0; k < 8; k++) {
            float mid = 22.5F + 45.0F * k;
            p.arc(60.5F, mid - 4.0F, mid + 4.0F, MID);
        }
    }

    private static void gaugeMiddle(CirclePen p) {
        p.dashedRing(51.5F, 8, 0.86F, FULL, 360.0F / 8.0F * 0.07F);
        for (int k = 0; k < 8; k++) {
            float[] at = polar(51.5F, k * 45.0F);
            diamond(p, at[0], at[1], FULL);
        }
        p.dottedRing(47.5F, 60, 0.55F, MID, 0.0F);
        p.arc(44.5F, 10.0F, 110.0F, FULL);
        p.arc(44.5F, 130.0F, 230.0F, FULL);
        p.arc(44.5F, 250.0F, 350.0F, FULL);
        for (float deg : new float[] {0.0F, 120.0F, 240.0F}) {
            p.radial(41.0F, 44.5F, deg, FULL);
        }
    }

    private static void gaugeInner(CirclePen p) {
        p.ring(36.5F, FULL);
        p.ring(33.5F, DIM);
        for (int k = 0; k < 36; k++) {
            if (k % 9 != 0) {
                p.radial(33.9F, 35.4F, k * 10.0F, DIM);
            }
        }
        for (int k = 0; k < 4; k++) {
            p.radial(9.0F, 36.0F, k * 90.0F, FULL);
            p.radial(26.0F, 33.0F, 45.0F + k * 90.0F, MID);
            p.arc(24.5F, k * 90.0F + 8.0F, k * 90.0F + 82.0F, FULL);
        }
        p.polygon(17.0F, 4, 45.0F, FULL);
        p.polygon(12.0F, 4, 0.0F, MID);
        p.ring(5.5F, FULL);
        p.dot(0.0F, 0.0F, 1.0F, FULL);
    }

    // ================================================================== B 六角連鎖

    private static void hexChainOuter(CirclePen p) {
        int count = 16;
        float rc = 55.5F;
        for (int k = 0; k < count; k++) {
            float deg = 360.0F * k / count;
            float[] c = polar(rc, deg);
            // 鎖の六角形はすべて白（2026-11-10 ユーザーの指定）。
            p.white().polygonAt(c[0], c[1], 6.5F, 6, deg, 1, FULL);
            p.color();
            if (k % 2 == 0) {
                p.dot(c[0], c[1], 1.2F, FULL);
            } else {
                p.polygonAt(c[0], c[1], 3.0F, 6, deg + 30.0F, 1, MID);
            }
        }
        for (int k = 0; k < count; k++) {
            float mid = 360.0F * (k + 0.5F) / count;
            p.arc(rc, mid - 2.6F, mid + 2.6F, FULL);
            // いちばん外の破線の輪は白（Psi の魔法陣の外の輪に合わせる）。
            p.white().arc(62.5F, mid - 6.0F, mid + 6.0F, MID);
            p.color().arc(48.5F, mid - 6.0F, mid + 6.0F, MID);
        }
    }

    private static void hexChainLattice(CirclePen p) {
        float s = 6.0F;
        float w = s * 1.5F;
        float h = s * SQRT3;
        EdgeSet edges = new EdgeSet();
        for (int col = -12; col <= 12; col++) {
            for (int row = -12; row <= 12; row++) {
                float cx = col * w;
                float cy = row * h + (Math.floorMod(col, 2) == 1 ? h / 2.0F : 0.0F);
                if (Math.hypot(cx, cy) < 44.0) {
                    edges.hexagon(cx, cy, s, 0.0F, DIM);
                }
            }
        }
        p.keepDisc(43.0F).caps(false);
        edges.draw(p);
        p.reset();
        p.ring(45.5F, FULL);
        p.ring(43.5F, MID);
    }

    private static void hexChainInner(CirclePen p) {
        // 大きな六角形と、その頂点から外へ伸びる 6 本の線と先の点は白（2026-11-10 ユーザーの指定）。
        p.white().polygonAt(0.0F, 0.0F, 30.0F, 6, 30.0F, 1, FULL);
        p.color().polygonAt(0.0F, 0.0F, 27.0F, 6, 30.0F, 1, MID);
        p.polygonAt(0.0F, 0.0F, 18.0F, 6, 0.0F, 1, FULL);
        for (int k = 0; k < 6; k++) {
            float deg = 30.0F + 60.0F * k;
            p.white().radial(30.0F, 39.2F, deg, FULL);
            float[] at = polar(40.5F, deg);
            p.dot(at[0], at[1], 1.4F, FULL);
            p.color().radial(8.0F, 18.0F, 60.0F * k, MID);
        }
        p.polygonAt(0.0F, 0.0F, 7.0F, 6, 30.0F, 1, FULL);
        p.dot(0.0F, 0.0F, 1.0F, FULL);
    }

    // ================================================================== C 方陣

    private static void squareSealFrame(CirclePen p) {
        square(p, 63.3F, FULL);
        square(p, 58.5F, MID);
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                squareSealCorner(p, sx, sy);
            }
        }
        // 上下の辺の真ん中の、内向きの三角の印。
        for (int sy = -1; sy <= 1; sy += 2) {
            p.fillConvex(FULL, -5.5F, sy * 57.3F, 5.5F, sy * 57.3F, 0.0F, sy * 52.0F);
        }
    }

    /** 方陣の角 1 つぶん（{@code sx}, {@code sy} は向き）。 */
    private static void squareSealCorner(CirclePen p, float sx, float sy) {
        for (int t = 13; t <= 61; t += 8) {
            float u = t - 63.5F;
            p.line(sx * u, sy * 62.3F, sx * u, sy * 60.5F, MID);
            p.line(sx * 62.3F, sy * u, sx * 60.5F, sy * u, MID);
        }
        // 角の鉤と小さな四角。
        p.fillConvex(FULL, sx * 63.3F, sy * 63.3F, sx * 54.5F, sy * 63.3F, sx * 54.5F, sy * 61.0F, sx * 63.3F, sy * 61.0F);
        p.fillConvex(FULL, sx * 63.3F, sy * 61.0F, sx * 61.0F, sy * 61.0F, sx * 61.0F, sy * 54.5F, sx * 63.3F, sy * 54.5F);
        p.polyline(true, FULL, sx * 55.5F, sy * 55.5F, sx * 50.5F, sy * 55.5F, sx * 50.5F, sy * 50.5F, sx * 55.5F, sy * 50.5F);
        p.dot(sx * 53.0F, sy * 53.0F, 1.0F, FULL);
        // 回路の線。
        p.polyline(false, FULL, sx * 39.5F, sy * 57.5F, sx * 39.5F, sy * 47.5F, sx * 29.5F, sy * 37.5F);
        pad(p, sx * 29.5F, sy * 37.5F, FULL);
        p.polyline(false, MID, sx * 23.5F, sy * 57.5F, sx * 23.5F, sy * 51.5F, sx * 17.5F, sy * 45.5F);
        p.dot(sx * 17.5F, sy * 45.5F, 1.0F, MID);
        p.polyline(false, FULL, sx * 57.5F, sy * 39.5F, sx * 47.5F, sy * 39.5F, sx * 37.5F, sy * 29.5F);
        pad(p, sx * 37.5F, sy * 29.5F, FULL);
        p.polyline(false, MID, sx * 57.5F, sy * 23.5F, sx * 51.5F, sy * 23.5F, sx * 45.5F, sy * 17.5F);
        p.dot(sx * 45.5F, sy * 17.5F, 1.0F, MID);
    }

    private static void squareSealSeal(CirclePen p) {
        p.ring(44.5F, FULL);
        p.ring(41.5F, MID);
        p.dottedRing(47.5F, 32, 0.55F, MID, 0.0F);
        p.polygon(41.0F, 4, 45.0F, FULL);
        p.polygon(41.0F, 4, 0.0F, FULL);
        p.polygon(29.0F, 8, 22.5F, MID);
        p.ring(18.5F, FULL);
        for (int k = 0; k < 4; k++) {
            p.arc(10.5F, k * 90.0F + 15.0F, k * 90.0F + 75.0F, FULL);
        }
        for (int k = 0; k < 8; k++) {
            p.radial(10.5F, 18.5F, k * 45.0F + 22.5F, MID);
        }
        p.dot(0.0F, 0.0F, 1.0F, FULL);
    }

    private static void squareSealGrid(CirclePen p) {
        p.hole(0.0F, 0.0F, 50.0F).weight(0.8F);
        for (float t : new float[] {15.5F, 31.5F, 47.5F}) {
            for (int sign = -1; sign <= 1; sign += 2) {
                dashedLine(p, sign * t, -55.5F, sign * t, 55.5F, 2.0F, 2.0F, DIM);
                dashedLine(p, -55.5F, sign * t, 55.5F, sign * t, 2.0F, 2.0F, DIM);
            }
        }
        p.reset();
    }

    // ================================================================== D 符号環

    private static void codeRingOuter(CirclePen p) {
        p.ring(62.5F, FULL);
        p.ring(54.5F, FULL);
        glyphBand(p, 56.0F, 61.0F, 60, 4, 5, 7L, FULL, 2);
    }

    private static void codeRingMiddle(CirclePen p) {
        p.ring(49.5F, MID);
        p.ring(40.5F, FULL);
        glyphBand(p, 42.0F, 47.0F, 44, 4, 5, 11L, MID, 2);
        for (int k = 0; k < 6; k++) {
            p.radial(40.8F, 49.2F, k * 60.0F + 3.0F, FULL);
        }
    }

    private static void codeRingInner(CirclePen p) {
        p.ring(35.5F, MID);
        p.polygonAt(0.0F, 0.0F, 35.0F, 7, -90.0F, 3, FULL);
        p.polygonAt(0.0F, 0.0F, 35.0F, 7, -90.0F, 1, MID);
        p.ring(13.5F, FULL);
        p.dottedRing(9.5F, 7, 1.0F, FULL, -90.0F + 360.0F / 14.0F);
        p.dot(0.0F, 0.0F, 1.0F, FULL);
    }

    /**
     * 半径 {@code r0}〜{@code r1} の帯に、{@code cells} 文字ぶんの架空の文字を並べる。文字は {@code gw}×{@code gh} の升目の
     * 線でできていて、横の線は円弧、縦の線は中心へ向かう線になる。
     */
    private static void glyphBand(CirclePen p, float r0, float r1, int cells, int gw, int gh, long seed, float b, int gap) {
        Random rnd = new Random(seed);
        int cellW = gw + gap;
        float stripW = cells * cellW;
        float rowH = (r1 - r0) / gh;
        p.weight(0.8F);
        for (int i = 0; i < cells; i++) {
            float base = i * cellW;
            if (rnd.nextFloat() < 0.12F) {
                // 区切り: 真ん中に点だけ。
                float[] at = polar(r1 - (gh / 2 + 0.5F) * rowH, (base + gw / 2 + 0.5F) / stripW * 360.0F);
                p.dot(at[0], at[1], 0.55F, b);
                continue;
            }
            boolean[][] g = glyph(rnd, gw, gh);
            boolean[][] used = new boolean[gh][gw];
            for (int y = 0; y < gh; y++) {
                int x = 0;
                while (x < gw) {
                    if (!g[y][x]) {
                        x++;
                        continue;
                    }
                    int end = x;
                    while (end + 1 < gw && g[y][end + 1]) {
                        end++;
                    }
                    if (end > x) {
                        float radius = r1 - (y + 0.5F) * rowH;
                        p.arc(radius, (base + x + 0.5F) / stripW * 360.0F, (base + end + 0.5F) / stripW * 360.0F, b);
                        for (int k = x; k <= end; k++) {
                            used[y][k] = true;
                        }
                    }
                    x = end + 1;
                }
            }
            for (int x = 0; x < gw; x++) {
                int y = 0;
                while (y < gh) {
                    if (!g[y][x]) {
                        y++;
                        continue;
                    }
                    int end = y;
                    while (end + 1 < gh && g[end + 1][x]) {
                        end++;
                    }
                    if (end > y) {
                        p.radial(r1 - (end + 0.5F) * rowH, r1 - (y + 0.5F) * rowH, (base + x + 0.5F) / stripW * 360.0F, b);
                        for (int k = y; k <= end; k++) {
                            used[k][x] = true;
                        }
                    }
                    y = end + 1;
                }
            }
            for (int y = 0; y < gh; y++) {
                for (int x = 0; x < gw; x++) {
                    if (g[y][x] && !used[y][x]) {
                        float[] at = polar(r1 - (y + 0.5F) * rowH, (base + x + 0.5F) / stripW * 360.0F);
                        p.dot(at[0], at[1], 0.5F, b);
                    }
                }
            }
        }
        p.reset();
    }

    /** 架空の文字 1 つ: 縦の背骨に、横の線を 1〜3 本と、反対側の短い鉤。 */
    private static boolean[][] glyph(Random rnd, int w, int h) {
        boolean[][] g = new boolean[h][w];
        int[] spines = {0, w - 1, w / 2};
        int spine = spines[rnd.nextInt(spines.length)];
        int top = rnd.nextInt(2);
        for (int y = top; y < h; y++) {
            g[y][spine] = true;
        }
        List<Integer> rows = new ArrayList<>();
        for (int y = 0; y < h; y += 2) {
            rows.add(y);
        }
        Collections.shuffle(rows, rnd);
        int[] counts = {1, 2, 2, 3};
        int count = Math.min(rows.size(), counts[rnd.nextInt(counts.length)]);
        for (int r = 0; r < count; r++) {
            int y = rows.get(r);
            int x0;
            int x1;
            if (rnd.nextFloat() < 0.5F) {
                x0 = 0;
                x1 = w - 1;
            } else if (spine == 0) {
                x0 = 0;
                x1 = 1 + rnd.nextInt(w - 1);
            } else if (spine == w - 1) {
                x0 = rnd.nextInt(w - 1);
                x1 = w - 1;
            } else if (rnd.nextFloat() < 0.5F) {
                x0 = 0;
                x1 = spine;
            } else {
                x0 = spine;
                x1 = w - 1;
            }
            for (int x = x0; x <= x1; x++) {
                g[y][x] = true;
            }
        }
        if (rnd.nextFloat() < 0.45F) {
            int side = spine == 0 ? w - 1 : 0;
            int y0 = rnd.nextInt(h - 1);
            g[y0][side] = true;
            g[y0 + 1][side] = true;
        }
        return g;
    }

    // ================================================================== E 軌道環

    private static void orbitalOuter(CirclePen p) {
        for (int k = 0; k < 4; k++) {
            float d = k * 90.0F;
            p.arc(62.5F, d + 4.0F, d + 86.0F, FULL);
            p.radial(57.0F, 62.4F, d, FULL);
            p.radial(58.5F, 62.4F, d - 4.0F, FULL);
            p.radial(58.5F, 62.4F, d + 4.0F, FULL);
        }
        p.ring(60.5F, MID);
        p.dottedRing(57.5F, 96, 0.45F, DIM, 0.0F);
    }

    private static void orbitalOrbits(CirclePen p) {
        float[][] nodes = new float[3][];
        for (int k = 0; k < 3; k++) {
            float t = (float) Math.toRadians(40.0F + 120.0F * k);
            float rot = (float) Math.toRadians(60.0F * k);
            float x = 52.0F * (float) Math.cos(t);
            float y = 16.0F * (float) Math.sin(t);
            nodes[k] = new float[] {x * (float) Math.cos(rot) - y * (float) Math.sin(rot),
                    x * (float) Math.sin(rot) + y * (float) Math.cos(rot)};
            p.hole(nodes[k][0], nodes[k][1], 4.2F);
        }
        for (int k = 0; k < 3; k++) {
            p.ellipse(0.0F, 0.0F, 52.0F, 16.0F, 60.0F * k, FULL);
        }
        p.ring(52.5F, DIM);
        p.reset();
        for (float[] node : nodes) {
            p.ringAt(node[0], node[1], 3.0F, FULL);
            p.dot(node[0], node[1], 0.6F, FULL);
        }
    }

    private static void orbitalInner(CirclePen p) {
        for (int k = 0; k < 6; k++) {
            p.arc(30.5F, k * 60.0F + 6.0F, k * 60.0F + 54.0F, FULL);
            p.radial(12.9F, 26.1F, 60.0F * k + 30.0F, DIM);
        }
        p.ring(26.5F, MID);
        p.dashedRing(22.5F, 24, 0.5F, MID, 0.0F);
        p.ring(12.5F, FULL);
        p.ellipse(0.0F, 0.0F, 12.0F, 4.5F, 0.0F, MID);
        p.ellipse(0.0F, 0.0F, 12.0F, 4.5F, 90.0F, MID);
        p.dot(0.0F, 0.0F, 1.0F, FULL);
    }

    // ================================================================== F 作図陣

    private static final float F_SPACING = 22.0F;

    private static void constructionFloor(CirclePen p) {
        float s = F_SPACING;
        float[][] edgeNodes = {{2 * s, 0}, {-2 * s, 0}, {0, 2 * s}, {0, -2 * s}};
        float[][] cornerNodes = {{2 * s, 2 * s}, {-2 * s, 2 * s}, {2 * s, -2 * s}, {-2 * s, -2 * s}};

        // 光の帯と作図の円弧: 円の図形と中心のまわりは空ける。
        p.keepSquare(61.0F);
        for (float[] n : edgeNodes) {
            p.hole(n[0], n[1], 12.5F);
        }
        for (float[] n : cornerNodes) {
            p.hole(n[0], n[1], 12.5F);
        }
        p.hole(0.0F, 0.0F, 27.0F);
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                p.hole(sx * s, sy * s, 3.2F);
            }
        }
        float reach = 130.0F;
        float diag = (float) Math.sqrt(0.5);
        for (int k = -2; k <= 2; k++) {
            float d = 2 * s * k;
            // u - v = d（右下がり）と u + v = d（右上がり）の 2 向き。
            for (int dir = 0; dir < 2; dir++) {
                float ox = d / 2.0F;
                float oy = dir == 0 ? -d / 2.0F : d / 2.0F;
                float tx = diag;
                float ty = dir == 0 ? diag : -diag;
                float nx = -ty;
                float ny = tx;
                p.band(ox - tx * reach, oy - ty * reach, ox + tx * reach, oy + ty * reach, 2.2F, DIM);
                for (int side = -1; side <= 1; side += 2) {
                    float e = side * 2.6F;
                    p.line(ox - tx * reach + nx * e, oy - ty * reach + ny * e,
                            ox + tx * reach + nx * e, oy + ty * reach + ny * e, MID);
                }
            }
        }
        for (float[] n : edgeNodes) {
            p.ringAt(n[0], n[1], 2 * s, DIM);
        }
        // 作図の軸の短い破線。
        for (int k = 0; k < 4; k++) {
            float deg = k * 90.0F;
            radialBand(p, 27.0F, 29.5F, deg, 1.3F, MID);
            radialBand(p, 56.5F, 59.5F, deg, 1.3F, MID);
        }
        p.reset();

        // 辺の真ん中の円: 2 重の輪、中心を向いた 4 分の 1 と反対側の 4 分の 1 を塗り、中心の向きと直角に線。
        for (float[] n : edgeNodes) {
            float toward = (float) Math.toDegrees(Math.atan2(-n[1], -n[0]));
            for (float off : new float[] {0.0F, 180.0F}) {
                p.sector(n[0], n[1], 6.5F, 10.0F, toward + off - 45.0F, toward + off + 45.0F, DIM);
            }
            // 辺の真ん中の円の外の輪は白（Psi の魔法陣の白い小円に合わせる）。
            p.white().ringAt(n[0], n[1], 10.5F, FULL);
            p.color().ringAt(n[0], n[1], 6.0F, MID);
            float a = (float) Math.toRadians(toward + 90.0F);
            float cx = (float) Math.cos(a) * 10.0F;
            float cy = (float) Math.sin(a) * 10.0F;
            p.line(n[0] - cx, n[1] - cy, n[0] + cx, n[1] + cy, MID);
            p.dot(n[0], n[1], 1.0F, FULL);
        }
        // 角の円: 輪と六角形、外側の半分を塗る。
        for (float[] n : cornerNodes) {
            float away = (float) Math.toDegrees(Math.atan2(n[1], n[0]));
            p.sector(n[0], n[1], 0.0F, 10.0F, away - 90.0F, away + 90.0F, DIM);
            p.ringAt(n[0], n[1], 10.5F, FULL);
            p.polygonAt(n[0], n[1], 7.5F, 6, away, 1, FULL);
            p.ringAt(n[0], n[1], 2.5F, FULL);
        }
        // 中心と角のあいだで帯が交わるところの小さな輪。
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                p.ringAt(sx * s, sy * s, 2.5F, FULL);
            }
        }
    }

    private static void constructionGuide(CirclePen p) {
        // 中心を囲む作図の輪（弧・目盛り・菱形）は白（Psi の魔法陣の白い外の輪に合わせる）。点の輪は CAD の色。
        p.white();
        for (int k = 0; k < 4; k++) {
            float d = k * 90.0F;
            p.arc(30.5F, d + 5.0F, d + 85.0F, FULL);
            p.radial(30.5F, 31.3F, d, FULL);
            float[] at = polar(33.5F, d);
            diamond(p, at[0], at[1], FULL);
        }
        p.color();
        p.dottedRing(28.0F, 40, 0.5F, MID, 0.0F);
    }

    private static void constructionCenter(CirclePen p) {
        p.ring(24.5F, FULL);
        float r = 12.0F;
        float cut = 4.6F;
        for (int k = 0; k < 4; k++) {
            float a = (float) Math.toRadians(k * 90.0F);
            float b = (float) Math.toRadians(k * 90.0F + 90.0F);
            float[][] lens = CirclePen.polygonPoints((float) Math.cos(a) * r, (float) Math.sin(a) * r, r, 96, 0.0F);
            lens = clipByDisc(lens, (float) Math.cos(b) * r, (float) Math.sin(b) * r, r);
            // 真ん中は空けるので、先端を中心から cut のところで切る。
            float mid = (float) Math.toRadians(k * 90.0F + 45.0F);
            lens = clipHalfPlane(lens, (float) Math.cos(mid), (float) Math.sin(mid), cut);
            if (lens.length >= 3) {
                float cx = 0.0F;
                float cy = 0.0F;
                for (float[] v : lens) {
                    cx += v[0];
                    cy += v[1];
                }
                p.fillFan(cx / lens.length, cy / lens.length, lens, DIM);
            }
        }
        p.hole(0.0F, 0.0F, cut);
        for (int k = 0; k < 4; k++) {
            float[] c = polar(r, k * 90.0F);
            p.ringAt(c[0], c[1], r, MID);
        }
        p.reset();
        p.polygon(24.0F, 4, 45.0F, FULL);
        p.ring(4.0F, FULL);
        p.dot(0.0F, 0.0F, 1.0F, FULL);
    }

    // ================================================================== G 多連六角環

    private static void polyhexOuter(CirclePen p) {
        p.weight(0.75F);
        tileRing(p, 53.5F, 18, 4.75F, 50.0F, 0.0F);
        for (int k = 0; k < 18; k++) {
            p.arc(63.5F, 20.0F * k + 4.0F, 20.0F * (k + 1) - 4.0F, DIM);
        }
        p.reset();
    }

    private static void polyhexMiddle(CirclePen p) {
        p.weight(0.75F);
        tileRing(p, 34.0F, 15, 3.5F, -50.0F, 12.0F);
        for (int k = 0; k < 15; k++) {
            p.arc(43.25F, 24.0F * k + 6.0F, 24.0F * (k + 1) - 6.0F, MID);
        }
        p.ring(25.75F, DIM);
        p.reset();
    }

    private static void polyhexInner(CirclePen p) {
        p.weight(0.75F);
        p.ring(22.75F, MID);
        float s = 4.0F;
        EdgeSet edges = new EdgeSet();
        for (int k = -1; k < 6; k++) {
            float cx = 0.0F;
            float cy = 0.0F;
            if (k >= 0) {
                float[] c = polar(s * SQRT3, 30.0F + 60.0F * k);
                cx = c[0];
                cy = c[1];
            }
            if (k < 0 || k % 2 == 0) {
                p.fillPolygon(cx, cy, s, 6, 0.0F, DIM);
            }
            edges.hexagon(cx, cy, s, 0.0F, k < 0 ? FULL : MID);
        }
        p.caps(false);
        edges.draw(p);
        p.caps(true);
        for (int k = 0; k < 6; k++) {
            float[] c = polar(3.0F * s, 60.0F * k);
            p.polygonAt(c[0], c[1], s * 0.6F, 6, 0.0F, 1, FULL);
        }
        p.reset();
    }

    /**
     * 六角形を 2 つつないだタイルを、半径 {@code radius} の円に {@code count} 枚並べる。タイルの向きは円の接線から
     * {@code tilt} 度傾ける。薄い塗り、つなぎ目の線、輪郭の線。
     */
    private static void tileRing(CirclePen p, float radius, int count, float s, float tilt, float phase) {
        float step = s * SQRT3;
        for (int k = 0; k < count; k++) {
            float ang = phase + 360.0F * k / count;
            float[] c = polar(radius, ang);
            float phi = ang + 90.0F + tilt;
            float[] dir = polar(step / 2.0F, phi);
            float[][] a = CirclePen.polygonPoints(c[0] - dir[0], c[1] - dir[1], s, 6, phi + 30.0F);
            float[][] b = CirclePen.polygonPoints(c[0] + dir[0], c[1] + dir[1], s, 6, phi + 30.0F);
            // 1 つめの 6 頂点（phi+30 から）と、2 つめの phi+270, 330, 30, 90 の頂点で 1 周。
            float[][] outline = {a[0], a[1], a[2], a[3], a[4], a[5], b[4], b[5], b[0], b[1]};
            p.fillFan(c[0], c[1], outline, DIM);
            p.line(a[0][0], a[0][1], a[5][0], a[5][1], MID);
            float[] xy = new float[outline.length * 2];
            for (int i = 0; i < outline.length; i++) {
                xy[2 * i] = outline[i][0];
                xy[2 * i + 1] = outline[i][1];
            }
            p.polyline(true, FULL, xy);
        }
    }

    // ================================================================== 道具

    private static float[] polar(float r, float deg) {
        float a = (float) Math.toRadians(deg);
        return new float[] {(float) Math.cos(a) * r, (float) Math.sin(a) * r};
    }

    /** 小さなひし形の輪郭（ドット絵の「中空のひし形」）。 */
    private static void diamond(CirclePen p, float x, float y, float b) {
        p.polygonAt(x, y, 2.0F, 4, 0.0F, 1, b);
    }

    private static void square(CirclePen p, float half, float b) {
        p.polyline(true, b, -half, -half, half, -half, half, half, -half, half);
    }

    /** 回路の線の端の小さな四角。 */
    private static void pad(CirclePen p, float x, float y, float b) {
        p.fillConvex(b, x - 1.3F, y - 1.3F, x + 1.3F, y - 1.3F, x + 1.3F, y + 1.3F, x - 1.3F, y + 1.3F);
    }

    private static void radialBand(CirclePen p, float r0, float r1, float deg, float width, float b) {
        float[] a = polar(r0, deg);
        float[] c = polar(r1, deg);
        p.band(a[0], a[1], c[0], c[1], width, b);
    }

    private static void dashedLine(CirclePen p, float x0, float y0, float x1, float y1, float dash, float gap, float b) {
        float len = (float) Math.hypot(x1 - x0, y1 - y0);
        float dx = (x1 - x0) / len;
        float dy = (y1 - y0) / len;
        for (float t = 0.0F; t < len; t += dash + gap) {
            float e = Math.min(len, t + dash);
            p.line(x0 + dx * t, y0 + dy * t, x0 + dx * e, y0 + dy * e, b);
        }
    }

    /** 凸多角形のうち、円 ({@code cx}, {@code cy}, {@code r}) の中の部分（円は 96 角形で近似）。 */
    private static float[][] clipByDisc(float[][] poly, float cx, float cy, float r) {
        float[][] disc = CirclePen.polygonPoints(cx, cy, r, 96, 0.0F);
        float[][] out = poly;
        for (int i = 0; i < disc.length && out.length > 0; i++) {
            float[] a = disc[i];
            float[] b = disc[(i + 1) % disc.length];
            // 円の中心がある側を残す。
            float nx = -(b[1] - a[1]);
            float ny = b[0] - a[0];
            if (nx * (cx - a[0]) + ny * (cy - a[1]) < 0.0F) {
                nx = -nx;
                ny = -ny;
            }
            out = clipHalfPlane(out, nx, ny, nx * a[0] + ny * a[1]);
        }
        return out;
    }

    /** 凸多角形のうち、nx * x + ny * y >= c の側（Sutherland–Hodgman）。 */
    private static float[][] clipHalfPlane(float[][] poly, float nx, float ny, float c) {
        List<float[]> out = new ArrayList<>();
        int n = poly.length;
        for (int i = 0; i < n; i++) {
            float[] a = poly[i];
            float[] b = poly[(i + 1) % n];
            float da = nx * a[0] + ny * a[1] - c;
            float db = nx * b[0] + ny * b[1] - c;
            if (da >= 0.0F) {
                out.add(a);
            }
            if (da >= 0.0F != db >= 0.0F) {
                float t = da / (da - db);
                out.add(new float[] {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t});
            }
        }
        return out.toArray(new float[0][]);
    }

    /** 六角形の格子の辺を、重ねて描かないように 1 本ずつ集める（同じ辺は明るい方を残す）。 */
    private static final class EdgeSet {
        private final Map<Long, float[]> edges = new HashMap<>();

        void hexagon(float cx, float cy, float s, float rotDeg, float b) {
            float[][] v = CirclePen.polygonPoints(cx, cy, s, 6, rotDeg);
            for (int i = 0; i < 6; i++) {
                float[] a = v[i];
                float[] c = v[(i + 1) % 6];
                long key = (Math.round((a[0] + c[0]) * 20.0F) & 0xFFFFFFFFL) << 32 | (Math.round((a[1] + c[1]) * 20.0F) & 0xFFFFFFFFL);
                float[] old = edges.get(key);
                if (old == null || old[4] < b) {
                    edges.put(key, new float[] {a[0], a[1], c[0], c[1], b});
                }
            }
        }

        void draw(CirclePen p) {
            for (float[] e : edges.values()) {
                p.line(e[0], e[1], e[2], e[3], e[4]);
            }
        }
    }
}
