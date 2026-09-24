package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.network.MessageConnectorState;
import com.moratan251.psitweaks.common.storage.connector.*;
import com.moratan251.psitweaks.common.storage.idea.*;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConnectorRedstoneGameTests {
    private static ConnectorResource apple() {
        return ConnectorResource.item(ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow());
    }

    private static IdeaspaceConnectorBlockEntity place(GameTestHelper helper, BlockPos pos, UUID owner) {
        helper.setBlock(pos, PsitweaksBlocks.IDEASPACE_CONNECTOR.get());
        var connector = (IdeaspaceConnectorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        connector.initialize(owner, ItemStack.EMPTY);
        return connector;
    }

    private static void seed(IdeaspaceConnectorBlockEntity connector) {
        connector.setResource(0, apple());
        connector.setResource(1, ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow()));
        connector.setResource(2, ConnectorResource.ENERGY);
        connector.storage().insert(new ItemStack(Items.APPLE), 100);
        connector.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 10000);
        connector.storage().insertEnergy(10000, false);
    }

    @GameTest(template = "connector_empty")
    public static void redstoneDustConnectsAndControlsTheBlock(GameTestHelper helper) {
        BlockPos wirePos = new BlockPos(2, 1, 1), powerPos = new BlockPos(1, 1, 1);
        helper.setBlock(wirePos.below(), Blocks.STONE);
        helper.setBlock(wirePos, Blocks.REDSTONE_WIRE);
        helper.setBlock(powerPos, Blocks.REDSTONE_BLOCK);
        var connector = place(helper, new BlockPos(2, 1, 2), UUID.randomUUID());
        seed(connector);
        connector.setRedstoneMode(-1, Direction.EAST, ConnectorRedstoneMode.HIGH);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(wirePos)).getValue(RedStoneWireBlock.SOUTH) != RedstoneSide.NONE,
                    "Dust did not connect to the connector at a corner");
            helper.assertTrue(connector.handlers(Direction.EAST).energy.canExtract(), "Dust did not power the connector");
            helper.setBlock(powerPos, Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(!connector.handlers(Direction.EAST).energy.canExtract(), "Removing dust power did not stop transfers");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "connector_empty")
    public static void redstoneGatesCachedHandlersFromAnySignalFace(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        seed(connector);
        var north = connector.handlers(Direction.NORTH);
        var south = connector.handlers(Direction.SOUTH);
        connector.setRedstoneMode(-1, Direction.NORTH, ConnectorRedstoneMode.HIGH);
        connector.setRedstoneMode(-1, Direction.SOUTH, ConnectorRedstoneMode.LOW);
        connector.setSideMode(Direction.WEST, ConnectorSideMode.DISABLED);
        long version = connector.storage().getVersion();
        assertPassive(helper, north, false);
        assertPassive(helper, south, true);
        assertPassive(helper, connector.handlers(Direction.EAST), true);
        for (Direction direction : Direction.values()) {
            BlockPos powerPos = connector.getBlockPos().relative(direction);
            var oldState = helper.getLevel().getBlockState(powerPos);
            helper.getLevel().setBlockAndUpdate(powerPos, Blocks.REDSTONE_BLOCK.defaultBlockState());
            assertPassive(helper, north, true);
            assertPassive(helper, south, false);
            assertPassive(helper, connector.handlers(Direction.WEST), false);
            helper.getLevel().setBlockAndUpdate(powerPos, oldState);
            assertPassive(helper, north, false);
            assertPassive(helper, south, true);
        }
        helper.assertTrue(connector.storage().getVersion() == version, "Simulation or blocked execution changed resources");
        // Successful execute round-trips, using the same cached handler after all signal transitions.
        helper.assertTrue(south.items.insertItem(9, new ItemStack(Items.APPLE, 3), false).isEmpty()
                && south.items.extractItem(0, 3, false).getCount() == 3, "Active item execution failed");
        helper.assertTrue(south.fluids.fill(new FluidStack(Fluids.WATER, 30), FluidAction.EXECUTE) == 30
                && south.fluids.drain(30, FluidAction.EXECUTE).getAmount() == 30, "Active fluid execution failed");
        helper.assertTrue(south.energy.receiveEnergy(30, false) == 30 && south.energy.extractEnergy(30, false) == 30,
                "Active energy execution failed");
        if (MekanismCompat.isMekanismLoaded()) ConnectorRedstoneChemicalChecks.checkCachedHandler(helper, connector);
        helper.succeed();
    }

    private static void assertPassive(GameTestHelper helper, ConnectorHandlers handlers, boolean active) {
        for (boolean simulate : new boolean[] {true, false}) {
            if (active && !simulate) continue;
            helper.assertTrue(handlers.items.insertItem(9, new ItemStack(Items.APPLE, 3), simulate).getCount() == (active ? 0 : 3)
                    && handlers.items.insertItem(9, new ItemStack(Items.CARROT, 3), simulate).getCount() == (active ? 0 : 3)
                    && handlers.items.extractItem(0, 3, simulate).getCount() == (active ? 3 : 0), "Item redstone gate failed");
            var action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
            helper.assertTrue(handlers.fluids.fill(new FluidStack(Fluids.WATER, 30), action) == (active ? 30 : 0)
                    && handlers.fluids.fill(new FluidStack(Fluids.LAVA, 30), action) == (active ? 30 : 0)
                    && handlers.fluids.drain(30, action).getAmount() == (active ? 30 : 0)
                    && handlers.fluids.drain(new FluidStack(Fluids.WATER, 30), action).getAmount() == (active ? 30 : 0),
                    "Fluid redstone gate failed");
            helper.assertTrue(handlers.energy.receiveEnergy(30, simulate) == (active ? 30 : 0)
                    && handlers.energy.extractEnergy(30, simulate) == (active ? 30 : 0), "FE redstone gate failed");
        }
        helper.assertTrue(handlers.items.isItemValid(9, new ItemStack(Items.APPLE)) == active
                && handlers.fluids.isFluidValid(1, new FluidStack(Fluids.WATER, 1)) == active
                && handlers.energy.canReceive() == active && handlers.energy.canExtract() == active,
                "Handler permission query ignored redstone");
        helper.assertTrue(handlers.items.getStackInSlot(0).isEmpty() != active
                && handlers.fluids.getFluidInTank(1).isEmpty() != active
                && (handlers.energy.getEnergyStored() > 0) == active, "Inactive face still exposed extractable stock");
    }

    @GameTest(template = "connector_empty")
    public static void slotRedstoneOverridesFollowResourceIdentity(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        seed(connector);
        connector.setAllRedstoneModes(-1, ConnectorRedstoneMode.LOW);
        connector.setUsesCommonSettings(0, false);
        for (Direction side : Direction.values())
            helper.assertTrue(connector.redstoneMode(0, side) == ConnectorRedstoneMode.LOW, "Individual profile did not copy conditions");
        connector.setRedstoneMode(0, Direction.NORTH, ConnectorRedstoneMode.HIGH);
        var handler = connector.handlers(Direction.NORTH).items;
        for (int slot : new int[] {0, 8, 9})
            helper.assertTrue(handler.insertItem(slot, new ItemStack(Items.APPLE, 3), false).getCount() == 3,
                    "Input bypassed resource condition via slot " + slot);
        helper.assertTrue(handler.insertItem(9, new ItemStack(Items.CARROT), true).isEmpty()
                && connector.handlers(Direction.NORTH).energy.canReceive(), "Unpublished/common resource ignored shared condition");
        connector.setAllRedstoneModes(-1, ConnectorRedstoneMode.ALWAYS);
        helper.assertTrue(connector.redstoneMode(0, Direction.SOUTH) == ConnectorRedstoneMode.LOW
                && connector.redstoneMode(1, Direction.SOUTH) == ConnectorRedstoneMode.ALWAYS,
                "Changing shared conditions overwrote individual settings");
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.REDSTONE_BLOCK);
        helper.assertTrue(handler.insertItem(9, new ItemStack(Items.APPLE), true).isEmpty()
                && handler.extractItem(0, 3, true).getCount() == 3, "Individual HIGH did not open");
        connector.setSideMode(0, Direction.NORTH, ConnectorSideMode.INPUT);
        helper.assertTrue(handler.extractItem(0, 3, false).isEmpty(), "Redstone bypassed the configured I/O mode");
        connector.setUsesCommonSettings(0, true);
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
        helper.assertTrue(handler.extractItem(0, 3, true).getCount() == 3
                && !connector.setAllRedstoneModes(0, ConnectorRedstoneMode.HIGH), "Returning to shared settings failed");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void automaticOutputStopsAndResumesOnWorldSignal(GameTestHelper helper) {
        var source = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var target = place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
        seed(source);
        if (MekanismCompat.isMekanismLoaded()) ConnectorRedstoneChemicalChecks.seed(source);
        var settings = new ConnectorExportSettings[ConnectorExportSettings.TYPES];
        for (int type = 0; type < settings.length; type++) settings[type] = new ConnectorExportSettings(2, 5);
        source.setExportSettings(-1, settings);
        source.setAutomatic(Direction.EAST, true);
        source.setRedstoneMode(-1, Direction.EAST, ConnectorRedstoneMode.HIGH);
        // A direct call to the transfer helper must enforce the same gate as the ticker.
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.runAfterDelay(8, () -> {
            assertAutomaticAmounts(helper, target, 0);
            helper.setBlock(new BlockPos(1, 2, 1), Blocks.REDSTONE_BLOCK);
            helper.runAfterDelay(2, () -> {
                assertAutomaticAmounts(helper, target, 2);
                helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
                helper.runAfterDelay(12, () -> {
                    assertAutomaticAmounts(helper, target, 2);
                    helper.setBlock(new BlockPos(1, 2, 1), Blocks.REDSTONE_BLOCK);
                    helper.runAfterDelay(2, () -> {
                        assertAutomaticAmounts(helper, target, 4);
                        helper.succeed();
                    });
                });
            });
        });
    }

    private static void assertAutomaticAmounts(GameTestHelper helper, IdeaspaceConnectorBlockEntity target, long amount) {
        helper.assertTrue(apple().amount(target.storage()) == amount && target.storage().energy() == amount
                && target.storage().simulateExtractFluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow(), 10000) == amount,
                "Automatic redstone transition failed; expected " + amount + " of each resource");
        if (MekanismCompat.isMekanismLoaded()) ConnectorRedstoneChemicalChecks.assertAmount(helper, target, amount);
    }

    @GameTest(template = "connector_empty")
    public static void redstoneSettingsSurviveDiskAndLegacyDefaults(GameTestHelper helper) throws Exception {
        var source = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        seed(source);
        source.setAllRedstoneModes(-1, ConnectorRedstoneMode.HIGH);
        source.setUsesCommonSettings(0, false);
        source.setRedstoneMode(0, Direction.NORTH, ConnectorRedstoneMode.LOW);
        source.setAutomatic(Direction.EAST, true);
        CompoundTag saved = source.saveWithFullMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(!source.getUpdateTag(helper.getLevel().registryAccess()).contains("Redstone"),
                "Private redstone settings leaked to chunk watchers");
        var file = Files.createTempFile("psitweaks-connector-redstone-", ".dat");
        try {
            NbtIo.writeCompressed(saved, file);
            saved = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } finally { Files.deleteIfExists(file); }
        var restored = new IdeaspaceConnectorBlockEntity(source.getBlockPos(), source.getBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.getLevel().setBlockEntity(restored);
        restored.onLoad();
        helper.assertTrue(restored.redstoneMode(0, Direction.NORTH) == ConnectorRedstoneMode.LOW
                && restored.redstoneMode(0, Direction.SOUTH) == ConnectorRedstoneMode.HIGH
                && restored.redstoneMode(2, Direction.NORTH) == ConnectorRedstoneMode.HIGH
                && restored.handlers(Direction.NORTH).items.extractItem(0, 1, true).getCount() == 1
                && !restored.handlers(Direction.NORTH).energy.canExtract(), "Reload lost conditions or inheritance");
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.REDSTONE_BLOCK);
        helper.assertTrue(restored.handlers(Direction.NORTH).items.extractItem(0, 1, true).isEmpty()
                && restored.handlers(Direction.NORTH).energy.canExtract(), "Restored instance kept stale signal state");
        CompoundTag sync = new CompoundTag();
        restored.writeConnectionSettings(sync);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            MessageConnectorState.STREAM_CODEC.encode(buffer, new MessageConnectorState(3, UUID.randomUUID(), 1, sync));
            helper.assertTrue(MessageConnectorState.STREAM_CODEC.decode(buffer).data().equals(sync), "Sync lost redstone profiles");
        } finally { buffer.release(); }
        // Reload into the same instance to catch stale per-slot state on old saves.
        saved.remove("Redstone");
        saved.remove("SlotRedstone");
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        for (int slot = -1; slot < IdeaspaceConnectorBlockEntity.SLOTS; slot++) for (Direction side : Direction.values())
            helper.assertTrue(restored.redstoneMode(slot, side) == ConnectorRedstoneMode.ALWAYS, "Legacy save acquired a new restriction");
        helper.assertTrue(!restored.usesCommonSettings(0) && restored.usesCommonSettings(1), "Legacy load lost existing overrides");
        saved.putIntArray("Redstone", new int[] {-1, Integer.MAX_VALUE});
        saved.putIntArray("SlotRedstone", new int[] {Integer.MIN_VALUE});
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.redstoneMode(-1, Direction.DOWN) == ConnectorRedstoneMode.ALWAYS
                && restored.redstoneMode(0, Direction.DOWN) == ConnectorRedstoneMode.ALWAYS, "Invalid saved mode was not defaulted");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void redstoneActionsValidateSessionAndScope(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "redstone-owner"));
        var source = place(helper, new BlockPos(1, 1, 1), player.getUUID());
        player.setPos(Vec3.atCenterOf(source.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(31, player.getInventory(), source);
        var message = new MessageConnectorAction(31, menu.session(), menu.revision(), IdeaspaceConnectorMenu.REDSTONE, -1, 2);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            MessageConnectorAction.STREAM_CODEC.encode(buffer, message);
            menu.handleAction(player, MessageConnectorAction.STREAM_CODEC.decode(buffer));
        } finally { buffer.release(); }
        helper.assertTrue(source.redstoneMode(-1, Direction.NORTH) == ConnectorRedstoneMode.HIGH, "Face action failed");
        source.setUsesCommonSettings(0, false);
        menu.handleAction(player, new MessageConnectorAction(31, menu.session(), menu.revision(), IdeaspaceConnectorMenu.REDSTONE_ALL, 0, 2));
        for (Direction side : Direction.values())
            helper.assertTrue(source.redstoneMode(0, side) == ConnectorRedstoneMode.LOW, "All-faces action was not atomic");
        helper.assertTrue(source.redstoneMode(-1, Direction.NORTH) == ConnectorRedstoneMode.HIGH, "Individual all-faces changed common settings");
        long version = source.settingsVersion();
        var outsider = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "redstone-outsider"));
        outsider.setPos(player.position());
        var valid = new MessageConnectorAction(31, menu.session(), menu.revision(), IdeaspaceConnectorMenu.REDSTONE, -1, 2);
        menu.handleAction(outsider, valid);
        menu.handleAction(player, new MessageConnectorAction(32, menu.session(), menu.revision(), IdeaspaceConnectorMenu.REDSTONE, -1, 2));
        menu.handleAction(player, new MessageConnectorAction(31, UUID.randomUUID(), menu.revision(), IdeaspaceConnectorMenu.REDSTONE, -1, 2));
        menu.handleAction(player, new MessageConnectorAction(31, menu.session(), menu.revision() - 1, IdeaspaceConnectorMenu.REDSTONE, -1, 2));
        for (int action : new int[] {IdeaspaceConnectorMenu.REDSTONE, IdeaspaceConnectorMenu.REDSTONE_ALL}) {
            for (int slot : new int[] {-2, 1, 9, Integer.MAX_VALUE})
                menu.handleAction(player, new MessageConnectorAction(31, menu.session(), menu.revision(), action, slot, 0));
            for (int argument : new int[] {-1, 6, Integer.MAX_VALUE})
                menu.handleAction(player, new MessageConnectorAction(31, menu.session(), menu.revision(), action, -1, argument));
        }
        menu.handleAction(player, new MessageConnectorAction(31, menu.session(), menu.revision(), IdeaspaceConnectorMenu.REDSTONE_ALL, -1, 3));
        helper.assertTrue(source.settingsVersion() == version, "Invalid redstone action changed settings");
        helper.getLevel().destroyBlock(source.getBlockPos(), false);
        menu.handleAction(player, valid);
        helper.assertTrue(source.settingsVersion() == version, "Removed connector accepted redstone settings");
        helper.succeed();
    }
}
