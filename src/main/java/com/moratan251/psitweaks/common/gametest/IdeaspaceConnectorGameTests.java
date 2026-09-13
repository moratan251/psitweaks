package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.network.MessageConnectorState;
import com.moratan251.psitweaks.common.network.MessageConnectorTemplate;
import com.moratan251.psitweaks.common.network.MessageConnectorExportSettings;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaspaceConnector;
import com.moratan251.psitweaks.common.storage.connector.*;
import com.moratan251.psitweaks.common.storage.idea.*;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.util.UUID;
import java.util.List;
import java.util.function.Consumer;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;

@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaspaceConnectorGameTests {
    private static ConnectorExportSettings[] defaultExportSettings() {
        var values = new ConnectorExportSettings[ConnectorExportSettings.TYPES];
        for (int type = 0; type < values.length; type++) values[type] = ConnectorExportSettings.defaults(type);
        return values;
    }

    @GameTest(template = "connector_empty")
    public static void configuredExportAmountsRespectLimitsAndShortages(GameTestHelper helper) {
        var source = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var target = place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
        var apple = ConnectorResource.item(ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow());
        var water = ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow());
        source.setResource(0, apple);
        source.setResource(1, water);
        source.setResource(2, ConnectorResource.ENERGY);
        var settings = defaultExportSettings();
        settings[0] = new ConnectorExportSettings(3, 10);
        settings[1] = new ConnectorExportSettings(250, 15);
        settings[2] = new ConnectorExportSettings(175, 20);
        settings[3] = new ConnectorExportSettings(750, 25);
        helper.assertTrue(source.setExportSettings(-1, settings), "Valid common rates rejected");
        source.storage().insert(new ItemStack(Items.APPLE), 10);
        source.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 600);
        source.storage().insertEnergy(800, false);
        source.setAutomatic(Direction.EAST, true);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(apple.amount(target.storage()) == 3 && water.amount(target.storage()) == 250 && target.storage().energy() == 750,
                "Configured per-resource export amounts ignored");
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(apple.amount(source.storage()) == 4 && water.amount(source.storage()) == 100 && source.storage().energy() == 0
                && target.storage().energy() == 800, "Configured transfer lost resources or mishandled shortage");
        helper.assertTrue(source.handlers(Direction.EAST).items.extractItem(0, 64, true).getCount() == 4,
                "Automatic rate incorrectly capped external extraction");
        if (MekanismCompat.isMekanismLoaded()) ConnectorChemicalChecks.checkConfiguredAmounts(helper, source, target);
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void exportIntervalsAreIndependentAndCanBeShortened(GameTestHelper helper) {
        var source = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var target = place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
        var apple = ConnectorResource.item(ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow());
        source.setResource(0, apple);
        source.setResource(1, ConnectorResource.ENERGY);
        source.storage().insert(new ItemStack(Items.APPLE), 100);
        source.storage().insertEnergy(1000, false);
        var common = defaultExportSettings();
        common[0] = new ConnectorExportSettings(2, 10);
        common[3] = new ConnectorExportSettings(100, 20);
        source.setExportSettings(-1, common);
        source.setAutomatic(Direction.EAST, true);
        helper.runAfterDelay(9, () -> helper.assertTrue(apple.amount(target.storage()) == 0 && target.storage().energy() == 0,
                "Export happened before the configured deadline"));
        helper.runAfterDelay(11, () -> helper.assertTrue(apple.amount(target.storage()) == 2 && target.storage().energy() == 0,
                "Different resource intervals were not independent"));
        helper.runAfterDelay(21, () -> {
            helper.assertTrue(apple.amount(target.storage()) == 4 && target.storage().energy() == 100,
                    "Configured frequency drifted or exported twice in one tick");
            source.setUsesCommonSettings(0, false);
            var individual = common.clone();
            individual[0] = new ConnectorExportSettings(1, 1);
            source.setExportSettings(0, individual);
            helper.runAfterDelay(3, () -> {
                helper.assertTrue(apple.amount(target.storage()) > 4 && target.storage().energy() == 100,
                        "Shorter interval waited for the old deadline or changed another slot");
                source.setAutomatic(0, Direction.EAST, false);
                long count = apple.amount(target.storage());
                helper.runAfterDelay(3, () -> {
                    helper.assertTrue(apple.amount(target.storage()) == count, "Disabled slot kept exporting");
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = "connector_empty")
    public static void exportSettingsPayloadIsAtomicAndAuthorized(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "export-settings"));
        var source = place(helper, new BlockPos(1, 1, 1), player.getUUID());
        player.setPos(Vec3.atCenterOf(source.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(26, player.getInventory(), source);
        var settings = defaultExportSettings();
        settings[0] = new ConnectorExportSettings(7, 17);
        settings[1] = new ConnectorExportSettings(Integer.MAX_VALUE, 1200);
        var message = new MessageConnectorExportSettings(26, menu.session(), menu.revision(), -1, List.of(settings));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        try {
            MessageConnectorExportSettings.STREAM_CODEC.encode(buffer, message);
            menu.handleExportSettings(player, MessageConnectorExportSettings.STREAM_CODEC.decode(buffer));
        } finally { buffer.release(); }
        helper.assertTrue(source.exportSettings(0, 0).equals(settings[0]) && source.exportSettings(8, 1).equals(settings[1]),
                "Payload did not preserve valid rates or maximum values");
        source.setUsesCommonSettings(8, false);
        menu.handleExportSettings(player, new MessageConnectorExportSettings(26, menu.session(), menu.revision(), 8,
                List.of(defaultExportSettings())));
        helper.assertTrue(source.exportSettings(8, 0).amount() == 64 && source.exportSettings(0, 0).amount() == 7,
                "Individual rates affected common settings");
        long version = source.settingsVersion(), storageVersion = source.storage().getVersion();
        var outsider = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "rate-outsider"));
        outsider.setPos(player.position());
        var valid = new MessageConnectorExportSettings(26, menu.session(), menu.revision(), -1, List.of(settings));
        menu.handleExportSettings(outsider, valid);
        menu.handleExportSettings(player, new MessageConnectorExportSettings(26, UUID.randomUUID(), menu.revision(), -1, List.of(settings)));
        menu.handleExportSettings(player, new MessageConnectorExportSettings(27, menu.session(), menu.revision(), -1, List.of(settings)));
        menu.handleExportSettings(player, new MessageConnectorExportSettings(26, menu.session(), menu.revision() - 1, -1, List.of(settings)));
        for (int slot : new int[] {-2, 0, 9})
            menu.handleExportSettings(player, new MessageConnectorExportSettings(26, menu.session(), menu.revision(), slot, List.of(settings)));
        for (var invalid : new ConnectorExportSettings[] {new ConnectorExportSettings(0, 5), new ConnectorExportSettings(-1, 5),
                new ConnectorExportSettings(65, 5), new ConnectorExportSettings(1, 0), new ConnectorExportSettings(1, 1201)}) {
            var bad = settings.clone();
            bad[0] = invalid;
            menu.handleExportSettings(player, new MessageConnectorExportSettings(26, menu.session(), menu.revision(), -1, List.of(bad)));
        }
        helper.assertTrue(version == source.settingsVersion() && storageVersion == source.storage().getVersion(),
                "Invalid rates partially applied or mutated storage");
        helper.getLevel().destroyBlock(source.getBlockPos(), false);
        menu.handleExportSettings(player, valid);
        helper.assertTrue(version == source.settingsVersion(), "Removed connector accepted rate settings");
        helper.succeed();
    }

    private static IdeaspaceConnectorBlockEntity place(GameTestHelper helper, BlockPos relative, UUID owner) {
        helper.setBlock(relative, PsitweaksBlocks.IDEASPACE_CONNECTOR.get());
        var connector = (IdeaspaceConnectorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
        connector.initialize(owner, ItemStack.EMPTY);
        return connector;
    }

    @GameTest(template = "connector_empty")
    public static void individualFacesMatchResourcesAndPreserveInheritance(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var storage = connector.storage();
        var apple = ConnectorResource.item(ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow());
        var carrot = ConnectorResource.item(ItemResourceKey.of(new ItemStack(Items.CARROT)).orElseThrow());
        var water = ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow());
        var lava = ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.LAVA, 1)).orElseThrow());
        ConnectorResource[] resources = {apple, carrot, water, lava, ConnectorResource.ENERGY};
        for (int slot = 0; slot < resources.length; slot++) connector.setResource(slot, resources[slot]);
        storage.insert(new ItemStack(Items.APPLE), 12);
        storage.insert(new ItemStack(Items.CARROT), 10);
        storage.insertFluid(new FluidStack(Fluids.WATER, 1), 2000);
        storage.insertFluid(new FluidStack(Fluids.LAVA, 1), 3000);
        storage.insertEnergy(4000, false);
        var items = connector.handlers(Direction.NORTH).items;
        var fluids = connector.handlers(Direction.NORTH).fluids;
        var energy = connector.handlers(Direction.NORTH).energy;
        long version = storage.getVersion();
        for (int slot : new int[] {0, 2, 4}) {
            connector.setUsesCommonSettings(slot, false);
            connector.setSideMode(slot, Direction.NORTH, ConnectorSideMode.OUTPUT);
        }
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.INPUT);
        for (int slot = 0; slot < 9; slot++) {
            helper.assertTrue(!items.isItemValid(slot, new ItemStack(Items.APPLE))
                    && items.insertItem(slot, new ItemStack(Items.APPLE), false).getCount() == 1,
                    "Item input bypassed its resource settings through slot " + slot);
            helper.assertTrue(!fluids.isFluidValid(slot, new FluidStack(Fluids.WATER, 1)), "Water input bypassed its resource settings");
        }
        helper.assertTrue(items.getStackInSlot(0).getCount() == 12 && items.getStackInSlot(1).isEmpty()
                && fluids.getFluidInTank(2).getAmount() == 2000 && fluids.getFluidInTank(3).isEmpty()
                && energy.canExtract() && !energy.canReceive(), "Individual output did not override common input mode");
        helper.assertTrue(fluids.fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE) == 0
                && energy.receiveEnergy(100, false) == 0, "Registered resource accepted disallowed input");
        ItemStack variant = new ItemStack(Items.APPLE);
        variant.set(DataComponents.CUSTOM_NAME, Component.literal("Unpublished variant"));
        helper.assertTrue(items.insertItem(8, variant, true).isEmpty()
                && items.insertItem(8, new ItemStack(Items.CARROT), true).isEmpty()
                && fluids.fill(new FluidStack(Fluids.LAVA, 100), FluidAction.SIMULATE) == 100
                && storage.getVersion() == version, "Inheritance, component identity, or simulation mutated resources");
        helper.assertTrue(items.extractItem(0, 1, false).getCount() == 1
                && fluids.drain(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE).getAmount() == 100
                && energy.extractEnergy(200, false) == 200
                && apple.amount(storage) == 11 && water.amount(storage) == 1900 && storage.energy() == 3800,
                "Individual output did not preserve balances");
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.DISABLED);
        helper.assertTrue(!items.getStackInSlot(0).isEmpty() && !fluids.getFluidInTank(2).isEmpty() && energy.canExtract()
                && !items.insertItem(8, variant, true).isEmpty(), "Common changes leaked into overrides or unregistered input");
        connector.setUsesCommonSettings(0, true);
        helper.assertTrue(items.getStackInSlot(0).isEmpty(), "Returning to common settings did not update cached handler");
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.BOTH);
        helper.assertTrue(items.getStackInSlot(0).getCount() == 11 && items.insertItem(8, new ItemStack(Items.APPLE), true).isEmpty()
                && !energy.canReceive(), "Inherited slot did not follow common settings, or affected another slot");
        if (MekanismCompat.isMekanismLoaded()) ConnectorChemicalChecks.checkIndividualFaces(helper, connector);
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void individualAutomaticOutputRoutesEachResource(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var east = place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
        var north = place(helper, new BlockPos(1, 1, 0), UUID.randomUUID());
        var apple = ConnectorResource.item(ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow());
        var water = ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow());
        connector.setResource(0, apple);
        connector.setResource(1, water);
        connector.setResource(2, ConnectorResource.ENERGY);
        connector.storage().insert(new ItemStack(Items.APPLE), 90);
        connector.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 1500);
        connector.storage().insertEnergy(5000, false);
        for (Direction side : Direction.values()) connector.setSideMode(side, ConnectorSideMode.DISABLED);
        for (int slot = 0; slot < 3; slot++) connector.setUsesCommonSettings(slot, false);
        connector.setSideMode(0, Direction.EAST, ConnectorSideMode.OUTPUT);
        connector.setAutomatic(0, Direction.EAST, true);
        connector.setSideMode(1, Direction.NORTH, ConnectorSideMode.OUTPUT);
        connector.setAutomatic(1, Direction.NORTH, true);
        connector.setSideMode(2, Direction.EAST, ConnectorSideMode.OUTPUT);
        helper.assertTrue(connector.handlers(Direction.EAST).energy.extractEnergy(5000, true) == 5000,
                "Disabling automatic output also disabled passive extraction");
        for (Direction side : Direction.values()) ConnectorTransfers.push(connector, connector.storage(), side);
        helper.assertTrue(apple.amount(east.storage()) == 64 && water.amount(east.storage()) == 0 && east.storage().energy() == 0
                && apple.amount(north.storage()) == 0 && water.amount(north.storage()) == 1000 && north.storage().energy() == 0
                && apple.amount(connector.storage()) == 26 && water.amount(connector.storage()) == 500,
                "Automatic resources went to the wrong face or ignored per-slot toggles");
        connector.setAutomatic(0, Direction.EAST, false);
        connector.setSideMode(1, Direction.NORTH, ConnectorSideMode.INPUT);
        connector.setAutomatic(2, Direction.EAST, true);
        for (Direction side : Direction.values()) ConnectorTransfers.push(connector, connector.storage(), side);
        helper.assertTrue(apple.amount(east.storage()) == 64 && water.amount(north.storage()) == 1000
                && east.storage().energy() == 5000 && connector.storage().energy() == 0,
                "Automatic output ignored changed slot mode/toggle or lost FE");
        if (MekanismCompat.isMekanismLoaded()) ConnectorChemicalChecks.checkIndividualAutomatic(helper, connector, east, north);
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void individualSettingsActionsValidateTargetAndSession(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "slot-settings"));
        var connector = place(helper, new BlockPos(1, 1, 1), player.getUUID());
        player.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(25, player.getInventory(), connector);
        menu.setCarried(new ItemStack(Items.DIAMOND, 3));
        long stockVersion = connector.storage().getVersion();
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_COMMON, 8, 1));
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_SIDE, 8, Direction.EAST.ordinal()));
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_AUTO, 8, Direction.WEST.ordinal()));
        helper.assertTrue(!connector.usesCommonSettings(8) && connector.sideMode(8, Direction.EAST) == ConnectorSideMode.INPUT
                && connector.automatic(8, Direction.WEST) && connector.sideMode(Direction.EAST) == ConnectorSideMode.BOTH
                && connector.usesCommonSettings(0), "Individual actions changed the wrong slot or common settings");
        long settingsVersion = connector.settingsVersion();
        var outsider = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "slot-outsider"));
        outsider.setPos(player.position());
        var valid = new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_SIDE, 8, 0);
        menu.handleAction(outsider, valid);
        menu.handleAction(player, new MessageConnectorAction(25, UUID.randomUUID(), menu.revision(), IdeaspaceConnectorMenu.SLOT_COMMON, 8, 0));
        menu.handleAction(player, new MessageConnectorAction(26, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_COMMON, 8, 0));
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision() - 1, IdeaspaceConnectorMenu.SLOT_COMMON, 8, 0));
        for (int action : new int[] {IdeaspaceConnectorMenu.SLOT_SIDE, IdeaspaceConnectorMenu.SLOT_AUTO, IdeaspaceConnectorMenu.SLOT_COMMON})
            for (int slot : new int[] {-1, 9})
                menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), action, slot, 0));
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_SIDE, 8, 6));
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_AUTO, 8, -1));
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_COMMON, 8, 2));
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_SIDE, 0, 0));
        helper.assertTrue(connector.settingsVersion() == settingsVersion && connector.storage().getVersion() == stockVersion
                && menu.getCarried().is(Items.DIAMOND) && menu.getCarried().getCount() == 3, "Invalid action mutated settings or resources");
        connector.setSideMode(Direction.EAST, ConnectorSideMode.OUTPUT);
        menu.handleAction(player, new MessageConnectorAction(25, menu.session(), menu.revision(), IdeaspaceConnectorMenu.SLOT_COMMON, 8, 0));
        helper.assertTrue(connector.usesCommonSettings(8) && connector.sideMode(8, Direction.EAST) == ConnectorSideMode.OUTPUT,
                "Common toggle did not use current common settings");
        helper.getLevel().destroyBlock(connector.getBlockPos(), false);
        settingsVersion = connector.settingsVersion();
        menu.handleAction(player, valid);
        helper.assertTrue(connector.settingsVersion() == settingsVersion, "Removed connector accepted individual configuration");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void passiveTransfersRespectPublicationAndFaces(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var storage = connector.storage();
        var apple = ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow();
        var water = FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow();
        for (Direction side : Direction.values()) {
            helper.assertTrue(helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, connector.getBlockPos(), side) != null,
                    "Missing item capability on " + side);
            helper.assertTrue(helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, connector.getBlockPos(), side) != null,
                    "Missing fluid capability on " + side);
            helper.assertTrue(helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, connector.getBlockPos(), side) != null,
                    "Missing FE capability on " + side);
        }
        var items = connector.handlers(Direction.NORTH).items;
        var fluids = connector.handlers(Direction.NORTH).fluids;
        var energy = connector.handlers(Direction.NORTH).energy;
        long version = storage.getVersion();
        helper.assertTrue(items.insertItem(0, new ItemStack(Items.APPLE, 32), true).isEmpty(), "Simulated item input");
        helper.assertTrue(fluids.fill(new FluidStack(Fluids.WATER, 2500), FluidAction.SIMULATE) == 2500, "Simulated fluid input");
        helper.assertTrue(energy.receiveEnergy(4000, true) == 4000 && version == storage.getVersion(), "Simulation changed storage");
        items.insertItem(0, new ItemStack(Items.APPLE, 32), false);
        fluids.fill(new FluidStack(Fluids.WATER, 2500), FluidAction.EXECUTE);
        energy.receiveEnergy(4000, false);
        helper.assertTrue(items.extractItem(0, 32, false).isEmpty() && fluids.drain(1000, FluidAction.EXECUTE).isEmpty()
                && energy.extractEnergy(4000, false) == 0, "Unpublished resource escaped");
        connector.setResource(0, ConnectorResource.item(apple));
        connector.setResource(1, ConnectorResource.fluid(water));
        connector.setResource(2, ConnectorResource.ENERGY);
        helper.assertTrue(items.getStackInSlot(0).getCount() == 32 && fluids.getFluidInTank(1).getAmount() == 2500
                && energy.getEnergyStored() == 4000, "Published view is not live");
        ItemStack namedApple = new ItemStack(Items.APPLE, 5);
        namedApple.set(DataComponents.CUSTOM_NAME, Component.literal("distinct"));
        items.insertItem(8, namedApple, false);
        helper.assertTrue(items.extractItem(0, 64, false).getCount() == 32
                && storage.simulateExtract(ItemResourceKey.of(namedApple).orElseThrow(), 9) == 5, "Component variants mixed");
        helper.assertTrue(fluids.drain(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE).isEmpty(), "Wrong fluid extracted");
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.INPUT);
        helper.assertTrue(fluids.drain(1000, FluidAction.EXECUTE).isEmpty() && energy.extractEnergy(4000, false) == 0,
                "Cached handler ignored new input-only mode");
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.OUTPUT);
        helper.assertTrue(items.insertItem(0, new ItemStack(Items.APPLE), false).getCount() == 1
                && fluids.fill(new FluidStack(Fluids.WATER, 1), FluidAction.EXECUTE) == 0 && energy.receiveEnergy(1, false) == 0,
                "Output-only face accepted input");
        helper.assertTrue(fluids.drain(9000, FluidAction.EXECUTE).getAmount() == 2500 && energy.extractEnergy(9000, false) == 4000,
                "Stock shortage mishandled");
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.DISABLED);
        helper.assertTrue(!energy.canReceive() && !energy.canExtract(), "Disabled face remains usable");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void sharedViewsOwnershipAndRemoval(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        var first = place(helper, new BlockPos(1, 1, 1), owner);
        var second = place(helper, new BlockPos(2, 1, 1), owner);
        var other = place(helper, new BlockPos(3, 1, 1), UUID.randomUUID());
        first.setResource(0, ConnectorResource.ENERGY);
        second.setResource(0, ConnectorResource.ENERGY);
        other.setResource(0, ConnectorResource.ENERGY);
        var handle = first.handlers(Direction.UP).energy;
        handle.receiveEnergy(4000, false);
        helper.assertTrue(second.handlers(Direction.UP).energy.getEnergyStored() == 4000
                && other.handlers(Direction.UP).energy.getEnergyStored() == 0, "Owner isolation or shared view failed");
        second.handlers(Direction.UP).energy.extractEnergy(123, false);
        helper.assertTrue(handle.getEnergyStored() == 3877, "Connector cached old stock");
        first.setResource(8, ConnectorResource.ENERGY);
        helper.assertTrue(first.resource(0).equals(ConnectorResource.EMPTY), "Duplicate view would double-count FE");
        var player = new FakePlayer(helper.getLevel(), new GameProfile(owner, "connector-owner"));
        player.setPos(first.getBlockPos().getX() + 0.5, first.getBlockPos().getY() + 0.5, first.getBlockPos().getZ() + 0.5);
        var outsider = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "outsider"));
        outsider.setPos(player.position());
        helper.assertTrue(first.canConfigure(player) && !first.canConfigure(outsider), "Owner-only GUI failed");
        var menu = new IdeaspaceConnectorMenu(17, player.getInventory(), first);
        var clear = new MessageConnectorAction(17, menu.session(), menu.revision(), IdeaspaceConnectorMenu.CLEAR, 8, 0);
        menu.handleAction(outsider, clear);
        helper.assertTrue(first.resource(8).equals(ConnectorResource.ENERGY), "Foreign player changed settings");
        menu.handleAction(player, new MessageConnectorAction(17, UUID.randomUUID(), menu.revision(), IdeaspaceConnectorMenu.CLEAR, 8, 0));
        helper.assertTrue(first.resource(8).equals(ConnectorResource.ENERGY), "Stale menu changed settings");
        helper.getLevel().destroyBlock(first.getBlockPos(), true);
        helper.assertTrue(handle.receiveEnergy(1, false) == 0 && handle.extractEnergy(1, false) == 0
                && !menu.stillValid(player), "Removed connector still accessible");
        helper.assertTrue(second.storage().energy() == 3877, "Removal lost shared inventory");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void savesOnlySettingsAndPreservesLongAmounts(GameTestHelper helper) throws Exception {
        UUID owner = UUID.randomUUID();
        var connector = place(helper, new BlockPos(1, 1, 1), owner);
        connector.setResource(0, ConnectorResource.ENERGY);
        connector.setResource(1, ConnectorResource.item(ItemResourceKey.of(new ItemStack(Items.DIAMOND)).orElseThrow()));
        connector.setResource(2, ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.LAVA, 1)).orElseThrow()));
        connector.setSideMode(Direction.WEST, ConnectorSideMode.OUTPUT);
        connector.setAutomatic(Direction.WEST, true);
        connector.setUsesCommonSettings(0, false);
        connector.setSideMode(0, Direction.WEST, ConnectorSideMode.INPUT);
        connector.setAutomatic(0, Direction.WEST, false);
        connector.setSideMode(0, Direction.EAST, ConnectorSideMode.OUTPUT);
        connector.setAutomatic(0, Direction.EAST, true);
        var commonRates = defaultExportSettings();
        commonRates[0] = new ConnectorExportSettings(7, 13);
        commonRates[3] = new ConnectorExportSettings(900, 37);
        connector.setExportSettings(-1, commonRates);
        var slotRates = defaultExportSettings();
        slotRates[3] = new ConnectorExportSettings(123, 17);
        connector.setExportSettings(0, slotRates);
        long stored = connector.storage().insertEnergy(5_000_000_123L, false);
        var tag = connector.saveWithFullMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(!tag.contains("Energy") && !tag.contains("Items"), "Connector persisted a second inventory");
        helper.assertTrue(!connector.getUpdateTag(helper.getLevel().registryAccess()).contains("Owner")
                && !connector.getUpdateTag(helper.getLevel().registryAccess()).contains("Published"), "Private settings leaked to chunk watchers");
        var file = Files.createTempFile("psitweaks-connector-", ".dat");
        try {
            NbtIo.writeCompressed(tag, file);
            tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            var restored = new IdeaspaceConnectorBlockEntity(connector.getBlockPos(), connector.getBlockState());
            restored.loadWithComponents(tag, helper.getLevel().registryAccess());
            helper.getLevel().setBlockEntity(restored);
            helper.assertTrue(owner.equals(restored.owner()) && restored.resource(0).equals(ConnectorResource.ENERGY)
                    && restored.resource(1).equals(connector.resource(1)) && restored.resource(2).equals(connector.resource(2))
                    && restored.sideMode(Direction.WEST) == ConnectorSideMode.OUTPUT && restored.automatic(Direction.WEST),
                    "Disk round-trip lost owner/publication/sides/automatic settings");
            helper.assertTrue(!restored.usesCommonSettings(0) && restored.usesCommonSettings(1)
                    && restored.sideMode(0, Direction.WEST) == ConnectorSideMode.INPUT && !restored.automatic(0, Direction.WEST)
                    && restored.sideMode(0, Direction.EAST) == ConnectorSideMode.OUTPUT && restored.automatic(0, Direction.EAST),
                    "Disk round-trip lost individual modes/automatic/inheritance");
            helper.assertTrue(restored.exportSettings(0, 3).equals(slotRates[3]) && restored.exportSettings(1, 0).equals(commonRates[0])
                    && tag.getIntArray("ExportCooldowns")[0] == 17, "Disk round-trip lost export quantity, interval, or cooldown");
            helper.assertTrue(restored.storage().energy() == stored, "Reload changed owner balance");
            CompoundTag data = restored.resource(0).save(helper.getLevel().registryAccess());
            data.putLong("Amount", stored);
            var state = new MessageConnectorState(22, UUID.randomUUID(), 4, data);
            var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                MessageConnectorState.STREAM_CODEC.encode(buf, state);
                var decoded = MessageConnectorState.STREAM_CODEC.decode(buf);
                helper.assertTrue(decoded.data().getLong("Amount") == stored && decoded.session().equals(state.session()),
                        "Network truncated balance/session");
            } finally { buf.release(); }
            CompoundTag settings = new CompoundTag();
            restored.writeConnectionSettings(settings);
            var settingsBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                MessageConnectorState.STREAM_CODEC.encode(settingsBuffer, new MessageConnectorState(22, UUID.randomUUID(), 5, settings));
                helper.assertTrue(MessageConnectorState.STREAM_CODEC.decode(settingsBuffer).data().equals(settings),
                        "Network lost per-slot connection settings");
            } finally { settingsBuffer.release(); }
            // Exercise the previous save format on the same instance, clearing any in-memory overrides.
            tag.remove("SlotOverrides");
            tag.remove("SlotSides");
            tag.remove("SlotAutomatic");
            for (String key : new String[] {"ExportAmounts", "ExportIntervals", "SlotExportAmounts", "SlotExportIntervals", "ExportCooldowns"}) tag.remove(key);
            restored.loadWithComponents(tag, helper.getLevel().registryAccess());
            for (int slot = 0; slot < IdeaspaceConnectorBlockEntity.SLOTS; slot++)
                helper.assertTrue(restored.usesCommonSettings(slot) && restored.sideMode(slot, Direction.WEST) == ConnectorSideMode.OUTPUT
                        && restored.automatic(slot, Direction.WEST), "Legacy save did not preserve common behavior");
            for (int type = 0; type < ConnectorExportSettings.TYPES; type++)
                helper.assertTrue(restored.exportSettings(0, type).equals(ConnectorExportSettings.defaults(type)),
                        "Legacy save did not restore the old default export rates");
        } finally { Files.deleteIfExists(file); }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void automaticOutputWaitsAndDoesNotExpire(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var key = ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow();
        connector.storage().insert(new ItemStack(Items.APPLE), 80);
        connector.setResource(0, ConnectorResource.item(key));
        helper.setBlock(new BlockPos(2, 1, 1), Blocks.CHEST);
        connector.autoTransfer();
        var chest = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, connector.getBlockPos().east(), Direction.WEST);
        helper.assertTrue(chest.getStackInSlot(0).isEmpty(), "Automatic output defaults to on");
        connector.setAutomatic(Direction.EAST, true);
        helper.runAfterDelay(26, () -> {
            long inChest = 0;
            for (int i = 0; i < chest.getSlots(); i++) inChest += chest.getStackInSlot(i).getCount();
            helper.assertTrue(inChest == 80 && connector.storage().simulateExtract(key, 100) == 0, "Scheduled output lost or failed to transfer items");
            helper.assertTrue(helper.getLevel().getBlockState(connector.getBlockPos()).is(PsitweaksBlocks.IDEASPACE_CONNECTOR.get()),
                    "Conjured block expiration removed connector");
            helper.succeed();
        });
    }

    @GameTest(template = "connector_empty")
    public static void partialAutomaticTransfersConserveAllResources(GameTestHelper helper) {
        var storage = new PlayerIdeaStorage();
        var item = ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow();
        var fluid = FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow();
        storage.insert(new ItemStack(Items.APPLE), 32);
        storage.insertFluid(new FluidStack(Fluids.WATER, 1), 4000);
        storage.insertEnergy(5000, false);
        var itemTarget = new ItemStackHandler(1) {
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (simulate) return ItemStack.EMPTY;
                int accepted = Math.min(7, stack.getCount());
                super.insertItem(slot, stack.copyWithCount(accepted), false);
                return stack.copyWithCount(stack.getCount() - accepted);
            }
        };
        var fluidTarget = new FluidTank(10000) {
            @Override public int fill(FluidStack stack, FluidAction action) {
                return super.fill(action.simulate() ? stack : stack.copyWithAmount(Math.min(123, stack.getAmount())), action);
            }
        };
        var energyTarget = new EnergyStorage(10000) {
            @Override public int receiveEnergy(int amount, boolean simulate) { return super.receiveEnergy(simulate ? amount : Math.min(45, amount), simulate); }
        };
        helper.assertTrue(ConnectorTransfers.pushItem(storage, item, itemTarget, 64) == 7
                && storage.simulateExtract(item, 100) == 25 && itemTarget.getStackInSlot(0).getCount() == 7, "Item refund mismatch");
        helper.assertTrue(ConnectorTransfers.pushFluid(storage, fluid, fluidTarget, 1000) == 123
                && storage.simulateExtractFluid(fluid, 9000) == 3877 && fluidTarget.getFluidAmount() == 123, "Fluid refund mismatch");
        helper.assertTrue(ConnectorTransfers.pushEnergy(storage, energyTarget, 4000) == 45
                && storage.energy() == 4955 && energyTarget.getEnergyStored() == 45, "Energy refund mismatch");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void spellPlacesProtectsAndSharesResearch(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "connector-caster"));
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        var context = new SpellContext().setPlayer(player);
        var piece = new PieceTrickIdeaspaceConnector(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return new Vector3(pos.getX(), pos.getY(), pos.getZ()); }
        };
        var metadata = new SpellMetadata();
        piece.addToMetadata(metadata);
        helper.assertTrue(piece.params.size() == 1 && metadata.getStat(EnumSpellStat.POTENCY) == 100
                && metadata.getStat(EnumSpellStat.COST) == 500 && metadata.getStat(EnumSpellStat.COMPLEXITY) == 1,
                "Connector spell parameters/stats incorrect");
        Consumer<BlockEvent.EntityPlaceEvent> deny = event -> { if (event.getPos().equals(pos)) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(deny);
        try {
            piece.execute(context);
            helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), "Placement bypassed protection event");
        } finally { NeoForge.EVENT_BUS.unregister(deny); }
        piece.execute(context);
        var connector = (IdeaspaceConnectorBlockEntity) helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(connector != null && player.getUUID().equals(connector.owner()), "Spell did not establish owner");
        helper.assertTrue(connector.getBlockState().getRenderShape() == RenderShape.INVISIBLE, "Connector renders solid geometry");
        piece.execute(context);
        helper.assertTrue(helper.getLevel().getBlockEntity(pos) == connector, "Recast replaced configured connector");
        var group = PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), Psitweaks.location("trick_ideaspace_connector")).orElseThrow();
        helper.assertTrue(group.key().location().equals(Psitweaks.location("idea_storage")), "Connector has a separate research group");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void optionalChemicalIntegration(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        if (MekanismCompat.isMekanismLoaded()) ConnectorChemicalChecks.run(helper, connector);
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void publishedTemplatesAndAmountUpdatesPreserveResourceIdentity(GameTestHelper helper) {
        CompoundTag full = new CompoundTag();
        full.putBoolean("Full", true);
        full.putLong("Templates", 7);
        ListTag entries = new ListTag();
        // Multiple large component-bearing stacks can exceed the default 2 MiB NBT read budget.
        for (int i = 0; i < 9; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Identity", "resource-" + i);
            entry.putByteArray("Components", new byte[300_000]);
            entry.putLong("Amount", 1);
            entries.add(entry);
        }
        full.put("Selected", entries);
        var message = new MessageConnectorState(4, UUID.randomUUID(), 8, full);
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            MessageConnectorState.STREAM_CODEC.encode(buf, message);
            var decoded = MessageConnectorState.STREAM_CODEC.decode(buf);
            helper.assertTrue(decoded.data().equals(full), "Large published templates failed to round-trip");
        } finally { buf.release(); }
        CompoundTag delta = new CompoundTag();
        delta.putLong("Templates", 7);
        ListTag amounts = new ListTag();
        for (int i = 0; i < 9; i++) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Amount", 5_000_000_123L + i);
            amounts.add(entry);
        }
        delta.put("Selected", amounts);
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        connector.setUsesCommonSettings(7, false);
        connector.setSideMode(7, Direction.SOUTH, ConnectorSideMode.INPUT);
        connector.writeConnectionSettings(delta);
        var rates = defaultExportSettings();
        rates[0] = new ConnectorExportSettings(9, 29);
        connector.setExportSettings(7, rates);
        connector.writeConnectionSettings(delta);
        CompoundTag result = IdeaspaceConnectorMenu.mergeAmounts(full, delta);
        var merged = result.getList("Selected", net.minecraft.nbt.Tag.TAG_COMPOUND);
        helper.assertTrue(merged.getCompound(8).getString("Identity").equals("resource-8")
                && merged.getCompound(8).getLong("Amount") == 5_000_000_131L
                && merged.getCompound(8).getByteArray("Components").length == 300_000,
                "Amount update corrupted identities, components, or long quantities");
        helper.assertTrue(result.getInt("SlotOverrides") == 1 << 7
                && result.getIntArray("SlotSides")[7 * 6 + Direction.SOUTH.ordinal()] == ConnectorSideMode.INPUT.ordinal(),
                "Amount update lost individual connection settings");
        helper.assertTrue(result.getIntArray("SlotExportAmounts")[7 * ConnectorExportSettings.TYPES] == 9
                && result.getIntArray("SlotExportIntervals")[7 * ConnectorExportSettings.TYPES] == 29,
                "Amount update lost automatic export settings");
        delta.put("Selected", new ListTag());
        helper.assertTrue(IdeaspaceConnectorMenu.mergeAmounts(full, delta) == null, "Mismatched pages must not be merged");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void openingConsumesHeldItemInteraction(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "connector-user"));
        var connector = place(helper, new BlockPos(1, 1, 1), player.getUUID());
        var pos = connector.getBlockPos();
        player.setPos(Vec3.atCenterOf(pos));
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        for (var item : new net.minecraft.world.item.Item[] {Items.APPLE, Items.BOW, Items.ENDER_PEARL}) {
            for (InteractionHand hand : InteractionHand.values()) {
                var stack = new ItemStack(item, 1);
                player.setItemInHand(hand, stack);
                player.startUsingItem(hand);
                var result = connector.getBlockState().useItemOn(stack, helper.getLevel(), player, hand, hit);
                helper.assertTrue(result.consumesAction() && !player.isUsingItem() && stack.getCount() == 1,
                        "Opening with " + item + " / " + hand + " allowed held item use");
            }
        }
        helper.assertTrue(connector.getBlockState().useWithoutItem(helper.getLevel(), player, hit).consumesAction(),
                "Empty-hand interaction fell through");
        var outsider = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "connector-outsider"));
        outsider.setPos(player.position());
        helper.assertTrue(connector.getBlockState().useItemOn(new ItemStack(Items.ENDER_PEARL), helper.getLevel(), outsider,
                        InteractionHand.MAIN_HAND, hit).consumesAction() && !connector.canConfigure(outsider),
                "Rejected GUI interaction fell through to item use");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void inventoryFiltersDoNotConsumeOrMoveResources(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "connector-inventory"));
        var connector = place(helper, new BlockPos(1, 1, 1), player.getUUID());
        player.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(13, player.getInventory(), connector);
        ItemStack apple = new ItemStack(Items.APPLE, 16);
        apple.set(DataComponents.CUSTOM_NAME, Component.literal("Filter variant"));
        player.getInventory().setItem(9, apple.copy());
        helper.assertTrue(menu.slots.size() == 36 && ItemStack.matches(menu.getSlot(0).getItem(), apple),
                "Player inventory is not exposed as normal slots");
        menu.setCarried(apple.copy());
        long storageVersion = connector.storage().getVersion();
        menu.handleAction(player, new MessageConnectorAction(13, menu.session(), menu.revision(), IdeaspaceConnectorMenu.ASSIGN, 0, 0));
        helper.assertTrue(connector.resource(0).equals(ConnectorResource.item(ItemResourceKey.of(apple).orElseThrow()))
                && ItemStack.matches(menu.getCarried(), apple) && ItemStack.matches(player.getInventory().getItem(9), apple),
                "Item filter consumed inventory, cursor, or components");
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));
        menu.handleAction(player, new MessageConnectorAction(13, menu.session(), menu.revision(), IdeaspaceConnectorMenu.ASSIGN, 1, 1));
        helper.assertTrue(connector.resource(1).equals(ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow()))
                && menu.getCarried().is(Items.WATER_BUCKET) && menu.getCarried().getCount() == 1,
                "Container filter drained or replaced the bucket");
        var waterFilter = connector.resource(1);
        for (var emptyOrPlain : new ItemStack[] {new ItemStack(Items.BUCKET), apple.copy()}) {
            menu.setCarried(emptyOrPlain);
            menu.handleAction(player, new MessageConnectorAction(13, menu.session(), menu.revision(), IdeaspaceConnectorMenu.ASSIGN, 1, 1));
            helper.assertTrue(connector.resource(1).equals(waterFilter) && ItemStack.matches(menu.getCarried(), emptyOrPlain),
                    "Empty container or normal item cleared an existing contents filter");
        }
        menu.handleAction(player, new MessageConnectorAction(13, menu.session(), menu.revision(), IdeaspaceConnectorMenu.ENERGY, 2, 0));
        helper.assertTrue(connector.resource(2).equals(ConnectorResource.ENERGY) && connector.storage().getVersion() == storageVersion,
                "Filter configuration changed stored resources");
        menu.quickMoveStack(player, 0);
        helper.assertTrue(player.getInventory().getItem(9).isEmpty() && ItemStack.matches(player.getInventory().getItem(0), apple),
                "Shift movement failed between inventory and hotbar");
        menu.quickMoveStack(player, 27);
        helper.assertTrue(ItemStack.matches(player.getInventory().getItem(9), apple) && player.getInventory().getItem(0).isEmpty(),
                "Return shift movement lost inventory");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void externalGhostTemplatesValidateSessionAndNeverCreateStock(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "connector-ghost"));
        var connector = place(helper, new BlockPos(1, 1, 1), player.getUUID());
        player.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(14, player.getInventory(), connector);
        ItemStack item = new ItemStack(Items.DIAMOND, 64);
        item.set(DataComponents.CUSTOM_NAME, Component.literal("JEI variant"));
        var resource = ConnectorResource.item(ItemResourceKey.of(item).orElseThrow());
        var message = new MessageConnectorTemplate(14, menu.session(), 0, resource.save(player.registryAccess()));
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        try {
            MessageConnectorTemplate.STREAM_CODEC.encode(buf, message);
            menu.handleTemplate(player, MessageConnectorTemplate.STREAM_CODEC.decode(buf));
        } finally { buf.release(); }
        helper.assertTrue(connector.resource(0).equals(resource) && connector.resource(0).itemStack(1).getCount() == 1,
                "Ghost item identity/components lost");
        var fluid = ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.LAVA, 1000)).orElseThrow());
        var fluidTag = fluid.save(player.registryAccess());
        menu.handleTemplate(player, new MessageConnectorTemplate(14, menu.session(), 1, fluidTag));
        helper.assertTrue(connector.resource(1).equals(fluid) && resource.amount(connector.storage()) == 0
                && fluid.amount(connector.storage()) == 0 && player.getInventory().isEmpty() && menu.getCarried().isEmpty(),
                "Ghost template created or required real stock");
        var chemical = ConnectorResource.chemical(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen"));
        menu.handleTemplate(player, new MessageConnectorTemplate(14, menu.session(), 2, chemical.save(player.registryAccess())));
        helper.assertTrue(connector.resource(2).equals(MekanismCompat.isMekanismLoaded() ? chemical : ConnectorResource.EMPTY)
                        && chemical.amount(connector.storage()) == 0,
                "Chemical filter did not respect optional integration or created stock");
        long version = connector.settingsVersion();
        menu.handleTemplate(player, new MessageConnectorTemplate(14, UUID.randomUUID(), 0, fluidTag));
        menu.handleTemplate(player, new MessageConnectorTemplate(15, menu.session(), 0, fluidTag));
        menu.handleTemplate(player, new MessageConnectorTemplate(14, menu.session(), 9, fluidTag));
        var outsider = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ghost-outsider"));
        outsider.setPos(player.position());
        menu.handleTemplate(outsider, message);
        var invalidChemical = ConnectorResource.chemical(Psitweaks.location("missing_chemical")).save(player.registryAccess());
        menu.handleTemplate(player, new MessageConnectorTemplate(14, menu.session(), 2, invalidChemical));
        var oversized = fluidTag.copy();
        oversized.putByteArray("TooLarge", new byte[MessageConnectorTemplate.MAX_TEMPLATE_SIZE]);
        menu.handleTemplate(player, new MessageConnectorTemplate(14, menu.session(), 0, oversized));
        var oversizedBuffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        boolean rejected = false;
        try {
            MessageConnectorTemplate.STREAM_CODEC.encode(oversizedBuffer, new MessageConnectorTemplate(14, menu.session(), 0, oversized));
            try { MessageConnectorTemplate.STREAM_CODEC.decode(oversizedBuffer); }
            catch (net.minecraft.nbt.NbtAccounterException expected) { rejected = true; }
        } finally { oversizedBuffer.release(); }
        helper.assertTrue(rejected, "Ghost template decoding ignored the NBT allocation limit");
        helper.assertTrue(connector.settingsVersion() == version, "Invalid or foreign template changed settings");
        helper.getLevel().destroyBlock(connector.getBlockPos(), false);
        menu.handleTemplate(player, new MessageConnectorTemplate(14, menu.session(), 0, fluidTag));
        helper.assertTrue(connector.settingsVersion() == version, "Removed connector accepted a ghost template");
        helper.succeed();
    }
}
