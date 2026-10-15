package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.compat.IdeaLogisticsChemicalChecks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.network.MessageConnectorExportSettings;
import com.moratan251.psitweaks.common.network.MessageConnectorTemplate;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.item.StrictItemFilter;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageResourceModes;
import com.moratan251.psitweaks.common.spells.param.ParamItemSearch;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageDeposit;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageResourceBase;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageWithdraw;
import com.moratan251.psitweaks.common.storage.connector.*;
import com.moratan251.psitweaks.common.storage.idea.*;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemStackHandler;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;

/** Ported from 1.21.1 (commit e1c3617): resource logistics tricks, connector input filter and stock rules. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaLogisticsGameTests {
    public static IdeaspaceConnectorBlockEntity place(GameTestHelper helper, BlockPos relative, UUID owner) {
        helper.setBlock(relative, PsitweaksBlocks.IDEASPACE_CONNECTOR.get());
        var connector = (IdeaspaceConnectorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
        connector.initialize(owner, ItemStack.EMPTY);
        return connector;
    }

    static ConnectorResource apple() { return ConnectorResource.item(ItemResourceKey.of(new ItemStack(Items.APPLE)).orElseThrow()); }
    static ConnectorResource water() { return ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow()); }

    public static ConnectorExportSettings[] rates() {
        var values = new ConnectorExportSettings[ConnectorExportSettings.TYPES];
        for (int i = 0; i < values.length; i++) values[i] = ConnectorExportSettings.defaults(i);
        return values;
    }

    @GameTest(template = "connector_empty")
    public static void inputFiltersAreLiveExactAndIndependent(GameTestHelper helper) {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var handlers = connector.handlers(Direction.NORTH);
        var named = new ItemStack(Items.APPLE, 3);
        named.setHoverName(Component.literal("Filtered variant"));
        var key = ItemResourceKey.of(named).orElseThrow();
        connector.setResource(0, apple());
        connector.setInputMode(ConnectorInputMode.ALLOW_LIST);
        helper.assertTrue(!handlers.items.insertItem(8, named, true).isEmpty(), "Empty allow list accepted a type");
        connector.setInputFilter(0, ConnectorResource.item(key));
        connector.setInputFilter(1, water());
        long version = connector.storage().getVersion();
        helper.assertTrue(handlers.items.insertItem(8, named, true).isEmpty()
                && !handlers.items.insertItem(8, new ItemStack(Items.APPLE), true).isEmpty()
                && version == connector.storage().getVersion(), "Filter ignored NBT identity or simulation changed stock");
        helper.assertTrue(handlers.items.insertItem(8, named, false).isEmpty()
                && connector.storage().simulateExtract(key, 10) == 3 && connector.resource(0).equals(apple()),
                "Input list changed published resources or cached capability ignored the list");
        helper.assertTrue(handlers.fluids.fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE) == 100
                && handlers.fluids.fill(new FluidStack(Fluids.LAVA, 100), FluidAction.EXECUTE) == 0,
                "Fluid input bypassed the allow list");
        connector.setInputMode(ConnectorInputMode.DENY_LIST);
        helper.assertTrue(!handlers.items.insertItem(8, named, false).isEmpty()
                && handlers.items.insertItem(8, new ItemStack(Items.CARROT), false).isEmpty(), "Deny list reversed");
        connector.setInputMode(ConnectorInputMode.EXISTING);
        helper.assertTrue(handlers.items.insertItem(8, named, true).isEmpty(), "Existing type rejected");
        connector.storage().extract(key, 3);
        helper.assertTrue(!handlers.items.insertItem(8, named, false).isEmpty(), "Zero balance still counted as existing");
        helper.assertTrue(handlers.energy.receiveEnergy(7, false) == 7 && !connector.setInputFilter(8, ConnectorResource.ENERGY),
                "Type filter affected FE or accepted an FE template");
        connector.setInputMode(ConnectorInputMode.ANY);
        connector.setRedstoneMode(-1, Direction.NORTH, ConnectorRedstoneMode.HIGH);
        helper.assertTrue(!handlers.items.insertItem(8, named, false).isEmpty(), "Input filter bypassed redstone");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void stockRulesRespectBoundaryTargetAndIndividualSettings(GameTestHelper helper) {
        var source = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var target = place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
        source.setResource(0, apple()); target.setResource(0, apple());
        source.setResource(1, water()); target.setResource(1, water());
        source.setResource(2, ConnectorResource.ENERGY);
        target.setResource(2, ConnectorResource.ENERGY);
        source.storage().insert(new ItemStack(Items.APPLE), 9);
        source.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 1500);
        source.storage().insertEnergy(1000, false);
        target.storage().insert(new ItemStack(Items.APPLE), 3);
        target.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 750);
        target.storage().insertEnergy(80, false);
        var settings = rates();
        settings[0] = new ConnectorExportSettings(64, 5, 10, 7);
        settings[1] = new ConnectorExportSettings(1000, 5, 1500, 1000);
        settings[3] = new ConnectorExportSettings(1000, 5, 1000, 100);
        source.setExportSettings(-1, settings);
        source.setAutomatic(Direction.EAST, true);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(apple().amount(target.storage()) == 3 && water().amount(target.storage()) == 1000
                && target.storage().energy() == 100, "Source boundary or destination shortfall wrong");
        source.storage().insert(new ItemStack(Items.APPLE), 1);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(apple().amount(target.storage()) == 7 && apple().amount(source.storage()) == 6,
                "Minimum must be an inclusive gate, not a remaining-stock reserve");
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(apple().amount(target.storage()) == 7, "Repeated export overshot target");
        source.setUsesCommonSettings(0, false);
        settings[0] = new ConnectorExportSettings(64, 5, 0, -1);
        source.setExportSettings(0, settings);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(apple().amount(source.storage()) == 0 && apple().amount(target.storage()) == 13
                && water().amount(target.storage()) == 1000, "Individual conditions changed other resources");
        source.storage().insert(new ItemStack(Items.APPLE), 2);
        source.setUsesCommonSettings(0, true);
        helper.assertTrue(source.handlers(Direction.WEST).items.extractItem(0, 2, false).getCount() == 2,
                "Automatic stock rules blocked external pulling");
        helper.assertTrue(new ConnectorExportSettings(64, 5, 0, 0).limit(Long.MAX_VALUE, 0) == 0
                && new ConnectorExportSettings(64, 5, 0, Long.MAX_VALUE).limit(Long.MAX_VALUE, Long.MAX_VALUE - 3) == 3,
                "Disabled replenishment or long target overflow");
        helper.succeed();
    }

    /** 1.21.1 compared data components; on 1.20.1 the identity is the stack's NBT tag. */
    @GameTest(template = "connector_empty")
    public static void stockCountsDistinguishComponents(GameTestHelper helper) {
        var named = new ItemStack(Items.APPLE, 64);
        named.setHoverName(Component.literal("Other"));
        var items = new ItemStackHandler(3);
        items.setStackInSlot(0, named);
        items.setStackInSlot(1, new ItemStack(Items.APPLE, 5));
        items.setStackInSlot(2, new ItemStack(Items.APPLE, 4));
        helper.assertTrue(ConnectorTransfers.countItems(items, apple().item()) == 9, "Stock count merged NBT variants");
        var tank = new FluidTank(1000);
        var namedWater = new FluidStack(Fluids.WATER, 600);
        namedWater.getOrCreateTag().putString("variant", "Other");
        tank.setFluid(namedWater);
        helper.assertTrue(ConnectorTransfers.countFluids(tank, water().fluid()) == 0, "Fluid stock merged NBT variants");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void logisticsSettingsPersistToDiskAndLoadOldSaves(GameTestHelper helper) throws Exception {
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        connector.setInputMode(ConnectorInputMode.DENY_LIST);
        connector.setInputFilter(8, water());
        var settings = rates();
        settings[0] = new ConnectorExportSettings(5, 12, Long.MAX_VALUE, Long.MAX_VALUE - 7);
        connector.setExportSettings(-1, settings);
        connector.setUsesCommonSettings(3, false);
        settings[0] = new ConnectorExportSettings(7, 18, 42, 1234);
        connector.setExportSettings(3, settings);
        var file = Files.createTempFile("psitweaks-logistics-", ".dat");
        try {
            NbtIo.writeCompressed(connector.saveWithoutMetadata(), file.toFile());
            var saved = NbtIo.readCompressed(file.toFile());
            var restored = new IdeaspaceConnectorBlockEntity(connector.getBlockPos(), connector.getBlockState());
            restored.load(saved);
            helper.assertTrue(restored.inputMode() == ConnectorInputMode.DENY_LIST && restored.inputFilter(8).equals(water())
                    && restored.exportSettings(0, 0).minimumStock() == Long.MAX_VALUE
                    && restored.exportSettings(0, 0).targetStock() == Long.MAX_VALUE - 7
                    && restored.exportSettings(3, 0).equals(settings[0]), "Disk reload lost logistics settings");
            for (String key : new String[] {"InputMode", "InputFilters", "ExportMinimums", "ExportTargets", "SlotExportMinimums", "SlotExportTargets"})
                saved.remove(key);
            restored.load(saved);
            helper.assertTrue(restored.inputMode() == ConnectorInputMode.ANY && restored.inputFilter(8).equals(ConnectorResource.EMPTY)
                    && restored.exportSettings(0, 0).equals(new ConnectorExportSettings(5, 12))
                    && restored.exportSettings(3, 0).equals(new ConnectorExportSettings(7, 18)), "Legacy defaults changed old rates");
        } finally { Files.deleteIfExists(file); }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void logisticsPayloadsAreBoundedAuthorizedAndKeepTemplates(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "logistics-settings"));
        var connector = place(helper, new BlockPos(1, 1, 1), player.getUUID());
        player.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(44, player.getInventory(), connector);
        var template = new MessageConnectorTemplate(44, menu.session(), 8, water().save(), true, menu.revision());
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            template.write(buffer);
            menu.handleTemplate(player, MessageConnectorTemplate.read(buffer));
            helper.assertTrue(buffer.readableBytes() == 0 && connector.inputFilter(8).equals(water()), "Filter template payload lost fields");
            buffer.clear();
            var settings = rates();
            settings[0] = new ConnectorExportSettings(64, 5, Long.MAX_VALUE, Long.MAX_VALUE);
            var message = new MessageConnectorExportSettings(44, menu.session(), menu.revision(), -1, List.of(settings));
            message.write(buffer);
            menu.handleExportSettings(player, MessageConnectorExportSettings.read(buffer));
            helper.assertTrue(connector.exportSettings(0, 0).equals(settings[0]) && buffer.readableBytes() == 0, "Long thresholds truncated");
            long version = connector.settingsVersion();
            menu.handleTemplate(player, template); // old revision
            menu.handleAction(player, new MessageConnectorAction(44, menu.session(), menu.revision() - 1, IdeaspaceConnectorMenu.INPUT_MODE, 0, 1));
            menu.handleAction(player, new MessageConnectorAction(44, menu.session(), menu.revision(), IdeaspaceConnectorMenu.INPUT_MODE, 0, 4));
            menu.handleTemplate(player, new MessageConnectorTemplate(44, menu.session(), 0, ConnectorResource.ENERGY.save(), true, menu.revision()));
            var outsider = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "outsider"));
            menu.handleTemplate(outsider, new MessageConnectorTemplate(44, menu.session(), 0, apple().save(), true, menu.revision()));
            settings[1] = new ConnectorExportSettings(1000, 5, -1, -2);
            menu.handleExportSettings(player, new MessageConnectorExportSettings(44, menu.session(), menu.revision(), -1, List.of(settings)));
            helper.assertTrue(connector.settingsVersion() == version, "Invalid, stale or unauthorized settings changed the connector");
            var previous = new CompoundTag();
            var templates = new ListTag();
            templates.add(water().save());
            previous.put("InputFilters", templates);
            var update = new CompoundTag();
            var amounts = new ListTag(); var amount = new CompoundTag(); amount.putLong("Amount", 55); amounts.add(amount);
            update.put("InputFilters", amounts);
            var merged = IdeaspaceConnectorMenu.mergeAmounts(previous, update);
            var entry = merged.getList("InputFilters", Tag.TAG_COMPOUND).getCompound(0);
            helper.assertTrue(ConnectorResource.load(entry).equals(water()) && entry.getLong("Amount") == 55,
                    "Amount sync discarded input filter identity");
        } finally { buffer.release(); }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void itemLogisticsConservesPartialTransfersAndBatches(GameTestHelper helper) {
        var storage = new PlayerIdeaStorage();
        var source = new ItemStackHandler(3) {
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return super.extractItem(slot, simulate ? amount : Math.min(amount, 7), simulate);
            }
        };
        source.setStackInSlot(0, new ItemStack(Items.APPLE, 30));
        source.setStackInSlot(1, new ItemStack(Items.DIRT, 64));
        source.setStackInSlot(2, new ItemStack(Items.APPLE, 10));
        helper.assertTrue(IdeaStorageResourceTransfers.depositItems(storage, source, stack -> stack.is(Items.APPLE), 12) == 12
                && apple().amount(storage) == 12 && source.getStackInSlot(0).getCount() == 23
                && source.getStackInSlot(1).getCount() == 64 && source.getStackInSlot(2).getCount() == 5,
                "Deposit ignored its shared limit, actual extraction or filter");
        storage.insert(new ItemStack(Items.APPLE), 188);
        var destination = new ItemStackHandler(4);
        helper.assertTrue(IdeaStorageResourceTransfers.withdrawItems(storage, destination, stack -> true, 150) == 150
                && ConnectorTransfers.countItems(destination, apple().item()) == 150 && apple().amount(storage) == 50,
                "Spell transfer failed across multiple stacks");
        var partial = new ItemStackHandler(1) {
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (simulate) return ItemStack.EMPTY;
                int accepted = Math.min(3, stack.getCount());
                super.insertItem(slot, stack.copyWithCount(accepted), false);
                return stack.copyWithCount(stack.getCount() - accepted);
            }
        };
        helper.assertTrue(IdeaStorageResourceTransfers.withdrawItems(storage, partial, stack -> true, 50) == 3
                && apple().amount(storage) == 47, "Unaccepted items were not refunded");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void depositReservesCapacityAndReleasesOnFailure(GameTestHelper helper) {
        int old = PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.get();
        var storage = new PlayerIdeaStorage();
        try {
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(2);
            storage.insert(new ItemStack(Items.APPLE), 126);
            var source = new ItemStackHandler(1) {
                @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (!simulate) {
                        helper.assertTrue(storage.insert(new ItemStack(Items.APPLE), 100) == 0
                                && storage.extract(apple().item(), 100) == 0, "Reentrant mutation bypassed insertion reservation");
                        PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(1);
                    }
                    return super.extractItem(slot, amount, simulate);
                }
            };
            source.setStackInSlot(0, new ItemStack(Items.APPLE, 20));
            helper.assertTrue(IdeaStorageResourceTransfers.depositItems(storage, source, stack -> true, 20) == 2
                    && apple().amount(storage) == 128 && source.getStackInSlot(0).getCount() == 18,
                    "Capacity or mid-transfer config reduction lost extracted resources");
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(old);
            var failing = new ItemStackHandler(1) {
                @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (!simulate) throw new IllegalStateException("test extraction failure");
                    return new ItemStack(Items.APPLE);
                }
            };
            try { IdeaStorageResourceTransfers.depositItems(storage, failing, stack -> true, 1); }
            catch (IllegalStateException expected) { }
            helper.assertTrue(storage.insert(new ItemStack(Items.APPLE), 1) == 1, "Failed transfer left warehouse locked");
            storage.markLoadFailed();
            helper.assertTrue(IdeaStorageResourceTransfers.depositItems(storage, source, stack -> true, 20) == 0
                    && source.getStackInSlot(0).getCount() == 18, "Unread warehouse consumed external resources");
        } finally { PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(old); }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void fluidLogisticsUsesActualDrainAndRefunds(GameTestHelper helper) {
        var storage = new PlayerIdeaStorage();
        var source = new FluidTank(2000) {
            @Override public FluidStack drain(FluidStack resource, FluidAction action) {
                return super.drain(new FluidStack(resource, action.simulate() ? resource.getAmount() : Math.min(123, resource.getAmount())), action);
            }
        };
        source.setFluid(new FluidStack(Fluids.WATER, 1000));
        helper.assertTrue(IdeaStorageResourceTransfers.depositFluids(storage, source, stack -> stack.getFluid() == Fluids.LAVA, 1000) == 0,
                "Fluid deposit ignored filter");
        helper.assertTrue(IdeaStorageResourceTransfers.depositFluids(storage, source, stack -> true, 1000) == 123
                && water().amount(storage) == 123 && source.getFluidAmount() == 877, "Fluid deposit used simulated quantity");
        var target = new FluidTank(2000) {
            @Override public int fill(FluidStack resource, FluidAction action) {
                return super.fill(new FluidStack(resource, action.simulate() ? resource.getAmount() : Math.min(17, resource.getAmount())), action);
            }
        };
        helper.assertTrue(IdeaStorageResourceTransfers.withdrawFluids(storage, target, stack -> true, 1000) == 17
                && water().amount(storage) == 106 && target.getFluidAmount() == 17, "Fluid remainder was lost");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void spellsExecuteThroughFacesFiltersAndProtection(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "idea-logistics"));
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        player.setPos(Vec3.atCenterOf(pos));
        var chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(pos);
        var named = new ItemStack(Items.APPLE, 20); named.setHoverName(Component.literal("Variant"));
        chest.setItem(0, new ItemStack(Items.APPLE, 40)); chest.setItem(1, named.copy());
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        var context = new SpellContext().setPlayer(player);
        Object[] filter = {"minecraft:app*"}; double[] itemCount = {32.9};
        var deposit = trick(true, BlockResourceModes.ITEM, param -> paramValue(param, pos, itemCount[0], filter[0]));
        var withdraw = trick(false, BlockResourceModes.ITEM, param -> paramValue(param, pos, itemCount[0], filter[0]));
        deposit.execute(context);
        helper.assertTrue(apple().amount(storage) == 32 && chest.getItem(0).getCount() == 8, "Item count must be direct, rounded down and filtered");
        // The wired value picks the match; Operator: Strict Item makes it compare NBT.
        filter[0] = new StrictItemFilter(List.of(named));
        deposit.execute(context);
        helper.assertTrue(storage.simulateExtract(ItemResourceKey.of(named).orElseThrow(), 100) == 20 && chest.getItem(0).getCount() == 8,
                "Strict item filter ignored NBT");
        filter[0] = "apple"; itemCount[0] = 16; withdraw.execute(context);
        helper.assertTrue(apple().amount(storage) == 16 && chest.getItem(0).getCount() == 24, "Withdrawal endpoint or ID-path match wrong");
        Consumer<PlayerInteractEvent.RightClickBlock> deny = event -> { if (event.getPos().equals(pos)) event.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(deny);
        boolean denied = false;
        try { withdraw.execute(context); } catch (SpellRuntimeException expected) { denied = true; }
        finally { MinecraftForge.EVENT_BUS.unregister(deny); }
        helper.assertTrue(denied && apple().amount(storage) == 16, "Spell ignored protection event");
        for (double invalid : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            itemCount[0] = invalid; boolean rejected = false;
            try { withdraw.execute(context); } catch (SpellRuntimeException expected) { rejected = true; }
            helper.assertTrue(rejected, "Invalid item count accepted");
        }
        itemCount[0] = 0.5; withdraw.execute(context);
        helper.assertTrue(apple().amount(storage) == 16, "Fractional item count must round down to zero");
        itemCount[0] = 1; withdraw.execute(context);
        helper.assertTrue(apple().amount(storage) == 15 && chest.getItem(0).getCount() == 25,
                "Number 1 must withdraw exactly one item");
        deposit.execute(context);
        helper.assertTrue(apple().amount(storage) == 16 && chest.getItem(0).getCount() == 24,
                "Number 1 must deposit exactly one item");
        helper.succeed();
    }

    /** A deposit or withdraw trick in {@code mode} whose inputs read {@code values}. */
    public static PieceTrickIdeaStorageResourceBase trick(boolean deposit, PsitweaksModeOption mode,
                                                          Function<SpellParam<?>, Object> values) {
        PieceTrickIdeaStorageResourceBase piece = deposit
                ? new PieceTrickIdeaStorageDeposit(new Spell()) {
                    @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.apply(param); }
                }
                : new PieceTrickIdeaStorageWithdraw(new Spell()) {
                    @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return values.apply(param); }
                };
        piece.setModeOption(mode);
        return piece;
    }

    public static Object paramValue(SpellParam<?> param, BlockPos pos, double quantity, Object filter) {
        return switch (param.name) {
            case SpellParam.GENERIC_NAME_POSITION -> new Vector3(pos.getX(), pos.getY(), pos.getZ());
            case SpellParam.GENERIC_NAME_DIRECTION -> new Vector3(0, 1, 0);
            case SpellParam.GENERIC_NAME_POWER, SpellParam.GENERIC_NAME_NUMBER -> quantity;
            default -> filter;
        };
    }

    @GameTest(template = "connector_empty")
    public static void fluidSpellsRespectInputFiltersFacesAndOwnConnector(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "fluid-logistics"));
        var connector = place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        connector.setResource(0, water());
        connector.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 1000);
        player.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var context = new SpellContext().setPlayer(player);
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        var deposit = trick(true, BlockResourceModes.FLUID, param -> paramValue(param, connector.getBlockPos(), 0.25, "water"));
        var withdraw = trick(false, BlockResourceModes.FLUID, param -> paramValue(param, connector.getBlockPos(), 0.25, null));
        deposit.execute(context);
        helper.assertTrue(water().amount(storage) == 250 && water().amount(connector.storage()) == 750, "Fluid spell failed at specified face");
        connector.setInputMode(ConnectorInputMode.ALLOW_LIST);
        withdraw.execute(context);
        helper.assertTrue(water().amount(storage) == 250, "Fluid spell bypassed target input filter");
        connector.setInputFilter(0, water());
        withdraw.execute(context);
        helper.assertTrue(water().amount(storage) == 0 && water().amount(connector.storage()) == 1000, "Fluid spell did not supply allowed target");
        connector.setRedstoneMode(-1, Direction.UP, ConnectorRedstoneMode.HIGH);
        deposit.execute(context);
        helper.assertTrue(water().amount(storage) == 0, "Fluid spell bypassed redstone face restriction");
        // initialize() keeps an existing owner, so re-initializing the connector above would leave it foreign.
        // A connector the caster really owns, with an active face, isolates the own-connector guard.
        var own = place(helper, new BlockPos(2, 1, 1), player.getUUID());
        own.setResource(0, water());
        storage.insertFluid(new FluidStack(Fluids.WATER, 1), 600);
        var ownDeposit = trick(true, BlockResourceModes.FLUID, param -> paramValue(param, own.getBlockPos(), 0.25, "water"));
        var ownWithdraw = trick(false, BlockResourceModes.FLUID, param -> paramValue(param, own.getBlockPos(), 0.25, null));
        long version = storage.getVersion();
        ownDeposit.execute(context); ownWithdraw.execute(context);
        helper.assertTrue(storage.getVersion() == version && water().amount(storage) == 600,
                "Own-connector spell mutated the same warehouse twice");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void allPiecesRegisterAndModesPreserveFourConnections(GameTestHelper helper) throws Exception {
        for (String action : new String[] {"deposit", "withdraw"}) {
            var piece = (PieceTrickIdeaStorageResourceBase) SpellPiece.create(PsiAPI.getSpellPiece(Psitweaks.location("trick_idea_storage_" + action)), new Spell());
            helper.assertTrue(piece instanceof PsitweaksModeConfigurable && piece.getAvailableModeOptions().equals(IdeaStorageResourceModes.TRANSFER)
                            && piece.getModeOption() == BlockResourceModes.ITEM && !piece.isExactTransfer(),
                    "Spell must offer every resource kind and start as a partial item transfer");
            helper.assertTrue(piece.params.size() == 4 && piece.params.values().stream().filter(param -> param.canDisable).count() == 1
                            && piece.params.get(PsitweaksSpellParams.FILTER) instanceof ParamItemSearch,
                    "Item mode requires position, face and quantity plus the optional item search filter");
            var metadata = new SpellMetadata(); piece.addToMetadata(metadata);
            helper.assertTrue(metadata.getStat(EnumSpellStat.POTENCY) == 50 && metadata.getStat(EnumSpellStat.COST) == 100,
                    "Spell metadata differs from documented stats");
            piece.paramSides.put(piece.params.get(SpellParam.GENERIC_NAME_POSITION), SpellParam.Side.TOP);
            piece.paramSides.put(piece.params.get(SpellParam.GENERIC_NAME_DIRECTION), SpellParam.Side.BOTTOM);
            piece.paramSides.put(piece.params.get(SpellParam.GENERIC_NAME_NUMBER), SpellParam.Side.RIGHT);
            piece.paramSides.put(piece.params.get(PsitweaksSpellParams.FILTER), SpellParam.Side.LEFT);
            for (var mode : piece.getAvailableModeOptions()) {
                piece.setModeOption(mode);
                boolean item = mode == BlockResourceModes.ITEM || mode == IdeaStorageResourceModes.ITEM_EXACT;
                boolean energy = mode == BlockResourceModes.ENERGY;
                helper.assertTrue(piece.getModeOption() == mode && piece.isExactTransfer() == IdeaStorageResourceModes.isExact(mode),
                        "Mode switch did not take effect: " + mode.serializedId());
                String quantity = item ? SpellParam.GENERIC_NAME_NUMBER : SpellParam.GENERIC_NAME_POWER;
                helper.assertTrue(piece.params.containsKey(quantity)
                                && !piece.params.containsKey(item ? SpellParam.GENERIC_NAME_POWER : SpellParam.GENERIC_NAME_NUMBER),
                        "Only item transfers must use Number instead of Power: " + mode.serializedId());
                helper.assertTrue(energy ? !piece.params.containsKey(PsitweaksSpellParams.FILTER)
                                : item == (piece.params.get(PsitweaksSpellParams.FILTER) instanceof ParamItemSearch),
                        "Wrong filter for " + mode.serializedId());
                assertLogisticsConnections(helper, piece, item, !energy);
            }
            piece.setModeOption(IdeaStorageResourceModes.CHEMICAL_EXACT);
            helper.assertTrue(piece.paramSides.get(piece.params.get(PsitweaksSpellParams.FILTER)) == SpellParam.Side.OFF,
                    "Leaving FE mode must not invent a filter wire");
            piece.paramSides.put(piece.params.get(PsitweaksSpellParams.FILTER), SpellParam.Side.LEFT);
            var tag = new CompoundTag(); piece.writeToNBT(tag);
            helper.assertTrue(tag.getString("psitweaksMode").equals("resource_chemical_exact"), "Mode must save under its own ID");
            var reloaded = (PieceTrickIdeaStorageResourceBase) SpellPiece.createFromNBT(new Spell(), tag);
            helper.assertTrue(reloaded.getModeOption() == IdeaStorageResourceModes.CHEMICAL_EXACT && reloaded.isExactTransfer(),
                    "NBT lost the mode");
            assertLogisticsConnections(helper, reloaded, false, true);
            tag.putString("psitweaksMode", "unknown");
            reloaded.readFromNBT(tag);
            helper.assertTrue(reloaded.getModeOption() == BlockResourceModes.ITEM, "Unknown mode must fall back to Item");
        }
        helper.assertTrue(IdeaStorageResourceTransfers.amountForPower(1.5, 1, Integer.MAX_VALUE) == 1
                && IdeaStorageResourceTransfers.amountForPower(0.001, 1, Integer.MAX_VALUE) == 0
                && IdeaStorageResourceTransfers.amountForPower(1.5, 1000, Integer.MAX_VALUE) == 1500
                && IdeaStorageResourceTransfers.amountForPower(Double.MAX_VALUE, 1000, Long.MAX_VALUE) == Long.MAX_VALUE,
                "Resource power scaling or overflow");
        if (MekanismCompat.isMekanismLoaded()) IdeaLogisticsChemicalChecks.run(helper);
        helper.succeed();
    }

    private static void assertLogisticsConnections(GameTestHelper helper, PieceTrickIdeaStorageResourceBase piece, boolean item,
                                                   boolean filter) {
        int inputs = filter ? 4 : 3;
        helper.assertTrue(piece.params.size() == inputs && piece.paramSides.size() == inputs
                && piece.paramSides.get(piece.params.get(SpellParam.GENERIC_NAME_POSITION)) == SpellParam.Side.TOP
                && piece.paramSides.get(piece.params.get(SpellParam.GENERIC_NAME_DIRECTION)) == SpellParam.Side.BOTTOM
                && piece.paramSides.get(piece.params.get(item ? SpellParam.GENERIC_NAME_NUMBER : SpellParam.GENERIC_NAME_POWER)) == SpellParam.Side.RIGHT
                && piece.params.values().stream().filter(param -> param.canDisable).allMatch(param -> piece.paramSides.get(param) == SpellParam.Side.LEFT),
                "Mode switch or NBT reload lost or swapped connections");
    }
}
