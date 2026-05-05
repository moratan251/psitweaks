package com.moratan251.psitweaks.client.renderer;

import com.moratan251.psitweaks.common.entities.SpellGram.EntityFlareCircle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.client.render.entity.RenderSpellCircle;

public class FlareCircleRenderer extends EntityRenderer<EntityFlareCircle> {

    private static final int FIXED_RED_COLOR = 0xFF0000;

    public FlareCircleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityFlareCircle entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        float horizontalScale = entity.getVisualScale();
        if (horizontalScale <= 0.0F) {
            return;
        }

        float alive = entity.tickCount + partialTicks;
        RenderSpellCircle.renderSpellCircle(
                alive,
                1.0F,
                horizontalScale,
                0.0F,
                1.0F,
                0.0F,
                FIXED_RED_COLOR,
                poseStack,
                buffer
        );

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityFlareCircle entity) {
        return ResourceLocation.fromNamespaceAndPath("psi", "textures/misc/spell_circle0.png");
    }
}
