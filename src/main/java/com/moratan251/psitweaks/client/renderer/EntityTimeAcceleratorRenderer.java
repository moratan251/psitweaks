package com.moratan251.psitweaks.client.renderer;

import com.moratan251.psitweaks.common.entities.EntityTimeAccelerator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class EntityTimeAcceleratorRenderer extends EntityRenderer<EntityTimeAccelerator> {

    public EntityTimeAcceleratorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntityTimeAccelerator entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {

        // 画面上に出る文字列
        int rate = entity.getTimeRate();
        int sec = entity.getRemainingTime() / 20;

        Component text = Component.literal("x" + rate + "  " + sec + "s");

        poseStack.pushPose();

        // エンティティ位置（ブロック中心）から少し上へ
        poseStack.translate(0.0, 0.6, 0.0);

        // 常にカメラ���向ける（ビルボード）
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // 文字は小さく
        float scale = 0.025f;
        poseStack.scale(-scale, -scale, scale);

        Font font = Minecraft.getInstance().font;
        Matrix4f mat = poseStack.last().pose();

        // 中央寄せ
        float x = -font.width(text) / 2.0f;

        // drawInBatch: 影付き・半透明背景なしのシンプル表示
        font.drawInBatch(
                text,
                x,
                0,
                0xFFFFFF,
                false,
                mat,
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityTimeAccelerator entity) {
        return null;
    }
}