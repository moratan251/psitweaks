package com.moratan251.psitweaks.client.renderer.circle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.client.config.PsitweaksClientConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * 数式で組み立てた魔法陣（{@link CircleDesign}）を、層ごとに焼いた画像
 * （{@code textures/misc/magic_circle/<図案>_<層>_<大きさ>.png}）で描く。層ごとに四角形 1 枚を、その層の速さで回し、CAD の色を
 * 頂点色で掛けて加算合成で描く（{@link PsitweaksRenderTypes#additiveLinearTexture}）。毎フレームの仕事は四角形を数枚書くだけ。
 * <p>
 * 画像は記録フォルダ（{@code .steering/202611100300-zone-vector-magic-circle/tools/}）の生成ツール CircleTextureGen で、
 * {@link CircleDesign} の形から作る。図案を直したら画像を作り直す。明るさは R = G = B の灰色（1 で頭打ち）。中心は画素の格子の
 * 中心で、層が持つ正方形の対称（左右・上下・対角の鏡映、90 度ごとの回転）は画素の単位で厳密にそろえてある。
 * <p>
 * 画像を焼いてある図案だけを渡すこと（今は {@link CircleDesigns#CONSTRUCTION}）。ミップマップは使わず、画面に映る大きさ以上の
 * いちばん小さい段（1024 / 512 / 256 / 128 px）を選ぶ（縮小しすぎるとちらつくため）。
 * <p>
 * 内側の層の画像は、生成ツールが形の届く範囲だけに切り詰めてある（段の細かさは同じで、一辺が短い。例: F の中心の層は 1024 の段で
 * 416 px）。描画は読み込んだ画像の一辺から層の四角形の大きさを決める（一辺 / 段の大きさ × {@link #EXTENT}）ので、どの図案でも
 * コードを変えずに効く。
 */
public final class TextureCircleRenderer {
    /** 画像の一辺の半分（図面の単位、外周の半径が 64）。生成ツールの EXTENT と同じ値。 */
    public static final float EXTENT = 66.0F;
    private static final int[] SIZES = {1024, 512, 256, 128};
    /** 層どうしの高さの間隔（ブロック）。深度を書かないので重なりのちらつきはないが、真横から見たときに厚みが出ない程度。 */
    private static final float LAYER_GAP = 0.003F;
    /** 段を替えるときに境目からはみ出す割合（境目で段が行き来しないための幅）。 */
    private static final float HYSTERESIS = 0.1F;
    /** 図案ごとの画像の場所（{@code [層][大きさ]}、{@link #SIZES} の順）。図案は決まった数なので増え続けない。 */
    private static final Map<String, ResourceLocation[][]> TEXTURES = new ConcurrentHashMap<>();

    private TextureCircleRenderer() {
    }

    /**
     * 魔法陣を {@code poseStack} の原点を中心に、x-z 平面（水平）に寝かせて描く。
     *
     * @param radius     外周の半径（ブロック）。四角い図案では一辺の半分。画像はその {@code EXTENT / 64} 倍の大きさ。
     * @param distance   カメラから魔法陣の中心までの距離（ブロック）。画像の大きさを選ぶのに使う。
     * @param previous   この魔法陣に前のフレームで選んだ段（この関数の戻り値。初めては -1）。境目で段が行き来しないように使う。
     * @param time       回転に使う経過 tick（partialTick 込み）
     * @param color      0xRRGGBB。暗い色は加算で見えなくなるので、明るさを持ち上げてから使う。
     * @param brightness 全体の明るさ（0〜1）
     * @return 次のフレームに {@code previous} として渡す段（描かなかったときは {@code previous} のまま）
     */
    public static int render(CircleDesign design, PoseStack poseStack, MultiBufferSource buffers, float radius,
                             float distance, int previous, float time, int color, float brightness) {
        if (radius <= 0.0F || brightness <= 0.0F) {
            return previous;
        }
        int glow = GlowDrawing.glowColor(color & 0xFFFFFF);
        int red = Math.min(255, (int) (GlowDrawing.red(glow) * brightness));
        int green = Math.min(255, (int) (GlowDrawing.green(glow) * brightness));
        int blue = Math.min(255, (int) (GlowDrawing.blue(glow) * brightness));
        float fullHalf = radius * EXTENT / 64.0F;
        ResourceLocation[][] textures = TEXTURES.computeIfAbsent(design.id(), id -> locations(design));
        int level = sizeIndex(fullHalf, distance, previous);
        int size = Math.max(level, maxSizeIndex());
        Matrix4f pose = poseStack.last().pose();
        for (int layer = 0; layer < design.layerCount(); layer++) {
            ResourceLocation texture = textures[layer][size];
            int side = side(texture);
            if (side <= 0) {
                continue;
            }
            // 内側の層の画像は、使う範囲だけに切り詰めてある（同じ細かさのまま一辺が短い）。
            float half = fullHalf * side / SIZES[size];
            VertexConsumer consumer = buffers.getBuffer(PsitweaksRenderTypes.additiveLinearTexture(texture));
            float angle = design.layer(layer).spin() * time % 360.0F * Mth.DEG_TO_RAD;
            float cos = Mth.cos(angle) * half;
            float sin = Mth.sin(angle) * half;
            float y = layer * LAYER_GAP;
            // 画像の u は図面の +x、v は図面の +y（床では +z）。
            vertex(consumer, pose, -1.0F, -1.0F, cos, sin, y, 0.0F, 0.0F, red, green, blue);
            vertex(consumer, pose, -1.0F, 1.0F, cos, sin, y, 0.0F, 1.0F, red, green, blue);
            vertex(consumer, pose, 1.0F, 1.0F, cos, sin, y, 1.0F, 1.0F, red, green, blue);
            vertex(consumer, pose, 1.0F, -1.0F, cos, sin, y, 1.0F, 0.0F, red, green, blue);
        }
        return level;
    }

    /**
     * 層の画像の一辺（画素）。初めて使うときに {@link CircleLayerTexture} として読み込む。読めなかった画像（Minecraft の
     * 「見つからないテクスチャ」が登録されている）は 0 を返し、その層は描かない（毎フレーム読み直さない）。
     */
    private static int side(ResourceLocation location) {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        AbstractTexture texture = textures.getTexture(location, null);
        if (texture instanceof CircleLayerTexture layer) {
            return layer.side();
        }
        if (texture == MissingTextureAtlasSprite.getTexture()) {
            return 0;
        }
        textures.register(location, new CircleLayerTexture(location));
        return textures.getTexture(location, null) instanceof CircleLayerTexture layer ? layer.side() : 0;
    }

    private static ResourceLocation[][] locations(CircleDesign design) {
        ResourceLocation[][] locations = new ResourceLocation[design.layerCount()][SIZES.length];
        for (int layer = 0; layer < locations.length; layer++) {
            for (int s = 0; s < SIZES.length; s++) {
                locations[layer][s] = ResourceLocation.fromNamespaceAndPath(Psitweaks.MOD_ID,
                        "textures/misc/magic_circle/" + design.id() + "_" + layer + "_" + SIZES[s] + ".png");
            }
        }
        return locations;
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float z, float cos, float sin, float y,
                               float u, float v, int red, int green, int blue) {
        consumer.addVertex(pose, x * cos - z * sin, y, x * sin + z * cos).setUv(u, v).setColor(red, green, blue, 255);
    }

    /**
     * 一辺 {@code 2 * half} ブロックの画像を {@code distance} 離れて見たときに、画面に映る大きさ（画素、斜めに見たときの縮みは
     * 数えない）以上のいちばん小さい段。近くで 1024 より大きく映るときは 1024。
     * <p>
     * 境目のちょうど上でカメラが揺れても段が毎フレーム行き来しないよう、前の段 {@code previous} は、映る大きさがその段の
     * 範囲から {@link #HYSTERESIS} の割合だけはみ出すまで使い続ける（細かい段へは上の境目の 1.1 倍、粗い段へは下の境目の
     * 0.9 倍で替わる）。
     */
    static int sizeIndex(float half, float distance, int previous) {
        Minecraft minecraft = Minecraft.getInstance();
        float pixelsPerRadian = minecraft.getWindow().getHeight()
                / (2.0F * (float) Math.tan(Math.toRadians(minecraft.options.fov().get()) * 0.5));
        return sizeIndex(2.0F * half / Math.max(distance, 0.5F) * pixelsPerRadian, previous);
    }

    /** 画面に映る大きさ {@code onScreen}（画素）から段を選ぶ（{@link #sizeIndex(float, float, int)}）。 */
    static int sizeIndex(float onScreen, int previous) {
        if (previous >= 0 && previous < SIZES.length) {
            float upper = previous == 0 ? Float.MAX_VALUE : SIZES[previous] * (1.0F + HYSTERESIS);
            float lower = previous == SIZES.length - 1 ? 0.0F : SIZES[previous + 1] * (1.0F - HYSTERESIS);
            if (onScreen > lower && onScreen <= upper) {
                return previous;
            }
        }
        int index = 0;
        while (index + 1 < SIZES.length && SIZES[index + 1] >= onScreen) {
            index++;
        }
        return index;
    }

    /** クライアント設定 {@code effects.zoneCircleQuality}（魔法陣の画質）で決まる、いちばん細かい段。 */
    private static int maxSizeIndex() {
        int pixels = PsitweaksClientConfig.CLIENT.zoneCircleQuality.get().pixels();
        int index = 0;
        while (index + 1 < SIZES.length && SIZES[index] > pixels) {
            index++;
        }
        return index;
    }
}
