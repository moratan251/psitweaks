package com.moratan251.psitweaks.client.head;

import com.moratan251.psitweaks.Psitweaks;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

/**
 * 魔法師の頭のテクスチャ。村人の素の見た目 (villager.png) に魔法師の服 (spellcaster.png) を重ねて、読み込み時に作る。
 * 村人の描画と同じ重ね方なので、リソースパックで村人や魔法師の見た目を変えると頭も同じように変わる。
 */
public class MagiciansHeadTexture extends SimpleTexture {
    /** 作ったテクスチャを置く場所。この名前のファイルは無い。 */
    public static final ResourceLocation LOCATION = Psitweaks.location("textures/entity/magicians_head.png");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation VILLAGER = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
    private static final ResourceLocation PROFESSION = Psitweaks.location("textures/entity/villager/profession/spellcaster.png");
    // 実体の描画 (entity_cutout) が捨てる透明度 (0.1 未満) と同じ境目。これ以上なら上の層が下を隠す。
    private static final int CUTOUT_ALPHA = 26;

    public MagiciansHeadTexture() {
        super(LOCATION);
    }

    @Override
    protected TextureImage getTextureImage(ResourceManager resourceManager) {
        NativeImage villager = read(resourceManager, VILLAGER);
        NativeImage profession = read(resourceManager, PROFESSION);
        try {
            if (villager == null && profession == null) {
                return new TextureImage(new FileNotFoundException(VILLAGER + ", " + PROFESSION));
            }
            return new TextureImage(null, compose(villager, profession));
        } finally {
            if (villager != null) {
                villager.close();
            }
            if (profession != null) {
                profession.close();
            }
        }
    }

    /**
     * 下の層に上の層を重ねる。大きさが違うとき (高解像度のリソースパック) は大きいほうに合わせて引き伸ばす。
     */
    private static NativeImage compose(@Nullable NativeImage bottom, @Nullable NativeImage top) {
        int width = Math.max(width(bottom), width(top));
        int height = Math.max(height(bottom), height(top));
        NativeImage result = new NativeImage(width, height, true);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int upper = sample(top, x, y, width, height);
                result.setPixelRGBA(x, y, (upper >>> 24) >= CUTOUT_ALPHA ? upper : sample(bottom, x, y, width, height));
            }
        }
        return result;
    }

    private static int sample(@Nullable NativeImage image, int x, int y, int width, int height) {
        if (image == null) {
            return 0;
        }
        return image.getPixelRGBA(x * image.getWidth() / width, y * image.getHeight() / height);
    }

    private static int width(@Nullable NativeImage image) {
        return image == null ? 0 : image.getWidth();
    }

    private static int height(@Nullable NativeImage image) {
        return image == null ? 0 : image.getHeight();
    }

    @Nullable
    private static NativeImage read(ResourceManager resourceManager, ResourceLocation location) {
        Optional<Resource> resource = resourceManager.getResource(location);
        if (resource.isEmpty()) {
            LOGGER.warn("Magician's Head texture: {} is missing", location);
            return null;
        }
        try (InputStream stream = resource.get().open()) {
            return NativeImage.read(stream);
        } catch (IOException e) {
            LOGGER.warn("Magician's Head texture: failed to read {}", location, e);
            return null;
        }
    }
}
