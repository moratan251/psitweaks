package com.moratan251.psitweaks.common.tile;

import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import com.moratan251.psitweaks.common.storage.connector.ConnectorHandlers;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import com.moratan251.psitweaks.common.storage.connector.ConnectorTransfers;
import com.moratan251.psitweaks.common.storage.connector.ConnectorExportSettings;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    private final java.util.Map<Direction, java.util.Map<net.minecraftforge.common.capabilities.Capability<?>, net.minecraftforge.common.util.LazyOptional<?>>> capabilities = new java.util.EnumMap<>(Direction.class);
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (side == null || isRemoved()) return super.getCapability(cap, side);
        var values = capabilities.computeIfAbsent(side, face -> {
            var map = new java.util.HashMap<net.minecraftforge.common.capabilities.Capability<?>, net.minecraftforge.common.util.LazyOptional<?>>();
            var handler = handlers(face);
            map.put(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, net.minecraftforge.common.util.LazyOptional.of(() -> handler.items));
            map.put(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, net.minecraftforge.common.util.LazyOptional.of(() -> handler.fluids));
            map.put(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY, net.minecraftforge.common.util.LazyOptional.of(() -> handler.energy));
            if (com.moratan251.psitweaks.common.compat.MekanismCompat.isMekanismLoaded())
                com.moratan251.psitweaks.common.compat.ConnectorMekanism.addCapabilities(this, face, map);
            return map;
        });
        var value = values.get(cap);
        return value == null ? super.getCapability(cap, side) : value.cast();
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps();
        if (capabilities != null) {
            capabilities.values().forEach(map -> map.values().forEach(net.minecraftforge.common.util.LazyOptional::invalidate));
            capabilities.clear();
        }
    }
    public static final int SLOTS = 9;
    private UUID owner;
    private final ConnectorResource[] resources = new ConnectorResource[SLOTS];
    private final ConnectorSideMode[] sides = new ConnectorSideMode[6];
    private final boolean[] automatic = new boolean[6];
    private final boolean[] slotOverrides = new boolean[SLOTS];
    private final ConnectorSideMode[][] slotSides = new ConnectorSideMode[SLOTS][6];
    private final boolean[][] slotAutomatic = new boolean[SLOTS][6];
    private final ConnectorHandlers[] handlers = new ConnectorHandlers[6];
    private long settingsVersion;
    private int nextSide;
    private final ConnectorExportSettings[] commonExport = new ConnectorExportSettings[ConnectorExportSettings.TYPES];
    private final ConnectorExportSettings[][] slotExport = new ConnectorExportSettings[SLOTS][ConnectorExportSettings.TYPES];
    private final long[] nextExportTicks = new long[SLOTS];
    private int[] savedExportCooldowns;
    private long nextExportTick = Long.MAX_VALUE;

    public IdeaspaceConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(PsitweaksBlockEntityTypes.IDEASPACE_CONNECTOR.get(), pos, state);
        Arrays.fill(resources, ConnectorResource.EMPTY);
        Arrays.fill(sides, ConnectorSideMode.BOTH);
        for (var modes : slotSides) Arrays.fill(modes, ConnectorSideMode.BOTH);
        for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
            commonExport[type] = ConnectorExportSettings.defaults(type);
            for (var values : slotExport) values[type] = commonExport[type];
        }
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

    public boolean usesCommonSettings(int slot) {
        return slot < 0 || slot >= SLOTS || !slotOverrides[slot];
    }

    public void setUsesCommonSettings(int slot, boolean common) {
        if (slot < 0 || slot >= SLOTS || common == usesCommonSettings(slot)) return;
        if (!common) {
            System.arraycopy(sides, 0, slotSides[slot], 0, 6);
            System.arraycopy(automatic, 0, slotAutomatic[slot], 0, 6);
            System.arraycopy(commonExport, 0, slotExport[slot], 0, ConnectorExportSettings.TYPES);
        }
        slotOverrides[slot] = !common;
        resetExportInterval(slot);
        settingsChanged();
    }

    public ConnectorExportSettings exportSettings(int slot, int type) {
        return usesCommonSettings(slot) ? commonExport[type] : slotExport[slot][type];
    }

    public boolean setExportSettings(int slot, ConnectorExportSettings[] settings) {
        if (slot < -1 || slot >= SLOTS || (slot >= 0 && usesCommonSettings(slot))
                || settings == null || settings.length != ConnectorExportSettings.TYPES) return false;
        for (int type = 0; type < settings.length; type++)
            if (settings[type] == null || !settings[type].valid(type)) return false;
        System.arraycopy(settings, 0, slot < 0 ? commonExport : slotExport[slot], 0, settings.length);
        for (int i = 0; i < SLOTS; i++) if (i == slot || (slot < 0 && usesCommonSettings(i))) resetExportInterval(i);
        settingsChanged();
        return true;
    }

    private void resetExportInterval(int slot) {
        if (level == null) return;
        restoreExportCooldowns();
        int type = ConnectorExportSettings.type(resource(slot).kind());
        nextExportTicks[slot] = level.getGameTime() + (type < 0 ? 1 : exportSettings(slot, type).interval());
    }

    public ConnectorSideMode sideMode(int slot, @Nullable Direction side) {
        if (slot < 0 || slot >= SLOTS || side == null) return ConnectorSideMode.DISABLED;
        return usesCommonSettings(slot) ? sideMode(side) : slotSides[slot][side.ordinal()];
    }

    public void setSideMode(int slot, Direction side, ConnectorSideMode mode) {
        if (usesCommonSettings(slot)) return;
        slotSides[slot][side.ordinal()] = mode;
        settingsChanged();
    }

    public boolean automatic(int slot, Direction side) {
        if (slot < 0 || slot >= SLOTS) return false;
        return usesCommonSettings(slot) ? automatic(side) : slotAutomatic[slot][side.ordinal()];
    }

    public void setAutomatic(int slot, Direction side, boolean enabled) {
        if (usesCommonSettings(slot)) return;
        slotAutomatic[slot][side.ordinal()] = enabled;
        settingsChanged();
    }

    public int publishedSlot(ConnectorResource resource) {
        if (resource.kind() != ConnectorResource.Kind.EMPTY)
            for (int slot = 0; slot < SLOTS; slot++) if (resources[slot].equals(resource)) return slot;
        return -1;
    }

    /** Match the incoming identity, not an arbitrary insertion slot supplied by a pipe. */
    public boolean allowsInput(Direction side, ConnectorResource resource) {
        int slot = publishedSlot(resource);
        return (slot < 0 ? sideMode(side) : sideMode(slot, side)).input;
    }

    public ConnectorHandlers handlers(Direction side) { return handlers[side.ordinal()]; }
    public long settingsVersion() { return settingsVersion; }

    public boolean publishesEnergy() {
        for (ConnectorResource resource : resources) if (resource.kind() == ConnectorResource.Kind.ENERGY) return true;
        return false;
    }

    private boolean hasAutomaticOutput() {
        for (Direction side : Direction.values()) if (hasAutomaticOutput(side)) return true;
        return false;
    }

    public boolean hasAutomaticOutput(Direction side) {
        for (int slot = 0; slot < SLOTS; slot++)
            if (resources[slot].kind() != ConnectorResource.Kind.EMPTY && automatic(slot, side) && sideMode(slot, side).output)
                return true;
        return false;
    }

    private void settingsChanged() {
        settingsVersion++;
        setChanged();
        if (level instanceof ServerLevel) {
            invalidateCaps();
            reviveCaps();
            refreshExportSchedule();
        }
    }

    private boolean slotExports(int slot) {
        if (resource(slot).kind() == ConnectorResource.Kind.EMPTY) return false;
        for (Direction side : Direction.values()) if (automatic(slot, side) && sideMode(slot, side).output) return true;
        return false;
    }

    private void restoreExportCooldowns() {
        if (savedExportCooldowns == null || level == null) return;
        for (int slot = 0; slot < SLOTS; slot++) nextExportTicks[slot] = level.getGameTime() + savedExportCooldowns[slot];
        savedExportCooldowns = null;
    }

    private void refreshExportSchedule() {
        nextExportTick = Long.MAX_VALUE;
        if (!(level instanceof ServerLevel)) return;
        restoreExportCooldowns();
        for (int slot = 0; slot < SLOTS; slot++) if (slotExports(slot))
            nextExportTick = Math.min(nextExportTick, Math.max(level.getGameTime() + 1, nextExportTicks[slot]));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshExportSchedule();
    }

    public void autoTransfer() {
        // Idle ticks only compare a timestamp. No storage/capability lookup until a slot is due.
        if (level == null || level.getGameTime() < nextExportTick) return;
        long now = level.getGameTime();
        PlayerIdeaStorage storage = storage();
        if (storage == null) { nextExportTick = now + 20; return; }
        int due = 0;
        for (int slot = 0; slot < SLOTS; slot++) if (slotExports(slot) && now >= nextExportTicks[slot]) due |= 1 << slot;
        for (int i = 0; i < 6; i++) {
            Direction side = Direction.values()[(nextSide + i) % 6];
            if (hasAutomaticOutput(side)) ConnectorTransfers.push(this, storage, side, due);
        }
        nextSide = (nextSide + 1) % 6;
        for (int slot = 0; slot < SLOTS; slot++) if ((due & (1 << slot)) != 0)
            nextExportTicks[slot] = now + exportSettings(slot, ConnectorExportSettings.type(resource(slot).kind())).interval();
        if (due != 0) setChanged();
        refreshExportSchedule();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        ListTag selected = new ListTag();
        for (ConnectorResource resource : resources) selected.add(resource.save());
        tag.put("Published", selected);
        writeConnectionSettings(tag);
        int[] cooldowns = new int[SLOTS];
        long now = level == null ? 0 : level.getGameTime();
        for (int slot = 0; slot < SLOTS; slot++) cooldowns[slot] = savedExportCooldowns != null ? savedExportCooldowns[slot]
                : (int) Math.max(0, Math.min(nextExportTicks[slot] - now, ConnectorExportSettings.MAX_INTERVAL));
        tag.putIntArray("ExportCooldowns", cooldowns);
    }

    /** Shared by persistence and the owner's menu sync; contains settings only. */
    public void writeConnectionSettings(CompoundTag tag) {
        int[] modes = new int[6];
        int autoMask = 0;
        for (int i = 0; i < 6; i++) {
            modes[i] = sides[i].ordinal();
            if (automatic[i]) autoMask |= 1 << i;
        }
        tag.putIntArray("Sides", modes);
        tag.putInt("Automatic", autoMask);
        int overrides = 0;
        int[] perSlotModes = new int[SLOTS * 6], perSlotAutomatic = new int[SLOTS];
        for (int slot = 0; slot < SLOTS; slot++) {
            if (slotOverrides[slot]) overrides |= 1 << slot;
            for (int face = 0; face < 6; face++) {
                perSlotModes[slot * 6 + face] = slotSides[slot][face].ordinal();
                if (slotAutomatic[slot][face]) perSlotAutomatic[slot] |= 1 << face;
            }
        }
        tag.putInt("SlotOverrides", overrides);
        tag.putIntArray("SlotSides", perSlotModes);
        tag.putIntArray("SlotAutomatic", perSlotAutomatic);
        int[] amounts = new int[ConnectorExportSettings.TYPES], intervals = new int[ConnectorExportSettings.TYPES];
        int[] slotAmounts = new int[SLOTS * ConnectorExportSettings.TYPES], slotIntervals = new int[slotAmounts.length];
        for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
            amounts[type] = commonExport[type].amount();
            intervals[type] = commonExport[type].interval();
            for (int slot = 0; slot < SLOTS; slot++) {
                slotAmounts[slot * ConnectorExportSettings.TYPES + type] = slotExport[slot][type].amount();
                slotIntervals[slot * ConnectorExportSettings.TYPES + type] = slotExport[slot][type].interval();
            }
        }
        tag.putIntArray("ExportAmounts", amounts);
        tag.putIntArray("ExportIntervals", intervals);
        tag.putIntArray("SlotExportAmounts", slotAmounts);
        tag.putIntArray("SlotExportIntervals", slotIntervals);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ListTag selected = tag.getList("Published", Tag.TAG_COMPOUND);
        Arrays.fill(resources, ConnectorResource.EMPTY);
        for (int i = 0; i < Math.min(SLOTS, selected.size()); i++) {
            ConnectorResource resource = ConnectorResource.load(selected.getCompound(i));
            boolean duplicate = false;
            for (int j = 0; j < i; j++) duplicate |= resources[j].equals(resource);
            if (!duplicate) resources[i] = resource;
        }
        int[] modes = tag.getIntArray("Sides");
        for (int i = 0; i < 6; i++) {
            sides[i] = i < modes.length ? ConnectorSideMode.byId(modes[i]) : ConnectorSideMode.BOTH;
            automatic[i] = (tag.getInt("Automatic") & (1 << i)) != 0;
        }
        int[] perSlotModes = tag.getIntArray("SlotSides"), perSlotAutomatic = tag.getIntArray("SlotAutomatic");
        for (int slot = 0; slot < SLOTS; slot++) {
            // Old saves have no override mask, so all nine slots keep using their existing common settings.
            slotOverrides[slot] = (tag.getInt("SlotOverrides") & (1 << slot)) != 0;
            for (int face = 0; face < 6; face++) {
                int index = slot * 6 + face;
                slotSides[slot][face] = index < perSlotModes.length ? ConnectorSideMode.byId(perSlotModes[index]) : sides[face];
                slotAutomatic[slot][face] = slot < perSlotAutomatic.length ? (perSlotAutomatic[slot] & (1 << face)) != 0 : automatic[face];
            }
        }
        settingsVersion++;
        int[] amounts = tag.getIntArray("ExportAmounts"), intervals = tag.getIntArray("ExportIntervals");
        int[] slotAmounts = tag.getIntArray("SlotExportAmounts"), slotIntervals = tag.getIntArray("SlotExportIntervals");
        for (int type = 0; type < ConnectorExportSettings.TYPES; type++) {
            commonExport[type] = ConnectorExportSettings.read(type, amounts, intervals, type);
            for (int slot = 0; slot < SLOTS; slot++)
                slotExport[slot][type] = ConnectorExportSettings.read(type, slotAmounts, slotIntervals, slot * ConnectorExportSettings.TYPES + type);
        }
        int[] cooldowns = tag.getIntArray("ExportCooldowns");
        savedExportCooldowns = new int[SLOTS];
        for (int slot = 0; slot < SLOTS; slot++) savedExportCooldowns[slot] = slot < cooldowns.length
                ? Math.max(0, Math.min(cooldowns[slot], ConnectorExportSettings.MAX_INTERVAL)) : 0;
        refreshExportSchedule();
    }

    @Override
    public CompoundTag getUpdateTag() {
        // Chunk watchers only need the particle color. Settings go only to the owner's open menu.
        CompoundTag tag = new CompoundTag();
        if (!getColorizer().isEmpty()) tag.put("colorizer", getColorizer().save(new CompoundTag()));
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
