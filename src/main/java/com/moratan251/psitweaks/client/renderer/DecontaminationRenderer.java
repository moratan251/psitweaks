package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityDecontamination;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** 作動式: 除染の核の見た目: CAD の色の核の魔法陣（{@link SpellGramCoreRenderer}）だけ。光の粒は核とサーバーが出す。 */
public class DecontaminationRenderer extends EntityRenderer<EntityDecontamination> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("psi", "textures/misc/spell_circle0.png");

    public DecontaminationRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityDecontamination entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        SpellGramCoreRenderer.render(entity, entity.getColor(), 0.0F, partialTicks, entityRenderDispatcher.camera, poseStack, buffer);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityDecontamination entity) {
        return TEXTURE;
    }
}
