package com.moratan251.psitweaks.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moratan251.psitweaks.common.tier.TranscendentEnergyCubeTier;
import mekanism.api.RelativeSide;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.MekanismISTER;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.IPersistentConfigInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

public class RenderTranscendentEnergyCubeItem extends MekanismISTER {
    public static final RenderTranscendentEnergyCubeItem RENDERER = new RenderTranscendentEnergyCubeItem();

    private ModelEnergyCore core;

    private RenderTranscendentEnergyCubeItem() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        core = new ModelEnergyCore(getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, int packedOverlay) {
        ModelData modelData = ModelData.of(TileEntityEnergyCube.SIDE_STATE_PROPERTY, getSideStates(stack));
        renderBlockItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay, modelData);

        double energyRatio = StorageUtils.getEnergyRatio(stack);
        if (energyRatio <= 0 || core == null) {
            return;
        }

        float tick = Minecraft.getInstance().levelRenderer.getTicks() + MekanismRenderer.getPartialTick();
        float rotation = 4.0F * tick;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(0, Math.sin(Math.toRadians(3.0F * tick)) / 7.0D, 0);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(RenderEnergyCube.coreVec.rotationDegrees(36.0F + rotation));
        core.render(poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay,
                TranscendentEnergyCubeTier.TRANSCENDENT.getBaseTier(), (float) energyRatio);
        poseStack.popPose();
    }

    private static CubeSideState[] getSideStates(ItemStack stack) {
        IPersistentConfigInfo energyConfig = AttachedSideConfig.getStoredConfigInfo(
                stack, ItemBlockEnergyCube.SIDE_CONFIG, TransmissionType.ENERGY);
        CubeSideState[] sideStates = new CubeSideState[EnumUtils.SIDES.length];
        for (RelativeSide side : EnumUtils.SIDES) {
            sideStates[side.ordinal()] = getSideState(energyConfig.getDataType(side));
        }
        return sideStates;
    }

    private static CubeSideState getSideState(DataType dataType) {
        if (dataType == DataType.NONE) {
            return CubeSideState.INACTIVE;
        }
        return dataType.canOutput() ? CubeSideState.ACTIVE_LIT : CubeSideState.ACTIVE_UNLIT;
    }
}
