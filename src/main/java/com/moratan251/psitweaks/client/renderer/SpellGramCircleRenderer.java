package com.moratan251.psitweaks.client.renderer;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramCircle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import vazkii.psi.client.render.entity.RenderSpellCircle;

/** 円の魔法式オブジェクト（{@link SpellGramCircle}）を Psi の魔法陣で、円ごとの色で描く。 */
public class SpellGramCircleRenderer<T extends SpellGramCircle> extends EntityRenderer<T> {

    /** ファイアサークルの色。 */
    public static final int FLARE_COLOR = 0xFF0000;
    /** アイスサークルの色。 */
    public static final int ICE_COLOR = 0x00CCFF;

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("psi", "textures/misc/spell_circle0.png");

    private final int color;

    public SpellGramCircleRenderer(EntityRendererProvider.Context context, int color) {
        super(context);
        this.color = color;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks,
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
                color,
                poseStack,
                buffer
        );

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return TEXTURE;
    }
}
