package com.moratan251.psitweaks.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * 何も描画しないエンティティレンダラー
 * パーティクルで視覚効果を表現するエンティティに使用
 */
public class EmptyRenderer<T extends Entity> extends EntityRenderer<T> {

    public EmptyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return null;
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTicks,
                       com.mojang.blaze3d.vertex.@NotNull PoseStack poseStack,
                       net.minecraft.client.renderer.@NotNull MultiBufferSource buffer,
                       int packedLight) {
        // 何も描画しない
    }
}