package com.moratan251.psitweaks.client.renderer.circle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 魔法陣の図形を {@link CircleMesh} の四角形に組み立てる道具。図面の単位は「外周の半径が 64」、角度は度で 0 が +x、90 が +y
 * （画面の下向き）。
 * <p>
 * 線は「芯」（同じ明るさの帯）の両側に「ぼかし」（明るさが 0 まで下がる帯）を付けて描くので、拡大してもガタガタせず、光の線の
 * ように見える。加算合成で描くため、重なったところは明るくなる。折れ線の角は継ぎ目が重ならないように留め継ぎにし、鋭い角だけは
 * 切り離して端を丸める。
 * <p>
 * {@link #hole} と {@link #keepDisc} / {@link #keepSquare} で、線を描く範囲を切り抜ける（塗りには効かない）。
 */
public final class CirclePen {
    public static final float FULL = 1.0F;
    public static final float MID = 0.6F;
    public static final float DIM = 0.35F;

    /** 普通の線の芯の幅（図面の単位）。ドット絵の 1px の線に当たる。 */
    public static final float LINE = 0.55F;
    /** 線のふちのぼかしの幅。 */
    private static final float FEATHER = 0.5F;
    /** 円を折れ線で近似するときの許す誤差。 */
    private static final float CHORD_ERROR = 0.04F;
    /** 留め継ぎの伸びがこれを超える鋭い角では、線を切り離す。 */
    private static final float SHARP_MITER = 2.0F;
    /** 塗りのふちの留め継ぎの伸びの上限。 */
    private static final float FILL_MITER_LIMIT = 3.0F;
    /** 切り抜きの境目を探すときの刻み。 */
    private static final float CLIP_STEP = 0.4F;
    private static final float EPSILON = 1.0E-4F;

    private final CircleMesh colorMesh;
    private final CircleMesh whiteMesh;
    /** これから描く図形を入れる形（{@link #white()} / {@link #color()} で切り替える）。 */
    private CircleMesh mesh;
    private final float lineScale;
    private float width = LINE;
    private boolean caps = true;
    private final List<float[]> holes = new ArrayList<>();
    private float keepRadius = -1.0F;
    private float keepHalf = -1.0F;

    /** {@code lineScale} は線の太さとぼかしに掛ける倍率（小さく出す魔法陣では太くする）。白の図形も同じ形に入れる。 */
    public CirclePen(CircleMesh mesh, float lineScale) {
        this(mesh, mesh, lineScale);
    }

    /**
     * CAD の色で描く図形を {@code colorMesh} に、{@link #white()} のあとに描く図形を {@code whiteMesh} に入れる
     * （Psi の魔法陣のように、一部を白で描くため）。
     */
    public CirclePen(CircleMesh colorMesh, CircleMesh whiteMesh, float lineScale) {
        this.colorMesh = colorMesh;
        this.whiteMesh = whiteMesh;
        this.mesh = colorMesh;
        this.lineScale = lineScale;
    }

    // ------------------------------------------------------------------ 状態

    /** これから描く図形を白で描く（{@link #color()} で CAD の色に戻す。{@link #reset} では戻らない）。 */
    public CirclePen white() {
        this.mesh = whiteMesh;
        return this;
    }

    /** これから描く図形を CAD の色で描く（はじめはこちら）。 */
    public CirclePen color() {
        this.mesh = colorMesh;
        return this;
    }

    /** これから描く線の芯の幅を、普通の線の何倍にするか。 */
    public CirclePen weight(float multiple) {
        this.width = LINE * multiple;
        return this;
    }

    /** 開いた線の端をぼかすか（格子のように線の端どうしがつながるところでは切る）。 */
    public CirclePen caps(boolean caps) {
        this.caps = caps;
        return this;
    }

    /** これから描く線を、円 ({@code cx}, {@code cy}, {@code r}) の中だけ描かない。 */
    public CirclePen hole(float cx, float cy, float r) {
        holes.add(new float[] {cx, cy, r});
        return this;
    }

    /** これから描く線を、中心からの距離が {@code r} 未満のところだけに描く。 */
    public CirclePen keepDisc(float r) {
        this.keepRadius = r;
        return this;
    }

    /** これから描く線を、|x| と |y| が {@code half} 未満のところだけに描く。 */
    public CirclePen keepSquare(float half) {
        this.keepHalf = half;
        return this;
    }

    /** 切り抜きをすべて外し、線の太さと端も元に戻す。 */
    public CirclePen reset() {
        holes.clear();
        keepRadius = -1.0F;
        keepHalf = -1.0F;
        width = LINE;
        caps = true;
        return this;
    }

    // ------------------------------------------------------------------ 線

    public void line(float x0, float y0, float x1, float y1, float b) {
        Path path = new Path();
        path.add(x0, y0);
        path.add(x1, y1);
        stroke(path, false, b);
    }

    /** 中心から角度 {@code deg} の向きに、半径 {@code r0} から {@code r1} までの線。 */
    public void radial(float r0, float r1, float deg, float b) {
        float a = rad(deg);
        float c = (float) Math.cos(a);
        float s = (float) Math.sin(a);
        line(r0 * c, r0 * s, r1 * c, r1 * s, b);
    }

    public void ring(float r, float b) {
        ringAt(0.0F, 0.0F, r, b);
    }

    public void ringAt(float cx, float cy, float r, float b) {
        stroke(arcPath(cx, cy, r, 0.0F, 360.0F, false), true, b);
    }

    /** 半径 {@code r} の円のうち、角度 {@code from} から {@code to} まで（{@code to} > {@code from}）。 */
    public void arc(float r, float from, float to, float b) {
        arcAt(0.0F, 0.0F, r, from, to, b);
    }

    public void arcAt(float cx, float cy, float r, float from, float to, float b) {
        if (to - from >= 360.0F - EPSILON) {
            ringAt(cx, cy, r, b);
            return;
        }
        stroke(arcPath(cx, cy, r, from, to, true), false, b);
    }

    /** {@code count} 本の破線の円。{@code duty} は 1 区切りのうち線のある割合。 */
    public void dashedRing(float r, int count, float duty, float b, float phase) {
        float step = 360.0F / count;
        for (int k = 0; k < count; k++) {
            float from = phase + k * step;
            arc(r, from, from + step * duty, b);
        }
    }

    /** 半径 {@code r} の円の上に、{@code count} 個の小さな点。 */
    public void dottedRing(float r, int count, float dotRadius, float b, float phase) {
        for (int k = 0; k < count; k++) {
            float a = rad(phase + 360.0F * k / count);
            dot((float) Math.cos(a) * r, (float) Math.sin(a) * r, dotRadius, b);
        }
    }

    /** 正 {@code sides} 角形の輪郭。{@code step} が 2 以上なら星形（{@code step} 個先の頂点へ結ぶ）。 */
    public void polygon(float r, int sides, float rotDeg, float b) {
        polygonAt(0.0F, 0.0F, r, sides, rotDeg, 1, b);
    }

    public void polygonAt(float cx, float cy, float r, int sides, float rotDeg, int step, float b) {
        float[][] p = polygonPoints(cx, cy, r, sides, rotDeg);
        int s = Math.max(1, step);
        int loops = gcd(sides, s);
        // {6/2} のように割り切れる星形は、頂点をずらした多角形が何周か重なる。
        for (int start = 0; start < loops; start++) {
            Path path = new Path();
            for (int k = 0, i = start; k < sides / loops; k++, i = (i + s) % sides) {
                path.add(p[i][0], p[i][1]);
            }
            stroke(path, true, b);
        }
    }

    /** 中心 ({@code cx}, {@code cy})、半径 {@code a} と {@code bAxis}、{@code rotDeg} 回した楕円。 */
    public void ellipse(float cx, float cy, float a, float bAxis, float rotDeg, float b) {
        int n = segments(Math.max(a, bAxis), 360.0F);
        float rot = rad(rotDeg);
        float cr = (float) Math.cos(rot);
        float sr = (float) Math.sin(rot);
        Path path = new Path();
        for (int i = 0; i < n; i++) {
            float t = (float) (2.0 * Math.PI * i / n);
            float x = a * (float) Math.cos(t);
            float y = bAxis * (float) Math.sin(t);
            path.add(cx + x * cr - y * sr, cy + x * sr + y * cr);
        }
        stroke(path, true, b);
    }

    /** 点を結んだ折れ線。{@code xy} は x, y を交互に並べたもの。 */
    public void polyline(boolean closed, float b, float... xy) {
        Path path = new Path();
        for (int i = 0; i + 1 < xy.length; i += 2) {
            path.add(xy[i], xy[i + 1]);
        }
        stroke(path, closed, b);
    }

    /** まっすぐな帯（幅 {@code bandWidth} の芯、ふちだけぼかす）。光の帯や太い線に使う。 */
    public void band(float x0, float y0, float x1, float y1, float bandWidth, float b) {
        float saved = width;
        width = bandWidth / lineScale;
        line(x0, y0, x1, y1, b);
        width = saved;
    }

    // ------------------------------------------------------------------ 塗り

    /** 塗りつぶした小さな円。 */
    public void dot(float cx, float cy, float r, float b) {
        float[][] p = polygonPoints(cx, cy, r, Math.max(8, segments(r, 360.0F)), 0.0F);
        fillFan(cx, cy, p, b);
    }

    /** 塗りつぶした正多角形。 */
    public void fillPolygon(float cx, float cy, float r, int sides, float rotDeg, float b) {
        fillFan(cx, cy, polygonPoints(cx, cy, r, sides, rotDeg), b);
    }

    /** 凸多角形の塗り（{@code xy} は x, y を交互に並べた頂点）。 */
    public void fillConvex(float b, float... xy) {
        int n = xy.length / 2;
        if (n < 3) {
            return;
        }
        float[][] p = new float[n][];
        float cx = 0.0F;
        float cy = 0.0F;
        for (int i = 0; i < n; i++) {
            p[i] = new float[] {xy[2 * i], xy[2 * i + 1]};
            cx += p[i][0];
            cy += p[i][1];
        }
        fillFan(cx / n, cy / n, p, b);
    }

    /**
     * 扇形の塗り: 中心 ({@code cx}, {@code cy})、半径 {@code r0} から {@code r1}、角度 {@code from} から {@code to}。
     * {@code r0} が 0 なら普通の扇形。
     */
    public void sector(float cx, float cy, float r0, float r1, float from, float to, float b) {
        if (r0 <= EPSILON) {
            boolean full = to - from >= 360.0F - EPSILON;
            Path arc = arcPath(cx, cy, r1, from, to, !full);
            float[][] p = new float[arc.size + (full ? 0 : 1)][];
            int i = 0;
            if (!full) {
                p[i++] = new float[] {cx, cy};
            }
            for (int k = 0; k < arc.size; k++) {
                p[i++] = new float[] {arc.x[k], arc.y[k]};
            }
            fillFan(cx, cy, p, b);
            return;
        }
        float saved = width;
        width = (r1 - r0) / lineScale;
        boolean savedCaps = caps;
        caps = true;
        float r = (r0 + r1) * 0.5F;
        if (to - from >= 360.0F - EPSILON) {
            stroke(arcPath(cx, cy, r, 0.0F, 360.0F, false), true, b);
        } else {
            stroke(arcPath(cx, cy, r, from, to, true), false, b);
        }
        width = saved;
        caps = savedCaps;
    }

    /**
     * 中心から見て星形の多角形（凹んでいてもよい）を、{@code (cx, cy)} から扇状に塗る。ふちは外向きにぼかす。
     * {@code points} は輪郭の頂点を順に並べたもの。
     */
    public void fillFan(float cx, float cy, float[][] points, float b) {
        int n = points.length;
        if (n < 3) {
            return;
        }
        for (int i = 0; i < n; i++) {
            float[] p = points[i];
            float[] q = points[(i + 1) % n];
            mesh.quad(cx, cy, b, cx, cy, b, p[0], p[1], b, q[0], q[1], b);
        }
        float area = 0.0F;
        for (int i = 0; i < n; i++) {
            float[] p = points[i];
            float[] q = points[(i + 1) % n];
            area += p[0] * q[1] - q[0] * p[1];
        }
        float sign = area > 0.0F ? -1.0F : 1.0F;
        float f = FEATHER * lineScale;
        float[][] out = new float[n][];
        for (int i = 0; i < n; i++) {
            float[] prev = points[(i + n - 1) % n];
            float[] p = points[i];
            float[] next = points[(i + 1) % n];
            float[] n0 = leftNormal(prev[0], prev[1], p[0], p[1]);
            float[] n1 = leftNormal(p[0], p[1], next[0], next[1]);
            float[] m = miter(n0, n1, FILL_MITER_LIMIT);
            out[i] = new float[] {m[0] * sign * f, m[1] * sign * f};
        }
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float[] p = points[i];
            float[] q = points[j];
            mesh.quad(p[0], p[1], b, p[0] + out[i][0], p[1] + out[i][1], 0.0F,
                    q[0] + out[j][0], q[1] + out[j][1], 0.0F, q[0], q[1], b);
        }
    }

    // ------------------------------------------------------------------ 線を帯にする

    private void stroke(Path path, boolean closed, float b) {
        path.dedupe(closed);
        if (path.size < 2) {
            return;
        }
        if (!clipping()) {
            strokeSplitSharp(path, closed, b);
            return;
        }
        List<Path> runs = visibleRuns(path, closed);
        if (runs == null) {
            strokeSplitSharp(path, closed, b);
            return;
        }
        for (Path run : runs) {
            run.dedupe(false);
            if (run.size >= 2 && run.length() > EPSILON * 10) {
                strokeSplitSharp(run, false, b);
            }
        }
    }

    /** 鋭い角で切り離してから描く。 */
    private void strokeSplitSharp(Path path, boolean closed, float b) {
        int n = path.size;
        if (n < 2) {
            return;
        }
        boolean[] sharp = new boolean[n];
        int first = -1;
        for (int i = 0; i < n; i++) {
            if (!closed && (i == 0 || i == n - 1)) {
                continue;
            }
            int prev = (i + n - 1) % n;
            int next = (i + 1) % n;
            float[] n0 = leftNormal(path.x[prev], path.y[prev], path.x[i], path.y[i]);
            float[] n1 = leftNormal(path.x[i], path.y[i], path.x[next], path.y[next]);
            float dot = n0[0] * n1[0] + n0[1] * n1[1];
            float cosHalf = (float) Math.sqrt(Math.max(0.0, (1.0 + dot) * 0.5));
            if (cosHalf < 1.0F / SHARP_MITER) {
                sharp[i] = true;
                if (first < 0) {
                    first = i;
                }
            }
        }
        if (first < 0) {
            emit(path, closed, b);
            return;
        }
        // 鋭い角から次の鋭い角までを、開いた線として描く。
        int count = closed ? n : n - 1;
        int start = closed ? first : 0;
        Path run = new Path();
        run.add(path.x[start], path.y[start]);
        for (int k = 1; k <= count; k++) {
            int i = (start + k) % n;
            run.add(path.x[i], path.y[i]);
            if (sharp[i] || k == count) {
                if (run.size >= 2) {
                    emit(run, false, b);
                }
                run = new Path();
                run.add(path.x[i], path.y[i]);
            }
        }
    }

    /** 芯の両側にぼかしを付けた帯を出す。開いた線なら両端もぼかす。 */
    private void emit(Path path, boolean closed, float b) {
        int n = path.size;
        float h = width * lineScale * 0.5F;
        float f = FEATHER * lineScale;
        float[][] m = new float[n][];
        for (int i = 0; i < n; i++) {
            boolean hasPrev = closed || i > 0;
            boolean hasNext = closed || i < n - 1;
            int prev = (i + n - 1) % n;
            int next = (i + 1) % n;
            if (hasPrev && hasNext) {
                float[] n0 = leftNormal(path.x[prev], path.y[prev], path.x[i], path.y[i]);
                float[] n1 = leftNormal(path.x[i], path.y[i], path.x[next], path.y[next]);
                m[i] = miter(n0, n1, SHARP_MITER);
            } else if (hasNext) {
                m[i] = leftNormal(path.x[i], path.y[i], path.x[next], path.y[next]);
            } else {
                m[i] = leftNormal(path.x[prev], path.y[prev], path.x[i], path.y[i]);
            }
        }
        int segments = closed ? n : n - 1;
        for (int i = 0; i < segments; i++) {
            int j = (i + 1) % n;
            float ax = path.x[i];
            float ay = path.y[i];
            float bx = path.x[j];
            float by = path.y[j];
            float[] ma = m[i];
            float[] mb = m[j];
            float ah = h;
            float ahf = h + f;
            mesh.quad(ax + ma[0] * ahf, ay + ma[1] * ahf, 0.0F, ax + ma[0] * ah, ay + ma[1] * ah, b,
                    bx + mb[0] * ah, by + mb[1] * ah, b, bx + mb[0] * ahf, by + mb[1] * ahf, 0.0F);
            mesh.quad(ax + ma[0] * ah, ay + ma[1] * ah, b, ax - ma[0] * ah, ay - ma[1] * ah, b,
                    bx - mb[0] * ah, by - mb[1] * ah, b, bx + mb[0] * ah, by + mb[1] * ah, b);
            mesh.quad(ax - ma[0] * ah, ay - ma[1] * ah, b, ax - ma[0] * ahf, ay - ma[1] * ahf, 0.0F,
                    bx - mb[0] * ahf, by - mb[1] * ahf, 0.0F, bx - mb[0] * ah, by - mb[1] * ah, b);
        }
        if (!closed && caps) {
            cap(path.x[0], path.y[0], path.x[1], path.y[1], m[0], h, f, b);
            cap(path.x[n - 1], path.y[n - 1], path.x[n - 2], path.y[n - 2], m[n - 1], h, f, b);
        }
    }

    /** 線の端 ({@code x}, {@code y}) を、内側の点 ({@code ix}, {@code iy}) と反対の向きにぼかす。 */
    private void cap(float x, float y, float ix, float iy, float[] m, float h, float f, float b) {
        float dx = x - ix;
        float dy = y - iy;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < EPSILON) {
            return;
        }
        float ex = dx / len * f;
        float ey = dy / len * f;
        float hf = h + f;
        float l2x = x + m[0] * hf;
        float l2y = y + m[1] * hf;
        float l1x = x + m[0] * h;
        float l1y = y + m[1] * h;
        float r1x = x - m[0] * h;
        float r1y = y - m[1] * h;
        float r2x = x - m[0] * hf;
        float r2y = y - m[1] * hf;
        mesh.quad(l2x, l2y, 0.0F, l1x, l1y, b, l1x + ex, l1y + ey, 0.0F, l2x + ex, l2y + ey, 0.0F);
        mesh.quad(l1x, l1y, b, r1x, r1y, b, r1x + ex, r1y + ey, 0.0F, l1x + ex, l1y + ey, 0.0F);
        mesh.quad(r1x, r1y, b, r2x, r2y, 0.0F, r2x + ex, r2y + ey, 0.0F, r1x + ex, r1y + ey, 0.0F);
    }

    // ------------------------------------------------------------------ 切り抜き

    private boolean clipping() {
        return !holes.isEmpty() || keepRadius >= 0.0F || keepHalf >= 0.0F;
    }

    private boolean visible(float x, float y) {
        if (keepRadius >= 0.0F && x * x + y * y >= keepRadius * keepRadius) {
            return false;
        }
        if (keepHalf >= 0.0F && (Math.abs(x) >= keepHalf || Math.abs(y) >= keepHalf)) {
            return false;
        }
        for (float[] hole : holes) {
            float dx = x - hole[0];
            float dy = y - hole[1];
            if (dx * dx + dy * dy < hole[2] * hole[2]) {
                return false;
            }
        }
        return true;
    }

    /** 見える部分ごとの折れ線。全部見えるなら null。 */
    private List<Path> visibleRuns(Path path, boolean closed) {
        int n = path.size;
        int segments = closed ? n : n - 1;
        List<Path> runs = new ArrayList<>();
        Path run = null;
        boolean allVisible = true;
        if (visible(path.x[0], path.y[0])) {
            run = new Path();
            run.add(path.x[0], path.y[0]);
        } else {
            allVisible = false;
        }
        for (int i = 0; i < segments; i++) {
            int j = (i + 1) % n;
            float ax = path.x[i];
            float ay = path.y[i];
            float bx = path.x[j];
            float by = path.y[j];
            float len = (float) Math.hypot(bx - ax, by - ay);
            int steps = Math.max(1, (int) Math.ceil(len / CLIP_STEP));
            float px = ax;
            float py = ay;
            boolean pv = visible(ax, ay);
            for (int s = 1; s <= steps; s++) {
                float t = (float) s / steps;
                float qx = ax + (bx - ax) * t;
                float qy = ay + (by - ay) * t;
                boolean qv = visible(qx, qy);
                if (qv != pv) {
                    allVisible = false;
                    float[] edge = boundary(px, py, pv, qx, qy);
                    if (pv) {
                        run.add(edge[0], edge[1]);
                        runs.add(run);
                        run = null;
                    } else {
                        run = new Path();
                        run.add(edge[0], edge[1]);
                    }
                }
                px = qx;
                py = qy;
                pv = qv;
            }
            if (run != null) {
                run.add(bx, by);
            }
        }
        if (allVisible) {
            return null;
        }
        if (run != null) {
            if (closed && !runs.isEmpty() && visible(path.x[0], path.y[0])) {
                // 継ぎ目をまたぐ部分は、最後の部分と最初の部分をつなぐ。
                Path head = runs.remove(0);
                for (int k = 1; k < head.size; k++) {
                    run.add(head.x[k], head.y[k]);
                }
            }
            runs.add(run);
        }
        return runs;
    }

    private float[] boundary(float ax, float ay, boolean aVisible, float bx, float by) {
        float lo = 0.0F;
        float hi = 1.0F;
        for (int k = 0; k < 14; k++) {
            float mid = (lo + hi) * 0.5F;
            boolean v = visible(ax + (bx - ax) * mid, ay + (by - ay) * mid);
            if (v == aVisible) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        float t = aVisible ? lo : hi;
        return new float[] {ax + (bx - ax) * t, ay + (by - ay) * t};
    }

    // ------------------------------------------------------------------ 形の点

    private Path arcPath(float cx, float cy, float r, float from, float to, boolean includeEnd) {
        float sweep = to - from;
        int n = segments(r, sweep);
        Path path = new Path();
        int last = includeEnd ? n : n - 1;
        for (int i = 0; i <= last; i++) {
            float a = rad(from + sweep * i / n);
            path.add(cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r);
        }
        return path;
    }

    public static float[][] polygonPoints(float cx, float cy, float r, int sides, float rotDeg) {
        float[][] p = new float[sides][];
        for (int k = 0; k < sides; k++) {
            float a = rad(rotDeg + 360.0F * k / sides);
            p[k] = new float[] {cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r};
        }
        return p;
    }

    /** 半径 {@code r} の円の {@code sweep} 度を、誤差 {@link #CHORD_ERROR} 以内で何本の線分にするか。 */
    private static int segments(float r, float sweep) {
        if (r <= CHORD_ERROR * 2.0F) {
            return Math.max(3, (int) Math.ceil(Math.abs(sweep) / 45.0F));
        }
        double step = 2.0 * Math.acos(1.0 - CHORD_ERROR / r);
        int n = (int) Math.ceil(Math.toRadians(Math.abs(sweep)) / step);
        return Math.max(Math.abs(sweep) >= 359.0F ? 12 : 2, Math.min(n, 720));
    }

    private static float[] leftNormal(float ax, float ay, float bx, float by) {
        float dx = bx - ax;
        float dy = by - ay;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < EPSILON) {
            return new float[] {0.0F, 0.0F};
        }
        return new float[] {-dy / len, dx / len};
    }

    /** 2 本の線分の法線から、留め継ぎの向きと伸び（長さ = 1 / cos(角の半分)、上限 {@code limit}）。 */
    private static float[] miter(float[] n0, float[] n1, float limit) {
        float sx = n0[0] + n1[0];
        float sy = n0[1] + n1[1];
        float len = (float) Math.sqrt(sx * sx + sy * sy);
        if (len < EPSILON) {
            return n1;
        }
        sx /= len;
        sy /= len;
        float cos = sx * n1[0] + sy * n1[1];
        float scale = cos < EPSILON ? limit : Math.min(limit, 1.0F / cos);
        return new float[] {sx * scale, sy * scale};
    }

    private static float rad(float deg) {
        return (float) Math.toRadians(deg);
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    /** 点の並び。 */
    private static final class Path {
        float[] x = new float[16];
        float[] y = new float[16];
        int size;

        void add(float px, float py) {
            if (size == x.length) {
                x = Arrays.copyOf(x, size * 2);
                y = Arrays.copyOf(y, size * 2);
            }
            x[size] = px;
            y[size] = py;
            size++;
        }

        /** 続けて同じ位置にある点を除く。閉じた線なら最後と最初の重なりも除く。 */
        void dedupe(boolean closed) {
            if (size < 2) {
                return;
            }
            int w = 1;
            for (int i = 1; i < size; i++) {
                if (Math.abs(x[i] - x[w - 1]) > EPSILON || Math.abs(y[i] - y[w - 1]) > EPSILON) {
                    x[w] = x[i];
                    y[w] = y[i];
                    w++;
                }
            }
            if (closed && w > 1 && Math.abs(x[w - 1] - x[0]) <= EPSILON && Math.abs(y[w - 1] - y[0]) <= EPSILON) {
                w--;
            }
            size = w;
        }

        float length() {
            float total = 0.0F;
            for (int i = 1; i < size; i++) {
                total += (float) Math.hypot(x[i] - x[i - 1], y[i] - y[i - 1]);
            }
            return total;
        }
    }
}
