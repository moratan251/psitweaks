package com.moratan251.psitweaks.client.render.tile;

import com.mojang.blaze3d.platform.NativeImage;
import com.moratan251.psitweaks.common.items.ItemPsitweaksProgram;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * プログラム研究台のホログラムに描く記号。完成品のプログラムのアイテムテクスチャから、暗い地（カードの地と縁）を
 * 除いた画素だけを取り出す。白・灰の画素はホログラムの色に染め、色の付いた画素はその色のまま描く。
 * <p>
 * 結果はアイテムごとにキャッシュし、リソースの再読み込みで捨てる（テクスチャの中身とアトラスが入れ替わるため）。
 * 描画スレッドからだけ触る。
 */
public final class ProgramHologramGlyphs implements ResourceManagerReloadListener {
    public static final ProgramHologramGlyphs RELOAD_LISTENER = new ProgramHologramGlyphs();

    /** 無彩色の記号の画素に掛ける色（ARGB）。白はこの色になる。 */
    public static final int NEUTRAL_TINT = 0xFFC8F6FF;
    private static final int UNTINTED = 0xFFFFFFFF;
    /** これ以下の明るさで彩度の低い画素は、カードの地や縁として描かない。 */
    private static final int BACKGROUND_MAX_BRIGHTNESS = 100;
    /** RGB の最大と最小の差がこれ以下なら無彩色とみなす。 */
    private static final int NEUTRAL_MAX_SPREAD = 24;
    private static final int MIN_SIZE = 4;
    private static final int MAX_SIZE = 64;

    private static final Map<Item, Optional<Glyph>> CACHE = new HashMap<>();

    /**
     * @param sprite 記号の色を読むスプライト（ブロックアトラス）
     * @param size   テクスチャの一辺の画素数
     * @param pixels 描く画素の {@code x | y << 8}
     * @param tints  各画素に掛ける色（ARGB）
     */
    public record Glyph(TextureAtlasSprite sprite, int size, int[] pixels, int[] tints) {
    }

    private ProgramHologramGlyphs() {
    }

    /** 描けるプログラムなら記号を返す。白紙のプログラム、プログラムでないアイテム、記号を取り出せないものは null。 */
    @Nullable
    public static Glyph get(Item item) {
        if (!(item instanceof ItemPsitweaksProgram program) || program.isBlankProgram()) {
            return null;
        }
        return CACHE.computeIfAbsent(item, ProgramHologramGlyphs::extract).orElse(null);
    }

    private static Optional<Glyph> extract(Item item) {
        TextureAtlasSprite sprite = iconSprite(item);
        if (sprite == null
                || !InventoryMenu.BLOCK_ATLAS.equals(sprite.atlasLocation())
                || MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
            return Optional.empty();
        }
        SpriteContents contents = sprite.contents();
        int size = contents.width();
        if (size != contents.height() || size < MIN_SIZE || size > MAX_SIZE) {
            return Optional.empty();
        }
        NativeImage image = contents.getOriginalImage();
        if (image.format() != NativeImage.Format.RGBA || image.getWidth() < size || image.getHeight() < size) {
            return Optional.empty();
        }
        List<int[]> found = new ArrayList<>();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int abgr = image.getPixelRGBA(x, y);
                if (FastColor.ABGR32.alpha(abgr) < 128) {
                    continue;
                }
                int r = FastColor.ABGR32.red(abgr);
                int g = FastColor.ABGR32.green(abgr);
                int b = FastColor.ABGR32.blue(abgr);
                int max = Math.max(r, Math.max(g, b));
                int spread = max - Math.min(r, Math.min(g, b));
                boolean neutral = spread <= NEUTRAL_MAX_SPREAD;
                if (neutral && max <= BACKGROUND_MAX_BRIGHTNESS) {
                    continue;
                }
                found.add(new int[]{x | y << 8, neutral ? NEUTRAL_TINT : UNTINTED});
            }
        }
        if (found.isEmpty()) {
            return Optional.empty();
        }
        int[] pixels = new int[found.size()];
        int[] tints = new int[found.size()];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = found.get(i)[0];
            tints[i] = found.get(i)[1];
        }
        return Optional.of(new Glyph(sprite, size, pixels, tints));
    }

    @Nullable
    private static TextureAtlasSprite iconSprite(Item item) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(new ItemStack(item), null, null, 0);
        List<BakedQuad> quads = model.getQuads(null, null, RandomSource.create(0L));
        if (!quads.isEmpty()) {
            return quads.getFirst().getSprite();
        }
        return model.getParticleIcon();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        CACHE.clear();
    }
}
