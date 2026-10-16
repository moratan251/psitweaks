package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.moratan251.psitweaks.client.combat.GlowDrawing;
import com.moratan251.psitweaks.client.combat.PsitweaksRenderTypes;
import com.moratan251.psitweaks.common.entities.combat.EntityDancingBlade;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Trick: Dancing Blades. A spectral psimetal sword pointing where it flies, wrapped in a glow and a short trail. */
public class DancingBladeRenderer extends CombatGlowRenderer<EntityDancingBlade> {
    private static final ResourceLocation PSIMETAL_SWORD = ResourceLocation.fromNamespaceAndPath("psi", "psimetal_sword");
    private static final float SPAWN_TICKS = 6.0F;

    private final ItemRenderer itemRenderer;
    private final ItemStack sword;

    public DancingBladeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.sword = BuiltInRegistries.ITEM.getOptional(PSIMETAL_SWORD).map(ItemStack::new)
                .orElseGet(() -> new ItemStack(Items.IRON_SWORD));
    }

    @Override
    public void render(@NotNull EntityDancingBlade entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int packedLight) {
        float age = entity.tickCount + partialTick;
        float appear = Mth.clamp(age / SPAWN_TICKS, 0.0F, 1.0F);
        Vec3 facing = updateFacing(entity);

        Vector3f origin = new Vector3f();
        Matrix4f pose = cameraPose(entity, partialTick, poseStack, origin);
        int color = GlowDrawing.glowColor(entity.getColor());
        byte state = entity.getState();
        float trail = state == EntityDancingBlade.STRIKE ? 2.0F : state == EntityDancingBlade.RETURN ? 1.2F : 0.7F;
        VertexConsumer glow = buffers.getBuffer(PsitweaksRenderTypes.ADDITIVE_COLOR);
        GlowDrawing.ribbon(glow, pose, origin, offset(origin, facing, -trail), 0.5F * appear, 0.0F, color,
                0.6F, 0.0F, 255);
        GlowDrawing.glowDisc(glow, pose, origin, 0.9F * appear, color, 0.45F, 255);

        // The sprite's tip points to its top-right corner: turn it to +Y, roll it slowly, then aim +Y along the flight.
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationTo(0.0F, 1.0F, 0.0F, (float) facing.x, (float) facing.y, (float) facing.z));
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getId() * 47.0F + age * 9.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
        float scale = 1.15F * appear;
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(sword, ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                poseStack, buffers, entity.level(), entity.getId());
        poseStack.popPose();
    }

    /** Turns smoothly towards the direction of travel; keeps the last direction while the blade barely moves. */
    private static Vec3 updateFacing(EntityDancingBlade entity) {
        Vec3 motion = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        if (motion.lengthSqr() > 1.0E-4) {
            Vec3 blended = entity.renderFacing.lerp(motion.normalize(), 0.35);
            if (blended.lengthSqr() > 1.0E-6) {
                entity.renderFacing = blended.normalize();
            }
        }
        return entity.renderFacing;
    }
}
