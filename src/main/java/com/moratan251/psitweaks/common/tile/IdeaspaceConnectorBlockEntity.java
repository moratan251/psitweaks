package com.moratan251.psitweaks.common.tile;

import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import com.moratan251.psitweaks.common.storage.connector.ConnectorHandlers;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import com.moratan251.psitweaks.common.storage.connector.ConnectorTransfers;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Holds only owner/settings. Every resource remains in its owner's SavedData. */
public class IdeaspaceConnectorBlockEntity extends ConjuredPulsarBlockEntity implements MenuProvider {
    public static final int SLOTS = 9;
    private UUID owner;
    private final ConnectorResource[] resources = new ConnectorResource[SLOTS];
    private final ConnectorSideMode[] sides = new ConnectorSideMode[6];
    private final boolean[] automatic = new boolean[6];
    private final ConnectorHandlers[] handlers = new ConnectorHandlers[6];
    private long settingsVersion;
    private int nextSide;

    public IdeaspaceConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksBlockEntityTypes.IDEASPACE_CONNECTOR.get(), pos, state);
        Arrays.fill(resources, ConnectorResource.EMPTY);
        Arrays.fill(sides, ConnectorSideMode.BOTH);
        for (Direction side : Direction.values()) handlers[side.ordinal()] = new ConnectorHandlers(this, side);
    }

    public void initialize(UUID owner, ItemStack colorizer) {
        if (this.owner != null) return;
        this.owner = owner;
        setColorizer(colorizer);
        setChanged();
    }

    @Nullable
    public UUID owner() { return owner; }

    @Nullable
    public PlayerIdeaStorage storage() {
        if (owner == null || isRemoved() || !(level instanceof ServerLevel serverLevel)
                || !level.hasChunkAt(worldPosition) || level.getBlockEntity(worldPosition) != this) return null;
        return IdeaStorageService.get(serverLevel.getServer(), owner);
    }

    public boolean canConfigure(Player player) {
        return owner != null && owner.equals(player.getUUID()) && !isRemoved()
                && player.level() == level && level.hasChunkAt(worldPosition)
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64;
    }

    public ConnectorResource resource(int slot) {
        return slot >= 0 && slot < SLOTS ? resources[slot] : ConnectorResource.EMPTY;
    }

    public boolean setResource(int slot, ConnectorResource resource) {
        if (slot < 0 || slot >= SLOTS || resource == null) return false;
        // Duplicate virtual slots would make storage buses count the same inventory twice.
        if (resource != ConnectorResource.EMPTY) {
            for (int i = 0; i < SLOTS; i++) {
                if (i != slot && resources[i].equals(resource)) resources[i] = ConnectorResource.EMPTY;
            }
        }
        resources[slot] = resource;
        settingsChanged();
        return true;
    }

    public ConnectorSideMode sideMode(@Nullable Direction side) {
        return side == null ? ConnectorSideMode.DISABLED : sides[side.ordinal()];
    }

    public void setSideMode(Direction side, ConnectorSideMode mode) {
        sides[side.ordinal()] = mode;
        settingsChanged();
    }

    public boolean automatic(Direction side) { return automatic[side.ordinal()]; }

    public void setAutomatic(Direction side, boolean enabled) {
        automatic[side.ordinal()] = enabled;
        settingsChanged();
    }

    public ConnectorHandlers handlers(Direction side) { return handlers[side.ordinal()]; }
    public long settingsVersion() { return settingsVersion; }

    public boolean publishesEnergy() {
        for (ConnectorResource resource : resources) if (resource.kind() == ConnectorResource.Kind.ENERGY) return true;
        return false;
    }

    private boolean hasAutomaticOutput() {
        for (Direction side : Direction.values()) if (automatic(side) && sideMode(side).output) return true;
        return false;
    }

    private void settingsChanged() {
        settingsVersion++;
        setChanged();
        if (level instanceof ServerLevel) {
            level.invalidateCapabilities(worldPosition);
            scheduleTransfer(1);
        }
    }

    private void scheduleTransfer(int delay) {
        if (level instanceof ServerLevel && hasAutomaticOutput())
            level.scheduleTick(worldPosition, getBlockState().getBlock(), delay);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        scheduleTransfer(5);
    }

    public void autoTransfer() {
        PlayerIdeaStorage storage = storage();
        if (storage == null || !hasAutomaticOutput()) return;
        boolean moved = false;
        for (int i = 0; i < 6; i++) {
            Direction side = Direction.values()[(nextSide + i) % 6];
            if (automatic(side) && sideMode(side).output) moved |= ConnectorTransfers.push(this, storage, side);
        }
        nextSide = (nextSide + 1) % 6;
        // No ticker when disabled; back off when empty, disconnected, or blocked.
        scheduleTransfer(moved ? 5 : 20);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) tag.putUUID("Owner", owner);
        ListTag selected = new ListTag();
        for (ConnectorResource resource : resources) selected.add(resource.save(registries));
        tag.put("Published", selected);
        int[] modes = new int[6];
        int autoMask = 0;
        for (int i = 0; i < 6; i++) {
            modes[i] = sides[i].ordinal();
            if (automatic[i]) autoMask |= 1 << i;
        }
        tag.putIntArray("Sides", modes);
        tag.putInt("Automatic", autoMask);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ListTag selected = tag.getList("Published", Tag.TAG_COMPOUND);
        Arrays.fill(resources, ConnectorResource.EMPTY);
        for (int i = 0; i < Math.min(SLOTS, selected.size()); i++) {
            ConnectorResource resource = ConnectorResource.load(selected.getCompound(i), registries);
            boolean duplicate = false;
            for (int j = 0; j < i; j++) duplicate |= resources[j].equals(resource);
            if (!duplicate) resources[i] = resource;
        }
        int[] modes = tag.getIntArray("Sides");
        for (int i = 0; i < 6; i++) {
            sides[i] = i < modes.length ? ConnectorSideMode.byId(modes[i]) : ConnectorSideMode.BOTH;
            automatic[i] = (tag.getInt("Automatic") & (1 << i)) != 0;
        }
        settingsVersion++;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Chunk watchers only need the particle color. Settings go only to the owner's open menu.
        CompoundTag tag = new CompoundTag();
        if (!getColorizer().isEmpty()) tag.put("colorizer", getColorizer().save(registries));
        return tag;
    }

    @Override
    public Component getDisplayName() { return Component.translatable("block.psitweaks.ideaspace_connector"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return canConfigure(player) ? new IdeaspaceConnectorMenu(id, inventory, this) : null;
    }
}
