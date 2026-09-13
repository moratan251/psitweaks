package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.network.MessageConnectorState;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Configuration-only menu; the client sends indices, never resource identities or owner UUIDs. */
public class IdeaspaceConnectorMenu extends AbstractContainerMenu {
    public static final int PAGE_SIZE = 27;
    public static final int ASSIGN = 0, CLEAR = 1, SIDE = 2, AUTO = 3, PAGE = 4;
    private final Player player;
    private final IdeaspaceConnectorBlockEntity connector;
    private final BlockPos pos;
    private UUID session = UUID.randomUUID();
    private long revision;
    private long templateRevision;
    private int page;
    private long syncedStorage = -1, syncedSettings = -1, lastSyncTick = -20;
    private List<ConnectorResource> shown = List.of();
    private CompoundTag clientState = new CompoundTag();

    public IdeaspaceConnectorMenu(int id, Inventory inventory, IdeaspaceConnectorBlockEntity connector) {
        this(id, inventory, connector.getBlockPos(), connector);
    }

    private IdeaspaceConnectorMenu(int id, Inventory inventory, BlockPos pos, IdeaspaceConnectorBlockEntity connector) {
        super(ModMenuTypes.IDEASPACE_CONNECTOR.get(), id);
        this.player = inventory.player;
        this.pos = pos;
        this.connector = connector;
    }

    public static IdeaspaceConnectorMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new IdeaspaceConnectorMenu(id, inventory, buf.readBlockPos(), null);
    }

    @Override public boolean stillValid(Player player) {
        return connector == null ? player.level().getBlockEntity(pos) instanceof IdeaspaceConnectorBlockEntity
                : connector.canConfigure(player);
    }

    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    public UUID session() { return session; }
    public long revision() { return revision; }
    public CompoundTag clientState() { return clientState; }

    public void applyState(MessageConnectorState message) {
        if (!player.level().isClientSide) return;
        if (!message.data().getBoolean("Full")) {
            if (!session.equals(message.session()) || revision != message.revision()) return;
            CompoundTag merged = mergeAmounts(clientState, message.data());
            if (merged != null) clientState = merged;
            return;
        }
        session = message.session();
        revision = message.revision();
        clientState = message.data();
    }

    /** Amount-only updates retain resource components without resending them every few ticks. */
    public static CompoundTag mergeAmounts(CompoundTag previous, CompoundTag update) {
        CompoundTag merged = update.copy();
        for (String key : new String[] {"Selected", "Available"}) {
            ListTag templates = previous.getList(key, Tag.TAG_COMPOUND);
            ListTag amounts = update.getList(key, Tag.TAG_COMPOUND);
            if (templates.size() != amounts.size()) return null;
            ListTag entries = new ListTag();
            for (int i = 0; i < templates.size(); i++) {
                CompoundTag entry = templates.getCompound(i).copy();
                entry.putLong("Amount", amounts.getCompound(i).getLong("Amount"));
                entries.add(entry);
            }
            merged.put(key, entries);
        }
        return merged;
    }

    public void handleAction(Player sender, MessageConnectorAction action) {
        if (sender != player || connector == null || !stillValid(sender) || !session.equals(action.session())) return;
        int argument = action.argument();
        switch (action.action()) {
            case ASSIGN -> {
                if (action.revision() != revision || argument < 0 || argument >= shown.size()) return;
                connector.setResource(action.slot(), shown.get(argument));
            }
            case CLEAR -> connector.setResource(action.slot(), ConnectorResource.EMPTY);
            case SIDE -> {
                if (argument < 0 || argument >= 6) return;
                Direction side = Direction.values()[argument];
                connector.setSideMode(side, connector.sideMode(side).next());
            }
            case AUTO -> {
                if (argument < 0 || argument >= 6) return;
                Direction side = Direction.values()[argument];
                connector.setAutomatic(side, !connector.automatic(side));
            }
            case PAGE -> {
                if (argument != -1 && argument != 1) return;
                page = Math.max(0, page + argument);
            }
            default -> { return; }
        }
        syncedSettings = -1;
        broadcastChanges();
    }

    @Override public void broadcastChanges() {
        super.broadcastChanges();
        if (!(player instanceof ServerPlayer serverPlayer) || connector == null || !stillValid(player)) return;
        PlayerIdeaStorage storage = connector.storage();
        if (storage == null) return;
        long now = player.level().getGameTime();
        boolean settingsChanged = syncedSettings != connector.settingsVersion();
        if (!settingsChanged && (syncedStorage == storage.getVersion() || now - lastSyncTick < 5)) return;
        List<ConnectorResource> catalog = new ArrayList<>();
        if (!storage.isLoadFailed()) {
            storage.itemEntries().forEach(entry -> catalog.add(ConnectorResource.item(entry.getKey())));
            storage.fluidEntries().forEach(entry -> catalog.add(ConnectorResource.fluid(entry.getKey())));
            if (MekanismCompat.isMekanismLoaded())
                storage.chemicalEntries().forEach(entry -> catalog.add(ConnectorResource.chemical(entry.getKey())));
            catalog.add(ConnectorResource.ENERGY); // FE can be configured even with a zero balance.
        }
        int pages = Math.max(1, (catalog.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.min(page, pages - 1);
        List<ConnectorResource> next = List.copyOf(catalog.subList(Math.min(page * PAGE_SIZE, catalog.size()),
                Math.min((page + 1) * PAGE_SIZE, catalog.size())));
        boolean full = settingsChanged || !next.equals(shown);
        if (!next.equals(shown)) {
            shown = next;
            revision++;
        }
        CompoundTag data = new CompoundTag();
        if (full) templateRevision++;
        data.putLong("Templates", templateRevision);
        data.putBoolean("Full", full);
        ListTag selected = new ListTag();
        for (int i = 0; i < IdeaspaceConnectorBlockEntity.SLOTS; i++) selected.add(entry(connector.resource(i), storage, full));
        ListTag available = new ListTag();
        for (ConnectorResource resource : shown) available.add(entry(resource, storage, full));
        data.put("Selected", selected);
        data.put("Available", available);
        data.putInt("Page", page);
        data.putInt("Pages", pages);
        data.putBoolean("LoadFailed", storage.isLoadFailed());
        int[] sides = new int[6];
        int automatic = 0;
        for (Direction direction : Direction.values()) {
            sides[direction.ordinal()] = connector.sideMode(direction).ordinal();
            if (connector.automatic(direction)) automatic |= 1 << direction.ordinal();
        }
        data.putIntArray("Sides", sides);
        data.putInt("Automatic", automatic);
        PacketDistributor.sendToPlayer(serverPlayer, new MessageConnectorState(containerId, session, revision, data));
        syncedStorage = storage.getVersion();
        syncedSettings = connector.settingsVersion();
        lastSyncTick = now;
    }

    private CompoundTag entry(ConnectorResource resource, PlayerIdeaStorage storage, boolean full) {
        CompoundTag tag = full ? resource.save(player.registryAccess()) : new CompoundTag();
        tag.putLong("Amount", resource.amount(storage));
        return tag;
    }
}
