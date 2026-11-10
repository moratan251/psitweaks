package com.moratan251.psitweaks.client.renderer.circle;

import java.util.Arrays;

/**
 * 魔法陣の 1 層ぶんの形: 層の平面に置いた四角形（頂点 4 つ）の並び。頂点ごとに位置 (x, y) と明るさ (0〜1) を持つ。
 * 単位は「外周の半径が 64」の図面の座標で、y は画面の下向き（床に置くと南向き）。
 * <p>
 * Minecraft のクラスを使わないので、ゲームの外のプレビューの道具でも同じ形を作れる。
 */
public final class CircleMesh {
    private static final int FLOATS_PER_VERTEX = 3;

    private float[] data = new float[FLOATS_PER_VERTEX * 4 * 256];
    private int size;

    void vertex(float x, float y, float brightness) {
        if (size + FLOATS_PER_VERTEX > data.length) {
            data = Arrays.copyOf(data, data.length * 2);
        }
        data[size++] = x;
        data[size++] = y;
        data[size++] = brightness;
    }

    void quad(float x0, float y0, float b0, float x1, float y1, float b1,
              float x2, float y2, float b2, float x3, float y3, float b3) {
        if (b0 <= 0.0F && b1 <= 0.0F && b2 <= 0.0F && b3 <= 0.0F) {
            return;
        }
        vertex(x0, y0, b0);
        vertex(x1, y1, b1);
        vertex(x2, y2, b2);
        vertex(x3, y3, b3);
    }

    /** 余った領域を切り詰める（作り終えたら 1 回呼ぶ）。 */
    CircleMesh trim() {
        if (data.length != size) {
            data = Arrays.copyOf(data, size);
        }
        return this;
    }

    public int vertexCount() {
        return size / FLOATS_PER_VERTEX;
    }

    public float x(int vertex) {
        return data[vertex * FLOATS_PER_VERTEX];
    }

    public float y(int vertex) {
        return data[vertex * FLOATS_PER_VERTEX + 1];
    }

    public float brightness(int vertex) {
        return data[vertex * FLOATS_PER_VERTEX + 2];
    }
}
