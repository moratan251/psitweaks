package com.moratan251.psitweaks.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.moratan251.psitweaks.common.entities.EntityDryIceProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public class DryIceProjectileRenderer extends EntityRenderer<EntityDryIceProjectile> {

    private static final float MODEL_SCALE = 0.75F;
    private static final float MODEL_DIRECTION_YAW_OFFSET = -90.0F;

    private final ItemRenderer itemRenderer;

    public DryIceProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.15F;
    }

    @Override
    public void render(
            EntityDryIceProjectile projectile,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);

        float pitch = Mth.lerp(partialTick, projectile.xRotO, projectile.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(MODEL_DIRECTION_YAW_OFFSET));

        itemRenderer.renderStatic(
                projectile.getItem(),
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                projectile.level(),
                projectile.getId()
        );
        poseStack.popPose();

        super.render(projectile, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityDryIceProjectile projectile) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
