package com.moratan251.psitweaks.common.tile.machine;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import java.util.Objects;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData;

public class PsionicGeneratorBlockEntity extends TileEntityConfigurableMachine {
    private static final String NBT_LINK_ACTIVE = "linkActive";
    private static final String NBT_CONSUME_PSI = "consumePsiPerTick";
    private static final String NBT_LAST_OWNER = "lastOwnerUUID";
    private static final int DEFAULT_CONSUME_PSI = 25;
    private static final int MIN_CONSUME_PSI = 1;
    private static final int MAX_CONSUME_PSI = 100;
    private static final long JOULES_PER_PSI_NUMERATOR = 125;
    private static final long JOULES_PER_PSI_DENOMINATOR = 2;
    private static final long AUTO_EJECT_FE_PER_TICK = 6_400;
    private static final long AUTO_EJECT_RATE_JOULES = AUTO_EJECT_FE_PER_TICK * 5 / 2;
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
    private long currentGenerationRateJoules;

    public PsionicGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksMekanismBlocks.PSIONIC_GENERATOR, pos, state);
        configComponent.setupOutputConfig(TransmissionType.ENERGY, energyContainer, ENERGY_OUTPUT_SIDES)
                .addDisabledSides(RelativeSide.FRONT);
        ejectorComponent = new TileComponentEjector(this, PsionicGeneratorBlockEntity::getAutoEjectRateJoules)
                .setOutputData(configComponent, TransmissionType.ENERGY)
                .setCanEject(type -> canFunction());
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        energyContainer = BasicEnergyContainer.create(getConfiguredEnergyCapacity(), automationType -> true, automationType -> true, listener);
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
    protected boolean onUpdateServer() {
        boolean sendUpdate = super.onUpdateServer();
        energySlot.fillContainer();
        syncOwnerState();
        refreshOwnerPsiState();

        currentGenerationRateJoules = 0;
        if (!linkActive || !ownerOnline || !canFunction()) {
            setActive(false);
            return sendUpdate;
        }

        PlayerData data = getOwnerData();
        if (data == null) {
            setActive(false);
            return sendUpdate;
        }

        int consume = getConsumePsiPerTick();
        if (ownerAvailablePsi < consume || energyContainer.getNeeded() <= 0) {
            setActive(false);
            return sendUpdate;
        }

        long generation = Math.min(getGenerationRateJoules(consume), energyContainer.getNeeded());
        if (generation <= 0) {
            setActive(false);
            return sendUpdate;
        }

        data.deductPsi(consume, 0, true, false);
        energyContainer.insert(generation, Action.EXECUTE, AutomationType.INTERNAL);
        ownerAvailablePsi = Math.max(0, ownerAvailablePsi - consume);
        currentGenerationRateJoules = generation;
        setActive(true);
        return true;
    }

    @Override
    public void saveAdditional(CompoundTag dataMap, HolderLookup.Provider provider) {
        super.saveAdditional(dataMap, provider);
        dataMap.putBoolean(NBT_LINK_ACTIVE, linkActive);
        dataMap.putInt(NBT_CONSUME_PSI, consumePsiPerTick);
        if (lastOwnerUUID != null) {
            dataMap.putUUID(NBT_LAST_OWNER, lastOwnerUUID);
        }
    }

    @Override
    public void loadAdditional(CompoundTag dataMap, HolderLookup.Provider provider) {
        super.loadAdditional(dataMap, provider);
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
        container.track(SyncableLong.create(this::getCurrentGenerationRateJoules, value -> currentGenerationRateJoules = value));
    }

    public void applySettings(int consumePsiPerTick, boolean linkActive) {
        this.consumePsiPerTick = clampConsumePsi(consumePsiPerTick);
        this.linkActive = linkActive;
        setChanged();
    }

    public void ensureOwner(Player player) {
        if (player != null && getSecurity() != null && getSecurity().getOwnerUUID() == null) {
            getSecurity().setOwnerUUID(player.getUUID());
        }
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

    public long getCurrentGenerationRateJoules() {
        return currentGenerationRateJoules;
    }

    public long getConfiguredGenerationRateJoules() {
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

    public static int clampConsumePsi(int consumePsiPerTick) {
        return Math.max(MIN_CONSUME_PSI, Math.min(MAX_CONSUME_PSI, consumePsiPerTick));
    }

    private static long getGenerationRateJoules(int consumePsiPerTick) {
        return consumePsiPerTick * JOULES_PER_PSI_NUMERATOR / JOULES_PER_PSI_DENOMINATOR;
    }

    private static long getAutoEjectRateJoules() {
        return AUTO_EJECT_RATE_JOULES;
    }

    private static long getConfiguredEnergyCapacity() {
        return Math.max(1L, PsitweaksConfig.COMMON.psionicGeneratorEnergyCapacity.get());
    }
}
