package com.moratan251.psitweaks.client.renderer;

import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TunnelerArrowRenderer extends ArrowRenderer<EntityTunnelerArrow> {
    // 専用テクスチャ未用意のため、当面はバニラ矢のテクスチャを使用
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public TunnelerArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTunnelerArrow entity) {
        return TEXTURE;
    }
}
