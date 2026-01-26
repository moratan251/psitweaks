package com.moratan251.psitweaks.client.renderer;

import com.moratan251.psitweaks.common.entities.EntityTimeAccelerator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityTimeAcceleratorRenderer extends EntityRenderer<EntityTimeAccelerator> {

    public EntityTimeAcceleratorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EntityTimeAccelerator entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {

        // 表示テキスト
        int rate = entity.getTimeRate();
        int sec = Math.max(0, (int) Math.ceil(entity.getRemainingTime() / 20.0));

        String line1 = "x" + rate;
        String line2 = sec + "s";

        // TIAB風：6面に描画（少し外側へ出す）
        // エンティティはブロック中心(0.5,0.5,0.5)にいる想定
        // translateのZ/Xが±0.51くらいだと面の外側に出せる
        float z = 0.51F;
        float x = 0.51F;
        float y = 0.064F;

        // 文字色（好みで）
        int colorRate = ChatFormatting.WHITE.getColor();
        int colorTime = ChatFormatting.AQUA.getColor();
        int fullBright = 0xF000F0;

        // Front
        drawTwoLines(poseStack, buffer, line1, line2, new Vector3f(0f, y, z), Axis.YP.rotationDegrees(0f), colorRate, colorTime, fullBright);
        // Back
        drawTwoLines(poseStack, buffer, line1, line2, new Vector3f(0f, y, -z), Axis.YP.rotationDegrees(180f), colorRate, colorTime, fullBright);
        // Right
        drawTwoLines(poseStack, buffer, line1, line2, new Vector3f(x, y, 0f), Axis.YP.rotationDegrees(90f), colorRate, colorTime, fullBright);
        // Left
        drawTwoLines(poseStack, buffer, line1, line2, new Vector3f(-x, y, 0f), Axis.YP.rotationDegrees(-90f), colorRate, colorTime, fullBright);

        // Top（上面はAxis.XPで回す）
        drawTwoLines(poseStack, buffer, line1, line2, new Vector3f(0f, z, 0f), Axis.XP.rotationDegrees(90f), colorRate, colorTime, fullBright);
        // Bottom
        drawTwoLines(poseStack, buffer, line1, line2, new Vector3f(0f, -z, 0f), Axis.XP.rotationDegrees(-90f), colorRate, colorTime, fullBright);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, fullBright);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityTimeAccelerator entity) {
        return null;
    }

    private void drawTwoLines(
            PoseStack poseStack,
            MultiBufferSource buffer,
            String line1,
            String line2,
            Vector3f translate,
            Quaternionf rotate,
            int color1,
            int color2,
            int packedLight
    ) {
        poseStack.pushPose();

        // 面へ移動
        poseStack.translate(translate.x(), translate.y(), translate.z());

        // 文字サイズ
        float scale = 0.02F;
        poseStack.scale(scale, -scale, scale);

        // 面の向き
        poseStack.mulPose(rotate);

        Font font = Minecraft.getInstance().font;
        Matrix4f mat = poseStack.last().pose();

        // 2行中央寄せ
        float w1 = font.width(line1);
        float w2 = font.width(line2);
        float x1 = -w1 / 2.0F;
        float x2 = -w2 / 2.0F;

        // 行間（ピクセル単位）
        float y1 = 0;
        float y2 = 9;

        font.drawInBatch(line1, x1, y1, color1, false, mat, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
        font.drawInBatch(line2, x2, y2, color2, false, mat, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }
}