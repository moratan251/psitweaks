package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import mekanism.api.Action;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData;

import java.util.Objects;
import java.util.UUID;

public class TileEntityPsionicGenerator extends TileEntityConfigurableMachine {

    private static final String NBT_LINK_ACTIVE = "linkActive";
    private static final String NBT_CONSUME_PSI = "consumePsiPerTick";
    private static final String NBT_LAST_OWNER = "lastOwnerUUID";
    private static final int DEFAULT_CONSUME_PSI = 25;
    private static final int MIN_CONSUME_PSI = 1;
    private static final int MAX_CONSUME_PSI = 100;
    private static final long JOULES_PER_PSI_NUMERATOR = 125;
    private static final long JOULES_PER_PSI_DENOMINATOR = 2;
    private static final long AUTO_EJECT_FE_PER_TICK = 6_400;
    private static final FloatingLong AUTO_EJECT_RATE_JOULES = FloatingLong.createConst(AUTO_EJECT_FE_PER_TICK)
            .multiply(5)
            .divide(2);
    private static final RelativeSide[] ENERGY_OUTPUT_SIDES = {
            RelativeSide.BACK,
            RelativeSide.LEFT,
            RelativeSide.RIGHT,
            RelativeSide.TOP,
            RelativeSide.BOTTOM
    };

    private BasicEnergyContainer energyContainer;
    private EnergyInventorySlot energySlot;

    private boolean linkActive;
    private int consumePsiPerTick = DEFAULT_CONSUME_PSI;
    private UUID lastOwnerUUID;

    private boolean ownerOnline;
    private int ownerAvailablePsi;
    private int ownerTotalPsi;
    private FloatingLong currentGenerationRateJoules = FloatingLong.ZERO;

    public TileEntityPsionicGenerator(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.PSIONIC_GENERATOR, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.ENERGY);
        configComponent.setupOutputConfig(TransmissionType.ENERGY, energyContainer, ENERGY_OUTPUT_SIDES)
                .addDisabledSides(RelativeSide.FRONT);
        ejectorComponent = new TileComponentEjector(this, TileEntityPsionicGenerator::getAutoEjectRateJoules)
                .setOutputData(configComponent, TransmissionType.ENERGY)
                .setCanEject(type -> MekanismUtils.canFunction(this));
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(
                this::getDirection,
                () -> configComponent
        );
        energyContainer = BasicEnergyContainer.create(getConfiguredEnergyCapacity(), BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, listener);
        builder.addContainer(energyContainer);
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        energySlot = EnergyInventorySlot.fill(getEnergyContainer(), listener, 143, 35);
        builder.addSlot(energySlot, RelativeSide.RIGHT);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainer();
        syncOwnerState();
        refreshOwnerPsiState();

        currentGenerationRateJoules = FloatingLong.ZERO;
        if (!linkActive || !ownerOnline || !MekanismUtils.canFunction(this)) {
            setActive(false);
            return;
        }

        PlayerData data = getOwnerData();
        if (data == null) {
            setActive(false);
            return;
        }

        int consume = getConsumePsiPerTick();
        if (ownerAvailablePsi < consume || energyContainer.getNeeded().isZero()) {
            setActive(false);
            return;
        }

        FloatingLong generation = getGenerationRateJoules(consume).min(energyContainer.getNeeded());
        if (generation.isZero()) {
            setActive(false);
            return;
        }

        data.deductPsi(consume, 0, true, false);
        energyContainer.insert(generation, Action.EXECUTE, AutomationType.INTERNAL);
        ownerAvailablePsi = Math.max(0, ownerAvailablePsi - consume);
        currentGenerationRateJoules = generation;
        setActive(true);
    }

    @Override
    protected void addGeneralPersistentData(CompoundTag dataMap) {
        super.addGeneralPersistentData(dataMap);
        dataMap.putBoolean(NBT_LINK_ACTIVE, linkActive);
        dataMap.putInt(NBT_CONSUME_PSI, consumePsiPerTick);
        if (lastOwnerUUID != null) {
            dataMap.putUUID(NBT_LAST_OWNER, lastOwnerUUID);
        }
    }

    @Override
    protected void loadGeneralPersistentData(CompoundTag dataMap) {
        super.loadGeneralPersistentData(dataMap);
        if (dataMap.contains(NBT_LINK_ACTIVE)) {
            linkActive = dataMap.getBoolean(NBT_LINK_ACTIVE);
        }
        if (dataMap.contains(NBT_CONSUME_PSI)) {
            consumePsiPerTick = clampConsumePsi(dataMap.getInt(NBT_CONSUME_PSI));
        }
        if (dataMap.hasUUID(NBT_LAST_OWNER)) {
            lastOwnerUUID = dataMap.getUUID(NBT_LAST_OWNER);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::isLinkActive, value -> linkActive = value));
        container.track(SyncableBoolean.create(this::isOwnerOnline, value -> ownerOnline = value));
        container.track(SyncableInt.create(this::getConsumePsiPerTick, value -> consumePsiPerTick = value));
        container.track(SyncableInt.create(this::getOwnerAvailablePsi, value -> ownerAvailablePsi = value));
        container.track(SyncableInt.create(this::getOwnerTotalPsi, value -> ownerTotalPsi = value));
        container.track(SyncableFloatingLong.create(this::getCurrentGenerationRateJoules, value -> currentGenerationRateJoules = value));
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(energyContainer.getEnergy(), energyContainer.getMaxEnergy());
    }

    @Override
    protected boolean makesComparatorDirty(SubstanceType type) {
        return type == SubstanceType.ENERGY;
    }

    public void applySettings(int consumePsiPerTick, boolean linkActive) {
        this.consumePsiPerTick = clampConsumePsi(consumePsiPerTick);
        this.linkActive = linkActive;
        setChanged();
    }

    public boolean isLinkActive() {
        return linkActive;
    }

    public boolean isOwnerOnline() {
        return ownerOnline;
    }

    public int getConsumePsiPerTick() {
        return clampConsumePsi(consumePsiPerTick);
    }

    public int getOwnerAvailablePsi() {
        return ownerAvailablePsi;
    }

    public int getOwnerTotalPsi() {
        return ownerTotalPsi;
    }

    public FloatingLong getCurrentGenerationRateJoules() {
        return currentGenerationRateJoules;
    }

    public FloatingLong getConfiguredGenerationRateJoules() {
        return getGenerationRateJoules(getConsumePsiPerTick());
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public String getOwnerDisplayName() {
        UUID ownerUUID = getOwnerUuid();
        if (ownerUUID == null) {
            return "Unowned";
        }
        String ownerName = getSecurity() == null ? null : getSecurity().getOwnerName();
        if (ownerName != null && !ownerName.isBlank()) {
            return ownerName;
        }
        return "Unknown";
    }

    public boolean isOwnedBy(Player player) {
        return player != null && Objects.equals(player.getUUID(), getOwnerUuid());
    }

    private void syncOwnerState() {
        UUID ownerUUID = getOwnerUuid();
        if (!Objects.equals(lastOwnerUUID, ownerUUID)) {
            lastOwnerUUID = ownerUUID;
            linkActive = false;
            consumePsiPerTick = DEFAULT_CONSUME_PSI;
            setChanged();
        }
    }

    private void refreshOwnerPsiState() {
        ServerPlayer owner = getOnlineOwner();
        ownerOnline = owner != null;
        if (!ownerOnline) {
            ownerAvailablePsi = 0;
            ownerTotalPsi = 0;
            return;
        }

        PlayerData data = PlayerDataHandler.get(owner);
        if (data == null) {
            ownerAvailablePsi = 0;
            ownerTotalPsi = 0;
            return;
        }

        ownerAvailablePsi = Math.max(0, data.getAvailablePsi());
        ownerTotalPsi = Math.max(0, data.getTotalPsi());
    }

    private PlayerData getOwnerData() {
        ServerPlayer owner = getOnlineOwner();
        return owner == null ? null : PlayerDataHandler.get(owner);
    }

    private ServerPlayer getOnlineOwner() {
        if (level == null || level.getServer() == null) {
            return null;
        }
        UUID ownerUUID = getOwnerUuid();
        return ownerUUID == null ? null : level.getServer().getPlayerList().getPlayer(ownerUUID);
    }

    private UUID getOwnerUuid() {
        return getSecurity() == null ? null : getSecurity().getOwnerUUID();
    }

    private static int clampConsumePsi(int consumePsiPerTick) {
        return Math.max(MIN_CONSUME_PSI, Math.min(MAX_CONSUME_PSI, consumePsiPerTick));
    }

    private static FloatingLong getGenerationRateJoules(int consumePsiPerTick) {
        return FloatingLong.createConst(consumePsiPerTick)
                .multiply(JOULES_PER_PSI_NUMERATOR)
                .divide(JOULES_PER_PSI_DENOMINATOR);
    }

    private static FloatingLong getAutoEjectRateJoules() {
        return AUTO_EJECT_RATE_JOULES;
    }

    private static FloatingLong getConfiguredEnergyCapacity() {
        return FloatingLong.createConst(Math.max(1L, PsitweaksConfig.COMMON.psionicGeneratorEnergyCapacity.get()));
    }
}
