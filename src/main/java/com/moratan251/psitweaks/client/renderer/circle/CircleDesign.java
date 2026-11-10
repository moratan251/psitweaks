package com.moratan251.psitweaks.client.renderer.circle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 数式で組み立てる魔法陣の図案: 重ねて描く層の並び。層ごとに形（{@link CirclePen} で描く手順）と回る速さを持つ。図形ごとに
 * CAD の色か白か（{@link CirclePen#white()}）を選べる。
 * <p>
 * ゲームは形から焼いた画像で描く（{@link TextureCircleRenderer}）。形そのものは画像の生成ツールとプレビューが使い、線の太さの
 * 倍率ごとに 1 回だけ組み立てて取っておく（倍率は決まった値だけを使うので、取っておく数は増え続けない）。
 */
public final class CircleDesign {
    /** 1 枚の層。{@code spin} は 1 tick に回る角度（度、正なら画面で時計回り）。 */
    public record Layer(float spin, Consumer<CirclePen> painter) {
    }

    private final String id;
    private final boolean square;
    private final List<Layer> layers;
    /** 線の太さの倍率ごとの形（[0] が CAD の色、[1] が白、それぞれ層ごと）。 */
    private final Map<Float, CircleMesh[][]> meshes = new HashMap<>();

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

    /** 線の太さの倍率 {@code lineScale} で組み立てた、層ごとの CAD の色で描く形。 */
    public CircleMesh[] meshes(float lineScale) {
        return built(lineScale)[0];
    }

    /** 線の太さの倍率 {@code lineScale} で組み立てた、層ごとの白で描く形（{@link CirclePen#white()}。ない層は頂点 0）。 */
    public CircleMesh[] whiteMeshes(float lineScale) {
        return built(lineScale)[1];
    }

    private synchronized CircleMesh[][] built(float lineScale) {
        return meshes.computeIfAbsent(lineScale, scale -> {
            CircleMesh[][] built = new CircleMesh[2][layers.size()];
            for (int i = 0; i < layers.size(); i++) {
                CircleMesh color = new CircleMesh();
                CircleMesh white = new CircleMesh();
                layers.get(i).painter().accept(new CirclePen(color, white, scale));
                built[0][i] = color.trim();
                built[1][i] = white.trim();
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
