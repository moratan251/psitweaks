package com.moratan251.psitweaks.client.renderer.circle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 数式で組み立てる魔法陣の図案: 重ねて描く層の並び。層ごとに形（{@link CirclePen} で描く手順）と回る速さを持つ。
 * <p>
 * 形は線の太さの倍率ごとに 1 回だけ組み立てて取っておき、毎フレームは回して色を掛けるだけにする。倍率は呼び出す側が決まった
 * 値（定数）だけを使うので、取っておく数は増え続けない。
 */
public final class CircleDesign {
    /** 1 枚の層。{@code spin} は 1 tick に回る角度（度、正なら画面で時計回り）。 */
    public record Layer(float spin, Consumer<CirclePen> painter) {
    }

    private final String id;
    private final boolean square;
    private final List<Layer> layers;
    private final Map<Float, CircleMesh[]> meshes = new HashMap<>();

    private CircleDesign(String id, boolean square, List<Layer> layers) {
        this.id = id;
        this.square = square;
        this.layers = List.copyOf(layers);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String id() {
        return id;
    }

    /** 四角い図案か（領域の床に一辺いっぱいに敷く）。丸い図案は内接円に収める。 */
    public boolean square() {
        return square;
    }

    public int layerCount() {
        return layers.size();
    }

    public Layer layer(int index) {
        return layers.get(index);
    }

    /** 線の太さの倍率 {@code lineScale} で組み立てた、層ごとの形。 */
    public synchronized CircleMesh[] meshes(float lineScale) {
        return meshes.computeIfAbsent(lineScale, scale -> {
            CircleMesh[] built = new CircleMesh[layers.size()];
            for (int i = 0; i < built.length; i++) {
                CircleMesh mesh = new CircleMesh();
                layers.get(i).painter().accept(new CirclePen(mesh, scale));
                built[i] = mesh.trim();
            }
            return built;
        });
    }

    public static final class Builder {
        private final String id;
        private final List<Layer> layers = new ArrayList<>();
        private boolean square;

        private Builder(String id) {
            this.id = id;
        }

        public Builder square() {
            this.square = true;
            return this;
        }

        public Builder layer(float spin, Consumer<CirclePen> painter) {
            layers.add(new Layer(spin, painter));
            return this;
        }

        public CircleDesign build() {
            return new CircleDesign(id, square, layers);
        }
    }
}
