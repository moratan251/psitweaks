package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.network.MessageConnectorState;
import com.moratan251.psitweaks.common.network.MessageConnectorTemplate;
import com.moratan251.psitweaks.common.network.MessageConnectorExportSettings;
import com.moratan251.psitweaks.common.storage.connector.ConnectorExportSettings;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorRedstoneMode;
import com.moratan251.psitweaks.common.storage.connector.ConnectorInputMode;
import com.moratan251.psitweaks.common.storage.idea.ItemResourceKey;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.network.PacketDistributor;

/** Player inventory plus ghost filters; published resources remain exclusively in owner storage. */
public class IdeaspaceConnectorMenu extends AbstractContainerMenu {
    public static final int ASSIGN = 0, CLEAR = 1, SIDE = 2, AUTO = 3, ENERGY = 4;
    public static final int SLOT_SIDE = 5, SLOT_AUTO = 6, SLOT_COMMON = 7;
    public static final int REDSTONE = 8, REDSTONE_ALL = 9;
    public static final int INPUT_MODE = 10, ASSIGN_INPUT_FILTER = 11, CLEAR_INPUT_FILTER = 12;
    public static final int INVENTORY_X = 8, INVENTORY_Y = 106, HOTBAR_Y = 164;
    private final Player player;
    private final IdeaspaceConnectorBlockEntity connector;
    private final BlockPos pos;
    private UUID session = UUID.randomUUID();
    private long revision;
    private long templateRevision;
    private boolean inventoryVisible = true;
    private long syncedStorage = -1, syncedSettings = -1, lastSyncTick = -20;
    private CompoundTag clientState = new CompoundTag();

    public IdeaspaceConnectorMenu(int id, Inventory inventory, IdeaspaceConnectorBlockEntity connector) {
        this(id, inventory, connector.getBlockPos(), connector);
    }

    private IdeaspaceConnectorMenu(int id, Inventory inventory, BlockPos pos, IdeaspaceConnectorBlockEntity connector) {
        super(ModMenuTypes.IDEASPACE_CONNECTOR.get(), id);
        this.player = inventory.player;
        this.pos = pos;
        this.connector = connector;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addPlayerSlot(inventory, column + row * 9 + 9, INVENTORY_X + column * 18, INVENTORY_Y + row * 18);
        for (int column = 0; column < 9; column++)
            addPlayerSlot(inventory, column, INVENTORY_X + column * 18, HOTBAR_Y);
    }

    private void addPlayerSlot(Inventory inventory, int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            @Override public boolean isActive() { return inventoryVisible; }
        });
    }

    public void setInventoryVisible(boolean visible) { inventoryVisible = visible; }

    public static IdeaspaceConnectorMenu fromNetwork(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new IdeaspaceConnectorMenu(id, inventory, buf.readBlockPos(), null);
    }

    @Override public boolean stillValid(Player player) {
        return connector == null ? player.level().getBlockEntity(pos) instanceof IdeaspaceConnectorBlockEntity
                : connector.canConfigure(player);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (!(index < 27 ? moveItemStackTo(stack, 27, 36, false) : moveItemStackTo(stack, 0, 27, false)))
            return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    public UUID session() { return session; }
    public BlockPos blockPos() { return pos; }
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
        for (String key : new String[] {"Selected", "InputFilters"}) {
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
        if (action.containerId() != containerId || !authorized(sender, action.session())) return;
        int argument = action.argument();
        if (action.action() >= INPUT_MODE && action.action() <= CLEAR_INPUT_FILTER
                && (action.revision() != revision || action.slot() < 0 || action.slot() >= IdeaspaceConnectorBlockEntity.SLOTS)) return;
        switch (action.action()) {
            case ASSIGN, ASSIGN_INPUT_FILTER -> {
                if (getCarried().isEmpty() || (argument != 0 && argument != 1)) return;
                ConnectorResource resource = ItemResourceKey.of(getCarried()).map(ConnectorResource::item).orElse(ConnectorResource.EMPTY);
                if (argument == 1) {
                    resource = ConnectorResource.EMPTY;
                    var fluids = FluidUtil.getFluidHandler(getCarried().copyWithCount(1)).orElse(null);
                    if (fluids != null) for (int tank = 0; tank < fluids.getTanks(); tank++) {
                        // A filter only needs the identity, regardless of the container's drain permission or rate.
                        resource = FluidResourceKey.of(fluids.getFluidInTank(tank)).map(ConnectorResource::fluid).orElse(ConnectorResource.EMPTY);
                        if (resource.kind() != ConnectorResource.Kind.EMPTY) break;
                    }
                    if (resource.kind() == ConnectorResource.Kind.EMPTY && MekanismCompat.isMekanismLoaded())
                        resource = ConnectorMekanism.containedResource(getCarried().copyWithCount(1));
                    if (resource.kind() == ConnectorResource.Kind.EMPTY) return;
                }
                if (action.action() == ASSIGN_INPUT_FILTER) {
                    if (resource.save(player.registryAccess()).sizeInBytes() > MessageConnectorTemplate.MAX_TEMPLATE_SIZE) return;
                    connector.setInputFilter(action.slot(), resource);
                }
                else connector.setResource(action.slot(), resource);
            }
            case CLEAR -> connector.setResource(action.slot(), ConnectorResource.EMPTY);
            case CLEAR_INPUT_FILTER -> connector.setInputFilter(action.slot(), ConnectorResource.EMPTY);
            case INPUT_MODE -> {
                if (argument < 0 || argument >= ConnectorInputMode.values().length) return;
                connector.setInputMode(ConnectorInputMode.byId(argument));
            }
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
            case ENERGY -> connector.setResource(action.slot(), ConnectorResource.ENERGY);
            case REDSTONE, REDSTONE_ALL -> {
                int slot = action.slot();
                if (slot < -1 || slot >= IdeaspaceConnectorBlockEntity.SLOTS || action.revision() != revision
                        || (slot >= 0 && connector.usesCommonSettings(slot))) return;
                if (action.action() == REDSTONE) {
                    if (argument < 0 || argument >= 6) return;
                    Direction side = Direction.values()[argument];
                    connector.setRedstoneMode(slot, side, connector.redstoneMode(slot, side).next());
                } else {
                    if (argument < 0 || argument >= ConnectorRedstoneMode.values().length) return;
                    connector.setAllRedstoneModes(slot, ConnectorRedstoneMode.byId(argument));
                }
            }
            case SLOT_SIDE, SLOT_AUTO, SLOT_COMMON -> {
                int slot = action.slot();
                if (slot < 0 || slot >= IdeaspaceConnectorBlockEntity.SLOTS || action.revision() != revision) return;
                if (action.action() == SLOT_COMMON) {
                    if (argument != 0 && argument != 1) return;
                    connector.setUsesCommonSettings(slot, argument == 0);
                } else {
                    if (argument < 0 || argument >= 6 || connector.usesCommonSettings(slot)) return;
                    Direction side = Direction.values()[argument];
                    if (action.action() == SLOT_SIDE) connector.setSideMode(slot, side, connector.sideMode(slot, side).next());
                    else connector.setAutomatic(slot, side, !connector.automatic(slot, side));
                }
            }
            default -> { return; }
        }
        syncedSettings = -1;
        broadcastChanges();
    }

    private boolean authorized(Player sender, UUID session) {
        return sender == player && connector != null && stillValid(sender) && this.session.equals(session);
    }

    public void handleExportSettings(Player sender, MessageConnectorExportSettings message) {
        if (message.containerId() != containerId || !authorized(sender, message.session()) || message.revision() != revision) return;
        if (!connector.setExportSettings(message.slot(), message.settings().toArray(ConnectorExportSettings[]::new))) return;
        syncedSettings = -1;
        broadcastChanges();
    }

    public void handleTemplate(Player sender, MessageConnectorTemplate message) {
        if (message.containerId() != containerId || !authorized(sender, message.session())
                || message.slot() < 0 || message.slot() >= IdeaspaceConnectorBlockEntity.SLOTS
                || (message.inputFilter() && message.revision() != revision)
                || message.resource().sizeInBytes() > MessageConnectorTemplate.MAX_TEMPLATE_SIZE) return;
        ConnectorResource resource = ConnectorResource.load(message.resource(), player.registryAccess());
        if (resource.kind() == ConnectorResource.Kind.EMPTY) return;
        if (resource.kind() == ConnectorResource.Kind.CHEMICAL
                && (!MekanismCompat.isMekanismLoaded() || !ConnectorMekanism.validChemical(resource.chemical()))) return;
        if (message.inputFilter()) {
            if (!connector.setInputFilter(message.slot(), resource)) return;
        } else connector.setResource(message.slot(), resource);
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
        boolean full = settingsChanged;
        CompoundTag data = new CompoundTag();
        if (full) { templateRevision++; revision++; }
        data.putLong("Templates", templateRevision);
        data.putBoolean("Full", full);
        ListTag selected = new ListTag();
        for (int i = 0; i < IdeaspaceConnectorBlockEntity.SLOTS; i++) selected.add(entry(connector.resource(i), storage, full));
        data.put("Selected", selected);
        ListTag filters = new ListTag();
        for (int i = 0; i < IdeaspaceConnectorBlockEntity.SLOTS; i++) filters.add(entry(connector.inputFilter(i), storage, full));
        data.put("InputFilters", filters);
        data.putBoolean("LoadFailed", storage.isLoadFailed());
        connector.writeConnectionSettings(data);
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
