package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.tier.TranscendentEnergyCubeTier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class TileEntityTranscendentEnergyCube extends TileEntityConfigurableMachine {

    private static final String NBT_SCALE = "scale";

    private float prevScale;
    private RateLimitedEnergyContainer energyContainer;
    private EnergyInventorySlot chargeSlot;
    private EnergyInventorySlot dischargeSlot;

    public TileEntityTranscendentEnergyCube(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.ENERGY, TransmissionType.ITEM);
        configComponent.setupIOConfig(TransmissionType.ITEM, chargeSlot, dischargeSlot, RelativeSide.FRONT, true)
                .setCanEject(false);
        configComponent.setupIOConfig(TransmissionType.ENERGY, energyContainer, RelativeSide.FRONT)
                .setEjecting(true);
        ejectorComponent = new TileComponentEjector(this, TileEntityTranscendentEnergyCube::getOutput)
                .setOutputData(configComponent, TransmissionType.ENERGY)
                .setCanEject(type -> MekanismUtils.canFunction(this));
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(
                this::getDirection,
                () -> configComponent
        );
        energyContainer = new RateLimitedEnergyContainer(getStorage(), getOutput(), listener);
        builder.addContainer(energyContainer);
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(
                this::getDirection,
                () -> configComponent
        );
        dischargeSlot = EnergyInventorySlot.fillOrConvert(getEnergyContainer(), this::getLevel, listener, 17, 35);
        chargeSlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35);
        dischargeSlot.setSlotOverlay(SlotOverlay.MINUS);
        chargeSlot.setSlotOverlay(SlotOverlay.PLUS);
        builder.addSlot(dischargeSlot);
        builder.addSlot(chargeSlot);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        chargeSlot.drainContainer();
        dischargeSlot.fillContainerOrConvert();
        float scale = MekanismUtils.getScale(prevScale, energyContainer);
        if (scale != prevScale) {
            prevScale = scale;
            sendUpdatePacket();
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(energyContainer.getEnergy(), energyContainer.getMaxEnergy());
    }

    @Override
    protected boolean makesComparatorDirty(SubstanceType type) {
        return type == SubstanceType.ENERGY;
    }

    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.putFloat(NBT_SCALE, prevScale);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains(NBT_SCALE)) {
            prevScale = tag.getFloat(NBT_SCALE);
        }
        updateModelData();
    }

    @Override
    public ModelData getModelData() {
        ConfigInfo configInfo = getConfig().getConfig(TransmissionType.ENERGY);
        if (configInfo == null) {
            return super.getModelData();
        }
        TileEntityEnergyCube.CubeSideState[] sideStates = new TileEntityEnergyCube.CubeSideState[EnumUtils.SIDES.length];
        for (RelativeSide side : EnumUtils.SIDES) {
            TileEntityEnergyCube.CubeSideState state = TileEntityEnergyCube.CubeSideState.INACTIVE;
            ISlotInfo slotInfo = configInfo.getSlotInfo(side);
            if (slotInfo != null) {
                if (slotInfo.canOutput()) {
                    state = TileEntityEnergyCube.CubeSideState.ACTIVE_LIT;
                } else if (slotInfo.canInput()) {
                    state = TileEntityEnergyCube.CubeSideState.ACTIVE_UNLIT;
                }
            }
            sideStates[side.ordinal()] = state;
        }
        return ModelData.builder()
                .with(TileEntityEnergyCube.SIDE_STATE_PROPERTY, sideStates)
                .build();
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public float getEnergyScale() {
        return prevScale;
    }

    public static FloatingLong getStorage() {
        return TranscendentEnergyCubeTier.TRANSCENDENT.getMaxEnergy();
    }

    public static FloatingLong getOutput() {
        return TranscendentEnergyCubeTier.TRANSCENDENT.getOutput();
    }

    private static class RateLimitedEnergyContainer extends BasicEnergyContainer {

        private final FloatingLong rate;

        private RateLimitedEnergyContainer(FloatingLong maxEnergy, FloatingLong rate, IContentsListener listener) {
            super(maxEnergy, alwaysTrue, alwaysTrue, listener);
            this.rate = rate;
        }

        @Override
        protected FloatingLong getRate(AutomationType automationType) {
            return automationType == AutomationType.INTERNAL ? rate : super.getRate(automationType);
        }
    }
}
