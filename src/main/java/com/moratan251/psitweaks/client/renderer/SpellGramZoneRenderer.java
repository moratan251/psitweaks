package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.SpellGramZone;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

/**
 * 領域型の魔法式オブジェクトの輪郭: 範囲を囲む箱の 12 本の辺を、領域の種類ごとの色の線で描く（試作）。
 * 耐久値が減るほど薄くなり、残りが 4 分の 1 を切るとちらつく。中心には術者の CAD の色の核の魔法陣（{@link SpellGramCoreRenderer}）。
 */
public class SpellGramZoneRenderer<T extends SpellGramZone> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("psi", "textures/misc/spell_circle0.png");

    public SpellGramZoneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        AABB outline = entity.getOutline().move(-entity.getX(), -entity.getY(), -entity.getZ());
        float max = entity.getMaxIntegrity();
        float fraction = max > 0.0F ? Mth.clamp(entity.getIntegrity() / max, 0.0F, 1.0F) : 1.0F;
        float alpha = 0.35F + 0.65F * fraction;
        if (fraction < 0.25F) {
            float time = entity.tickCount + partialTicks;
            alpha *= 0.6F + 0.4F * Mth.sin(time * 1.3F);
        }
        int edge = entity.getOutlineColor();
        LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(RenderType.lines()), outline,
                ((edge >> 16) & 0xFF) / 255.0F, ((edge >> 8) & 0xFF) / 255.0F, (edge & 0xFF) / 255.0F, alpha);
        SpellGramCoreRenderer.render(entity, entity.getColor(), 0.0F, partialTicks, entityRenderDispatcher.camera, poseStack, buffer);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public boolean shouldRender(@NotNull T entity, @NotNull net.minecraft.client.renderer.culling.Frustum frustum,
                                double camX, double camY, double camZ) {
        return frustum.isVisible(entity.getOutline());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return TEXTURE;
    }
}
