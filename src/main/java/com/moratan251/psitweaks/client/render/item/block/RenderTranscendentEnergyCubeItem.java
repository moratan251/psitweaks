package com.moratan251.psitweaks.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.moratan251.psitweaks.common.tier.TranscendentEnergyCubeTier;
import com.moratan251.psitweaks.common.tile.machine.TileEntityTranscendentEnergyCube;
import mekanism.api.RelativeSide;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.MekanismISTER;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class RenderTranscendentEnergyCubeItem extends MekanismISTER {

    public static final RenderTranscendentEnergyCubeItem RENDERER = new RenderTranscendentEnergyCubeItem();

    private ModelEnergyCore core;

    private RenderTranscendentEnergyCubeItem() {
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        core = new ModelEnergyCore(getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ModelData modelData = ModelData.builder()
                .with(TileEntityEnergyCube.SIDE_STATE_PROPERTY, getSideStates(stack))
                .build();
        renderBlockItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay, modelData);

        double level = StorageUtils.getStoredEnergyFromNBT(stack).divideToLevel(TileEntityTranscendentEnergyCube.getStorage());
        if (level > 0 && core != null) {
            renderEnergyCore(poseStack, buffer, packedOverlay, level);
        }
    }

    private static CubeSideState[] getSideStates(ItemStack stack) {
        CubeSideState[] sideStates = new CubeSideState[EnumUtils.SIDES.length];
        CompoundTag dataMap = ItemDataUtils.getDataMapIfPresent(stack);
        if (dataMap != null && dataMap.contains("componentConfig", Tag.TAG_COMPOUND)) {
            CompoundTag energyConfig = dataMap.getCompound("componentConfig")
                    .getCompound("config" + TransmissionType.ENERGY.ordinal());
            for (RelativeSide side : EnumUtils.SIDES) {
                DataType dataType = DataType.byIndexStatic(energyConfig.getInt("side" + side.ordinal()));
                sideStates[side.ordinal()] = getSideState(dataType);
            }
        } else {
            for (RelativeSide side : EnumUtils.SIDES) {
                sideStates[side.ordinal()] = side == RelativeSide.FRONT ? CubeSideState.ACTIVE_LIT : CubeSideState.ACTIVE_UNLIT;
            }
        }
        return sideStates;
    }

    private static CubeSideState getSideState(DataType dataType) {
        if (dataType == DataType.NONE) {
            return CubeSideState.INACTIVE;
        }
        return dataType.canOutput() ? CubeSideState.ACTIVE_LIT : CubeSideState.ACTIVE_UNLIT;
    }

    private void renderEnergyCore(PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, double level) {
        float ticks = Minecraft.getInstance().levelRenderer.getTicks() + MekanismRenderer.getPartialTick();
        float angle = 4.0F * ticks;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(0D, Math.sin(Math.toRadians(3.0F * ticks)) / 7.0D, 0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.mulPose(RenderEnergyCube.coreVec.rotationDegrees(36.0F + angle));
        core.render(poseStack, buffer, 0xF000F0, packedOverlay, TranscendentEnergyCubeTier.TRANSCENDENT.getBaseTier(), (float) level);
        poseStack.popPose();
    }
}
