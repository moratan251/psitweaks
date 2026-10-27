package com.moratan251.psitweaks.client.renderer.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the Trick: Parade lookalike as its owner: the owner's skin (slim or wide arms), armour, held items and name.
 * An autonomous caster's lookalike wears the skin of whoever placed the machine.
 * The owner sees it as a faint ghost without gear or name, so they can tell it from a real player; it wavers the same
 * way for everyone when it takes a hit.
 */
public class ParadeDecoyRenderer extends LivingEntityRenderer<EntityParadeDecoy, PlayerModel<EntityParadeDecoy>> {
    private final PlayerModel<EntityParadeDecoy> wideModel;
    private final PlayerModel<EntityParadeDecoy> slimModel;

    public ParadeDecoyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        wideModel = model;
        slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        addLayer(new ShownWhenSolid(this, new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager())));
        addLayer(new ShownWhenSolid(this, new ItemInHandLayer<>(this, context.getItemInHandRenderer())));
        addLayer(new ShownWhenSolid(this, new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer())));
    }

    @Override
    public void render(EntityParadeDecoy entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        model = skin(entity).model() == PlayerSkin.Model.SLIM ? slimModel : wideModel;
        boolean mainRight = entity.getMainArm() == HumanoidArm.RIGHT;
        HumanoidModel.ArmPose main = entity.getMainHandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        HumanoidModel.ArmPose off = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        model.rightArmPose = mainRight ? main : off;
        model.leftArmPose = mainRight ? off : main;
        model.crouching = entity.isCrouching();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    /** Crouching lowers the body like a player's. */
    @Override
    public Vec3 getRenderOffset(EntityParadeDecoy entity, float partialTick) {
        return entity.isCrouching() ? new Vec3(0.0, entity.getScale() * -2.0F / 16.0, 0.0) : super.getRenderOffset(entity, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityParadeDecoy entity) {
        return skin(entity).texture();
    }

    /** Hidden bodies are drawn see-through by the base renderer: the owner's ghost view and the waver after a hit. */
    @Override
    protected boolean isBodyVisible(EntityParadeDecoy entity) {
        return !isGhost(entity);
    }

    @Override
    protected boolean shouldShowName(EntityParadeDecoy entity) {
        return !isOwnerView(entity) && super.shouldShowName(entity);
    }

    @Override
    protected void scale(EntityParadeDecoy entity, PoseStack poseStack, float partialTick) {
        // Players are drawn at 15/16 scale.
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    private static boolean isGhost(EntityParadeDecoy entity) {
        return isOwnerView(entity) || entity.getWaver(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)) > 0.0F;
    }

    /** The owner, and whoever placed the autonomous caster that conjured it: it is their own lookalike. */
    private static boolean isOwnerView(EntityParadeDecoy entity) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && (entity.isOwnedBy(minecraft.player) || minecraft.player.getUUID().equals(entity.getLookUUID()));
    }

    private static PlayerSkin skin(EntityParadeDecoy entity) {
        UUID look = entity.getLookUUID();
        if (look == null) {
            return DefaultPlayerSkin.get(entity.getUUID());
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection != null ? connection.getPlayerInfo(look) : null;
        return info != null ? info.getSkin() : DefaultPlayerSkin.get(look);
    }

    /** A layer drawn only while the lookalike looks solid: no floating armour on the see-through ghost. */
    private static final class ShownWhenSolid extends RenderLayer<EntityParadeDecoy, PlayerModel<EntityParadeDecoy>> {
        private final RenderLayer<EntityParadeDecoy, PlayerModel<EntityParadeDecoy>> layer;

        ShownWhenSolid(RenderLayerParent<EntityParadeDecoy, PlayerModel<EntityParadeDecoy>> parent,
                       RenderLayer<EntityParadeDecoy, PlayerModel<EntityParadeDecoy>> layer) {
            super(parent);
            this.layer = layer;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, EntityParadeDecoy entity,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                           float headPitch) {
            if (!isGhost(entity)) {
                layer.render(poseStack, buffers, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks,
                        netHeadYaw, headPitch);
            }
        }
    }
}
