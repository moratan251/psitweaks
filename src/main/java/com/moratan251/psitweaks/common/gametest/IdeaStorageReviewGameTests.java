package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import com.moratan251.psitweaks.common.storage.connector.ConnectorTransfers;
import com.moratan251.psitweaks.common.storage.idea.*;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemHandlerHelper;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaStorageReviewGameTests {
    @GameTest(template = "connector_empty")
    public static void unreadWarehousesPreserveOriginalNbtOnDisk(GameTestHelper helper) throws Exception {
        var factory = IdeaStorageSavedData.factory(UUID.randomUUID());
        var data = factory.constructor().get();
        data.storage().insert(new ItemStack(Items.APPLE), 7);
        data.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 900);
        data.storage().insertEnergy(100, false);
        var registries = helper.getLevel().registryAccess();
        CompoundTag valid = data.save(new CompoundTag(), registries);
        var invalidTags = new ArrayList<CompoundTag>();
        var unknownItem = valid.copy();
        unknownItem.getList("Items", 10).getCompound(0).getCompound("item").putString("id", "absent_mod:lost_item");
        invalidTags.add(unknownItem);
        var unknownFluid = valid.copy();
        unknownFluid.getList("Fluids", 10).getCompound(0).getCompound("fluid").putString("id", "absent_mod:lost_fluid");
        invalidTags.add(unknownFluid);
        var badComponents = valid.copy();
        CompoundTag components = new CompoundTag();
        components.putString("minecraft:damage", "not an integer");
        badComponents.getList("Items", 10).getCompound(0).getCompound("item").put("components", components);
        invalidTags.add(badComponents);
        var missingItem = valid.copy();
        missingItem.getList("Items", 10).getCompound(0).remove("item");
        invalidTags.add(missingItem);
        var badList = valid.copy();
        badList.putString("Fluids", "unreadable list");
        invalidTags.add(badList);
        var badCount = valid.copy();
        badCount.getList("Items", 10).getCompound(0).putLong("count", -1);
        invalidTags.add(badCount);
        var overflow = valid.copy();
        ListTag duplicateItems = overflow.getList("Items", 10);
        duplicateItems.getCompound(0).putLong("count", Long.MAX_VALUE);
        duplicateItems.add(duplicateItems.getCompound(0).copy());
        invalidTags.add(overflow);
        var future = valid.copy();
        future.putInt("DataVersion", IdeaStorageSavedData.CURRENT_DATA_VERSION + 1);
        future.putString("UnknownFutureField", "retain me");
        invalidTags.add(future);
        var file = Files.createTempFile("psitweaks-unread-warehouse-", ".dat");
        try {
            for (CompoundTag original : invalidTags) {
                var loaded = factory.deserializer().apply(original, registries);
                helper.assertTrue(loaded.storage().isLoadFailed(), "Invalid warehouse was not locked: " + original);
                helper.assertTrue(loaded.storage().insertEnergy(10, false) == 0
                        && loaded.storage().extractEnergy(10, false) == 0
                        && loaded.storage().insert(new ItemStack(Items.DIAMOND), 1) == 0,
                        "Automatic input/output modified an unread warehouse");
                loaded.setDirty(); // Force the persistence path even when ordinary I/O could not dirty it.
                NbtIo.writeCompressed(loaded.save(new CompoundTag(), registries), file);
                CompoundTag disk = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
                helper.assertTrue(disk.equals(original), "Unread data changed on disk");
                disk.putString("CallerMutation", "must not alter retained data");
                helper.assertTrue(loaded.save(new CompoundTag(), registries).equals(original), "Save exposed retained mutable NBT");
            }
            var loaded = factory.deserializer().apply(valid, registries);
            helper.assertTrue(!loaded.storage().isLoadFailed() && loaded.storage().insertEnergy(1, false) == 1,
                    "Valid warehouse could not be used");
            NbtIo.writeCompressed(loaded.save(new CompoundTag(), registries), file);
            var restored = factory.deserializer().apply(NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()), registries);
            helper.assertTrue(restored.storage().energy() == 101 && restored.storage().itemTypeCount() == 1
                    && restored.storage().fluidTypeCount() == 1, "Valid data no longer round-trips");
        } finally { Files.deleteIfExists(file); }
        helper.succeed();
    }

    private static IdeaspaceConnectorBlockEntity place(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, PsitweaksBlocks.IDEASPACE_CONNECTOR.get());
        var connector = (IdeaspaceConnectorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        connector.initialize(UUID.randomUUID(), ItemStack.EMPTY);
        return connector;
    }

    @GameTest(template = "connector_empty")
    public static void fullPublishedSlotsStillAcceptUnpublishedItems(GameTestHelper helper) {
        var target = place(helper, new BlockPos(2, 1, 1));
        Item[] types = {Items.STONE, Items.DIRT, Items.COBBLESTONE, Items.OAK_LOG, Items.APPLE,
                Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT, Items.COAL};
        for (int slot = 0; slot < types.length; slot++) {
            var stack = new ItemStack(types[slot]);
            target.setResource(slot, ConnectorResource.item(ItemResourceKey.of(stack).orElseThrow()));
            target.storage().insert(stack, 64);
        }
        var input = target.handlers(Direction.WEST).items;
        ItemStack offered = new ItemStack(Items.BLAZE_ROD, 12);
        var key = ItemResourceKey.of(offered).orElseThrow();
        helper.assertTrue(input.getSlots() == 10 && input.getStackInSlot(9).isEmpty(), "Missing empty insertion slot");
        helper.assertTrue(ItemHandlerHelper.insertItemStacked(input, offered, true).isEmpty()
                && target.storage().simulateExtract(key, 100) == 0, "Simulation failed or inserted items");
        helper.assertTrue(ItemHandlerHelper.insertItemStacked(input, offered, false).isEmpty()
                && target.storage().simulateExtract(key, 100) == 12, "Stacked insertion stopped at full published slots");
        helper.assertTrue(input.getStackInSlot(9).isEmpty() && input.extractItem(9, 64, false).isEmpty(),
                "Insertion slot exposed inventory");
        var source = place(helper, new BlockPos(1, 1, 1));
        source.setResource(0, ConnectorResource.item(key));
        source.storage().insert(offered, 12);
        source.setAutomatic(Direction.EAST, true);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(source.storage().simulateExtract(key, 100) == 0
                && target.storage().simulateExtract(key, 100) == 24, "Connector-to-connector output failed");
        target.setUsesCommonSettings(4, false);
        target.setSideMode(4, Direction.WEST, ConnectorSideMode.OUTPUT);
        helper.assertTrue(!input.insertItem(9, new ItemStack(Items.APPLE), false).isEmpty(),
                "Extra input slot bypassed published resource settings");
        target.setSideMode(Direction.WEST, ConnectorSideMode.OUTPUT);
        helper.assertTrue(!ItemHandlerHelper.insertItemStacked(input, offered, false).isEmpty(), "Output-only face accepted input");
        helper.getLevel().destroyBlock(target.getBlockPos(), false);
        helper.assertTrue(!input.insertItem(9, offered, false).isEmpty(), "Cached input slot survived block removal");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void reentrantSupplyCannotWithdrawTwice(GameTestHelper helper) {
        for (boolean connectorPath : new boolean[] {false, true}) {
            var storage = new PlayerIdeaStorage();
            storage.insertEnergy(100, false);
            var other = new EnergyStorage(100);
            var target = new EnergyStorage(100) {
                @Override public int receiveEnergy(int amount, boolean simulate) {
                    helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, other, 80) == 0,
                            "Nested supply escaped warehouse guard");
                    helper.assertTrue(storage.extractEnergy(10, false) == 0 && storage.insertEnergy(10, false) == 0,
                            "Direct FE access escaped warehouse guard");
                    return super.receiveEnergy(Math.min(amount, 80), simulate);
                }
            };
            int moved = connectorPath ? ConnectorTransfers.pushEnergy(storage, target, 100)
                    : IdeaStorageEnergyTransfer.supply(storage, target, 100);
            helper.assertTrue(moved == 80 && storage.energy() == 20 && target.getEnergyStored() == 80
                    && other.getEnergyStored() == 0, "Reentrant supply changed total FE");
            helper.assertTrue(storage.extractEnergy(1, false) == 1, "Supply left warehouse locked");
        }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void reentrantAbsorbCannotConsumeReservedCapacity(GameTestHelper helper) {
        long previous = PsitweaksConfig.COMMON.ideaStorageMaxEnergy.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(100L);
            var storage = new PlayerIdeaStorage();
            var other = new EnergyStorage(100, 0, 100, 100);
            var source = new EnergyStorage(100, 0, 100, 100) {
                @Override public int extractEnergy(int amount, boolean simulate) {
                    helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, other, 100) == 0,
                            "Nested absorption consumed reserved capacity");
                    helper.assertTrue(storage.insertEnergy(100, false) == 0, "Direct insertion consumed reserved capacity");
                    if (!simulate) PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(1L);
                    return super.extractEnergy(amount, simulate);
                }
            };
            helper.assertTrue(IdeaStorageEnergyTransfer.absorb(storage, source, 100) == 100
                    && storage.energy() == 100 && source.getEnergyStored() == 0 && other.getEnergyStored() == 100,
                    "Absorption or mid-call capacity reduction lost FE");
            helper.assertTrue(storage.extractEnergy(1, false) == 1, "Absorption left warehouse locked");
        } finally { PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(previous); }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void energyTransferReleasesGuardAndRefundsOnFailure(GameTestHelper helper) {
        var storage = new PlayerIdeaStorage();
        storage.insertEnergy(100, false);
        var broken = new EnergyStorage(100) {
            @Override public int receiveEnergy(int amount, boolean simulate) {
                if (!simulate) throw new IllegalStateException("Test failure before acceptance");
                return super.receiveEnergy(amount, true);
            }
        };
        boolean thrown = false;
        try { ConnectorTransfers.pushEnergy(storage, broken, 100); }
        catch (IllegalStateException expected) { thrown = true; }
        helper.assertTrue(thrown && storage.energy() == 100, "Failed supply did not refund");
        var working = new EnergyStorage(100);
        helper.assertTrue(IdeaStorageEnergyTransfer.supply(storage, working, 20) == 20
                && storage.energy() == 80 && working.getEnergyStored() == 20, "Exception left FE guard locked");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void energyOnlySyncOmitsLargeTemplatesAndCoalescesChanges(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "storage-sync-review"));
        var storage = IdeaStorageService.get(helper.getLevel().getServer(), player.getUUID());
        ItemStack item = new ItemStack(Items.APPLE);
        CompoundTag content = new CompoundTag();
        content.putByteArray("LargeTemplate", new byte[240_000]);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(content));
        storage.insert(item, 7);
        storage.insertFluid(new FluidStack(Fluids.WATER, 1), 1000);
        storage.insertChemical(ResourceLocation.parse("mekanism:hydrogen"), 1000);
        storage.insertEnergy(10, false);
        var menu = new IdeaStorageMenu(7, player.getInventory(), player.getUUID(), 4);
        var full = menu.pollStorageSync(0);
        helper.assertTrue(full != null && full.full() && full.entries().size() == 1
                && full.fluidEntries().size() == 1 && full.chemicalEntries().size() == 1, "Initial snapshot incomplete");
        int fullBytes = roundTrip(helper, full);
        menu.acceptClientSync(full);
        long templates = menu.clientTemplateVersion();
        for (int tick = 1; tick < 5; tick++) {
            storage.insertEnergy(1, false);
            helper.assertTrue(menu.pollStorageSync(tick) == null, "Changes were not coalesced");
        }
        var energy = menu.pollStorageSync(5);
        helper.assertTrue(energy != null && !energy.full() && energy.energy() == 14
                && energy.entries().isEmpty() && energy.fluidEntries().isEmpty() && energy.chemicalEntries().isEmpty(),
                "Energy-only update included templates or lost latest FE amount");
        helper.assertTrue(energy.session().equals(full.session()) && energy.containerId() == 7
                && fullBytes > 240_000 && roundTrip(helper, energy) < 64, "Energy update was not small or scoped");
        // Receive while no storage screen is active: templates and JEI ingredient data must survive.
        menu.acceptClientSync(energy);
        helper.assertTrue(menu.clientSnapshot().energy() == 14 && menu.clientTemplateVersion() == templates
                && menu.clientSnapshot().entries() == full.entries()
                && menu.clientSnapshot().fluidEntries() == full.fluidEntries()
                && menu.clientSnapshot().chemicalEntries() == full.chemicalEntries()
                && menu.clientStorageEntries().size() == 1, "FE update rebuilt or discarded inventory templates");
        long received = menu.clientSnapshotVersion();
        menu.acceptClientSync(MessageIdeaStorageSync.energyUpdate(7, UUID.randomUUID(), 3, 999, 999));
        menu.acceptClientSync(MessageIdeaStorageSync.energyUpdate(8, full.session(), 3, 999, 999));
        helper.assertTrue(menu.clientSnapshotVersion() == received && menu.clientSnapshot().energy() == 14,
                "Foreign or stale FE update changed client snapshot");
        helper.assertTrue(menu.pollStorageSync(10) == null, "Unchanged inventory was sent again");
        storage.extractEnergy(Long.MAX_VALUE, false);
        var zero = menu.pollStorageSync(10);
        menu.acceptClientSync(zero);
        helper.assertTrue(zero.energy() == 0 && menu.clientSnapshot().energy() == 0, "Zero FE was not synchronized");
        storage.insertEnergy(20, false);
        helper.assertTrue(!menu.pollStorageSync(15).full(), "Reappearing FE forced full sync");
        storage.insert(new ItemStack(Items.DIAMOND), 1);
        helper.assertTrue(menu.pollStorageSync(16) == null, "Inventory change ignored throttle");
        var updated = menu.pollStorageSync(20);
        helper.assertTrue(updated.full() && updated.entries().size() == 2 && updated.energy() == 20,
                "Non-energy inventory change was not synchronized");
        var reopened = new IdeaStorageMenu(7, player.getInventory(), player.getUUID(), 4).pollStorageSync(20);
        helper.assertTrue(reopened.full() && !reopened.session().equals(full.session()), "Reopened menu reused stale FE session");
        helper.succeed();
    }

    private static int roundTrip(GameTestHelper helper, MessageIdeaStorageSync message) {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            MessageIdeaStorageSync.STREAM_CODEC.encode(buf, message);
            int bytes = buf.readableBytes();
            var decoded = MessageIdeaStorageSync.STREAM_CODEC.decode(buf);
            helper.assertTrue(decoded.full() == message.full() && decoded.session().equals(message.session())
                    && decoded.containerId() == message.containerId() && decoded.energy() == message.energy()
                    && decoded.entries().size() == message.entries().size(), "Sync codec changed data");
            return bytes;
        } finally { buf.release(); }
    }
}
