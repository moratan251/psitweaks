package com.moratan251.psitweaks.client.renderer.circle;

import java.io.IOException;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * 魔法陣の 1 層の画像。読み込んだ画像の一辺の画素数を覚える（{@link TextureCircleRenderer} が、層の四角形の大きさを画像の大きさ
 * から決めるため）。リソースの再読み込みでは {@code TextureManager} が同じオブジェクトを読み直すので、そのたびに覚え直す。
 */
final class CircleLayerTexture extends SimpleTexture {
    private volatile int side;

    CircleLayerTexture(ResourceLocation location) {
        super(location);
    }

    /** 読み込んだ画像の一辺（画素）。読めなかったときは 0。 */
    int side() {
        return side;
    }

    @Override
    protected TextureImage getTextureImage(ResourceManager resourceManager) {
        TextureImage image = super.getTextureImage(resourceManager);
        try {
            side = image.getImage().getWidth();
        } catch (IOException missing) {
            side = 0;
        }
        return image;
    }
}
