package com.moratan251.psitweaks.common.gametest;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageTransferModes;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageChemicalFreeCapacity;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageFluidFreeCapacity;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageItemFreeCapacity;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.*;
import com.moratan251.psitweaks.common.spells.util.IdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.idea.*;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.*;

/** Last transfer amount, exact transfers and the capacity/type selectors of the ideaspace logistics. */
@GameTestHolder(Psitweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IdeaLogisticsPlusGameTests {
    static final List<String> NEW_SELECTORS = List.of("selector_idea_storage_last_transfer",
            "selector_idea_storage_item_free_capacity", "selector_idea_storage_fluid_free_capacity",
            "selector_idea_storage_chemical_free_capacity", "selector_idea_storage_energy_free_capacity",
            "selector_idea_storage_item_type_count", "selector_idea_storage_fluid_type_count",
            "selector_idea_storage_chemical_type_count");

    private static ItemStack named(int count) {
        var stack = new ItemStack(Items.APPLE, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Exact variant"));
        return stack;
    }

    private static long stored(PlayerIdeaStorage storage, ItemStack template) {
        return storage.simulateExtract(ItemResourceKey.of(template).orElseThrow(), Long.MAX_VALUE);
    }

    private static long water(PlayerIdeaStorage storage) {
        return storage.simulateExtractFluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow(), Long.MAX_VALUE);
    }

    private static double lastTransfer(SpellContext context) {
        return (Double) new PieceSelectorIdeaStorageLastTransfer(new Spell()).execute(context);
    }

    @GameTest(template = "psi110_empty")
    public static void exactItemTransfersMoveAllOrNothing(GameTestHelper helper) {
        int oldStacks = PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.get();
        int oldTypes = PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(1); // 64 apples per type
            var storage = new PlayerIdeaStorage();
            var source = new ItemStackHandler(3);
            source.setStackInSlot(0, named(3));
            source.setStackInSlot(1, new ItemStack(Items.APPLE, 6));
            source.setStackInSlot(2, new ItemStack(Items.APPLE, 4));
            long version = storage.getVersion();
            helper.assertTrue(IdeaStorageResourceTransfers.depositItemsExact(storage, source, stack -> true, 11) == 0
                            && storage.getVersion() == version && source.getStackInSlot(0).getCount() == 3
                            && source.getStackInSlot(1).getCount() == 6 && source.getStackInSlot(2).getCount() == 4,
                    "Supply shortfall must leave both sides unchanged; other components must not top it up");
            helper.assertTrue(IdeaStorageResourceTransfers.depositItemsExact(storage, source, stack -> true, 8) == 8
                            && stored(storage, new ItemStack(Items.APPLE)) == 8 && storage.itemTypeCount() == 1
                            && source.getStackInSlot(0).getCount() == 3 && source.getStackInSlot(1).isEmpty()
                            && source.getStackInSlot(2).getCount() == 2,
                    "Exact deposit must span slots of the first type that can supply everything");

            storage.insert(new ItemStack(Items.APPLE), 52); // 60 of 64
            source.setStackInSlot(1, new ItemStack(Items.APPLE, 6));
            version = storage.getVersion();
            helper.assertTrue(IdeaStorageResourceTransfers.depositItemsExact(storage, source, stack -> true, 8) == 0
                            && storage.getVersion() == version && source.getStackInSlot(1).getCount() == 6
                            && source.getStackInSlot(2).getCount() == 2,
                    "Per-type cap shortfall must leave both sides unchanged");
            PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.set(1);
            helper.assertTrue(IdeaStorageResourceTransfers.depositItemsExact(storage, source,
                            stack -> stack.has(DataComponents.CUSTOM_NAME), 3) == 0 && source.getStackInSlot(0).getCount() == 3
                            && storage.itemTypeCount() == 1, "A new type must be refused while every type slot is used");
            PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.set(oldTypes);
            helper.assertTrue(IdeaStorageResourceTransfers.depositItems(storage, source, stack -> !stack.has(DataComponents.CUSTOM_NAME), 8) == 4,
                    "Partial deposit must still move what fits");

            var target = new ItemStackHandler(1);
            target.setStackInSlot(0, new ItemStack(Items.APPLE, 60));
            version = storage.getVersion();
            helper.assertTrue(IdeaStorageResourceTransfers.withdrawItemsExact(storage, target, stack -> true, 8) == 0
                            && storage.getVersion() == version && target.getStackInSlot(0).getCount() == 60,
                    "Destination shortfall must leave both sides unchanged");
            helper.assertTrue(IdeaStorageResourceTransfers.withdrawItemsExact(storage, new ItemStackHandler(2), stack -> true, 65) == 0
                            && storage.getVersion() == version, "Stock shortfall must leave both sides unchanged");
            var mixed = new PlayerIdeaStorage();
            mixed.insert(named(1), 3);
            mixed.insert(new ItemStack(Items.APPLE), 10);
            var empty = new ItemStackHandler(2);
            helper.assertTrue(IdeaStorageResourceTransfers.withdrawItemsExact(mixed, empty, stack -> stack.is(Items.APPLE), 5) == 5
                            && empty.getStackInSlot(0).getCount() == 5 && !empty.getStackInSlot(0).has(DataComponents.CUSTOM_NAME)
                            && empty.getStackInSlot(1).isEmpty() && stored(mixed, named(1)) == 3 && stored(mixed, new ItemStack(Items.APPLE)) == 5,
                    "Exact withdrawal must skip a type that cannot cover the amount and never mix component variants");
            helper.assertTrue(IdeaStorageResourceTransfers.withdrawItemsExact(storage, target, stack -> true, 4) == 4
                    && target.getStackInSlot(0).getCount() == 64, "Exact withdrawal must fill the remaining space");

            // A handler that breaks its own simulation keeps what really moved (loss-safe, not rolled back).
            var liar = new ItemStackHandler(1) {
                @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return super.extractItem(slot, simulate ? amount : Math.min(amount, 2), simulate);
                }
            };
            liar.setStackInSlot(0, new ItemStack(Items.CARROT, 5));
            var fresh = new PlayerIdeaStorage();
            helper.assertTrue(IdeaStorageResourceTransfers.depositItemsExact(fresh, liar, stack -> true, 5) == 2
                            && stored(fresh, new ItemStack(Items.CARROT)) == 2 && liar.getStackInSlot(0).getCount() == 3,
                    "Misbehaving source must keep the actually moved items without duplication");
        } finally {
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(oldStacks);
            PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.set(oldTypes);
        }
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void exactFluidTransfersMoveAllOrNothing(GameTestHelper helper) {
        long oldPerType = PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.get();
        int oldTypes = PsitweaksConfig.COMMON.ideaStorageMaxFluidTypes.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.set(1000L);
            var storage = new PlayerIdeaStorage();
            var source = new FluidTank(4000);
            source.setFluid(new FluidStack(Fluids.WATER, 1000));
            long version = storage.getVersion();
            helper.assertTrue(IdeaStorageResourceTransfers.depositFluidsExact(storage, source, stack -> true, 1200) == 0
                    && storage.getVersion() == version && source.getFluidAmount() == 1000, "Supply shortfall changed a side");
            helper.assertTrue(IdeaStorageResourceTransfers.depositFluidsExact(storage, source, stack -> true, 700) == 700
                    && water(storage) == 700 && source.getFluidAmount() == 300, "Exact fluid deposit moved a different amount");
            source.setFluid(new FluidStack(Fluids.WATER, 1000));
            version = storage.getVersion();
            helper.assertTrue(IdeaStorageResourceTransfers.depositFluidsExact(storage, source, stack -> true, 400) == 0
                    && storage.getVersion() == version && source.getFluidAmount() == 1000, "Per-type cap shortfall changed a side");
            PsitweaksConfig.COMMON.ideaStorageMaxFluidTypes.set(1);
            var lava = new FluidTank(1000);
            lava.setFluid(new FluidStack(Fluids.LAVA, 1000));
            helper.assertTrue(IdeaStorageResourceTransfers.depositFluidsExact(storage, lava, stack -> true, 100) == 0
                    && lava.getFluidAmount() == 1000 && storage.fluidTypeCount() == 1, "A new fluid type bypassed the type limit");
            PsitweaksConfig.COMMON.ideaStorageMaxFluidTypes.set(oldTypes);
            helper.assertTrue(IdeaStorageResourceTransfers.depositFluids(storage, source, stack -> true, 400) == 300,
                    "Partial fluid deposit must still move what fits");

            var target = new FluidTank(500);
            version = storage.getVersion();
            helper.assertTrue(IdeaStorageResourceTransfers.withdrawFluidsExact(storage, target, stack -> true, 600) == 0
                    && storage.getVersion() == version && target.isEmpty(), "Destination shortfall changed a side");
            helper.assertTrue(IdeaStorageResourceTransfers.withdrawFluidsExact(storage, new FluidTank(5000), stack -> true, 1001) == 0
                    && storage.getVersion() == version, "Stock shortfall changed a side");
            helper.assertTrue(IdeaStorageResourceTransfers.withdrawFluidsExact(storage, target, stack -> true, 500) == 500
                    && target.getFluidAmount() == 500 && water(storage) == 500, "Exact fluid withdrawal moved a different amount");
            helper.assertTrue(IdeaStorageResourceTransfers.withdrawFluidsExact(storage, target, stack -> stack.getFluid() == Fluids.LAVA, 1) == 0,
                    "Exact fluid withdrawal ignored the filter");
        } finally {
            PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.set(oldPerType);
            PsitweaksConfig.COMMON.ideaStorageMaxFluidTypes.set(oldTypes);
        }
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void exactSpellsAreSilentOnShortfallAndRecordTheAmount(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "exact-logistics"));
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        player.setPos(Vec3.atCenterOf(pos));
        var chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(pos);
        chest.setItem(0, new ItemStack(Items.APPLE, 30));
        chest.setItem(1, new ItemStack(Items.APPLE, 10));
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        var context = new SpellContext().setPlayer(player);
        double[] count = {41};
        var deposit = new PieceTrickIdeaStorageDepositItem(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, pos, count[0], "apple");
            }
        };
        deposit.setModeOption(IdeaStorageTransferModes.STRING_EXACT);
        helper.assertTrue(deposit.isExactTransfer() && deposit.params.containsKey("psitweaks.spellparam.string"),
                "String / Exact must keep the String filter");
        deposit.execute(context);
        helper.assertTrue(stored(storage, new ItemStack(Items.APPLE)) == 0 && chest.getItem(0).getCount() == 30
                && chest.getItem(1).getCount() == 10 && lastTransfer(context) == 0.0D, "Exact shortfall must be a silent no-op");
        count[0] = 35;
        deposit.execute(context);
        helper.assertTrue(stored(storage, new ItemStack(Items.APPLE)) == 35 && lastTransfer(context) == 35.0D
                && chest.getItem(0).isEmpty() && chest.getItem(1).getCount() == 5, "Exact deposit must move every requested item");
        deposit.setModeOption(PsitweaksModeOptions.STRING);
        count[0] = 41;
        deposit.execute(context);
        helper.assertTrue(stored(storage, new ItemStack(Items.APPLE)) == 40 && lastTransfer(context) == 5.0D,
                "Partial mode must keep moving what is available");

        var connector = IdeaLogisticsGameTests.place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
        connector.setResource(0, IdeaLogisticsGameTests.water());
        connector.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 1500);
        double[] strength = {2};
        var fluid = new PieceTrickIdeaStorageDepositFluid(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, connector.getBlockPos(), strength[0], "water");
            }
        };
        fluid.setModeOption(IdeaStorageTransferModes.EXACT);
        fluid.execute(context);
        helper.assertTrue(water(storage) == 0 && water(connector.storage()) == 1500 && lastTransfer(context) == 0.0D,
                "Exact fluid shortfall must be a silent no-op");
        strength[0] = 1.5;
        fluid.execute(context);
        helper.assertTrue(water(storage) == 1500 && water(connector.storage()) == 0 && lastTransfer(context) == 1500.0D,
                "Exact fluid deposit must move Power * 1000 mB");
        helper.succeed();
    }

    @GameTest(template = "connector_empty")
    public static void lastTransferTracksEveryTransferTrickPerCast(GameTestHelper helper) throws Exception {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "last-transfer"));
        BlockPos chestPos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.CHEST);
        player.setPos(Vec3.atCenterOf(chestPos));
        var chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.APPLE, 20));
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        var context = new SpellContext().setPlayer(player);
        helper.assertTrue(lastTransfer(context) == 0.0D, "Nothing has moved yet in this cast");
        double[] requested = {12};
        var depositItem = new PieceTrickIdeaStorageDepositItem(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, chestPos, requested[0], null);
            }
        };
        var withdrawItem = new PieceTrickIdeaStorageWithdrawItem(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, chestPos, requested[0], null);
            }
        };
        depositItem.execute(context);
        helper.assertTrue(lastTransfer(context) == 12.0D, "Item deposit not recorded");
        requested[0] = 5;
        withdrawItem.execute(context);
        helper.assertTrue(lastTransfer(context) == 5.0D && stored(storage, new ItemStack(Items.APPLE)) == 7, "Item withdrawal not recorded");
        helper.assertTrue(lastTransfer(new SpellContext().setPlayer(player)) == 0.0D, "Another cast must not see this cast's value");
        requested[0] = 50;
        withdrawItem.execute(context);
        helper.assertTrue(lastTransfer(context) == 7.0D, "Partial withdrawal must record the actual amount, not the request");
        withdrawItem.execute(context);
        helper.assertTrue(lastTransfer(context) == 0.0D, "A trick that moved nothing must reset the value to 0");
        requested[0] = 3;
        depositItem.execute(context);
        requested[0] = -1;
        boolean rejected = false;
        try { depositItem.execute(context); } catch (SpellRuntimeException expected) { rejected = true; }
        helper.assertTrue(rejected && lastTransfer(context) == 0.0D, "A trick stopped by an error must record 0");

        var connector = IdeaLogisticsGameTests.place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
        connector.setResource(0, IdeaLogisticsGameTests.water());
        connector.setResource(1, ConnectorResource.ENERGY);
        connector.storage().insertFluid(new FluidStack(Fluids.WATER, 1), 2000);
        connector.storage().insertEnergy(10_000, false);
        double[] strength = {0.25};
        var depositFluid = new PieceTrickIdeaStorageDepositFluid(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, connector.getBlockPos(), strength[0], null);
            }
        };
        depositFluid.execute(context);
        helper.assertTrue(lastTransfer(context) == 250.0D, "Fluid deposit must record mB");
        var withdrawFluid = new PieceTrickIdeaStorageWithdrawFluid(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, connector.getBlockPos(), strength[0] / 5, null);
            }
        };
        withdrawFluid.execute(context);
        helper.assertTrue(lastTransfer(context) == 50.0D && water(storage) == 200, "Fluid withdrawal must record mB");
        var absorb = new PieceTrickIdeaStorageAbsorbFE(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, connector.getBlockPos(), strength[0], null);
            }
        };
        absorb.execute(context);
        helper.assertTrue(lastTransfer(context) == 1000.0D && storage.energy() == 1000, "FE absorb must record FE");
        var supply = new PieceTrickIdeaStorageSupplyFE(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, connector.getBlockPos(), 1, null);
            }
        };
        supply.execute(context);
        helper.assertTrue(lastTransfer(context) == 1000.0D && storage.energy() == 0, "FE supply must record the FE actually sent");

        var caster = IdeaStorageSlotGameTests.caster(helper, false, true);
        var slotContext = new SpellContext().setPlayer(caster);
        var slotStorage = IdeaStorageService.get(caster.server, caster.getUUID());
        caster.getInventory().setItem(1, new ItemStack(Items.APPLE, 9));
        double[] count = {64}; Object[] filter = {null};
        PieceTrickIdeaStorageSlotBase depositSlot = IdeaStorageSlotGameTests.piece(true, count, filter);
        depositSlot.execute(slotContext);
        helper.assertTrue(lastTransfer(slotContext) == 9.0D && stored(slotStorage, new ItemStack(Items.APPLE)) == 9,
                "Slot deposit must record the items moved");
        count[0] = 4;
        PieceTrickIdeaStorageSlotBase withdrawSlot = IdeaStorageSlotGameTests.piece(false, count, filter);
        filter[0] = "apple";
        withdrawSlot.execute(slotContext);
        helper.assertTrue(lastTransfer(slotContext) == 4.0D && caster.getInventory().getItem(1).getCount() == 4,
                "Slot withdrawal must record the items moved");
        helper.assertTrue(context.customData.get(IdeaStorageLastTransfer.CONTEXT_KEY) instanceof Long,
                "The value lives in the cast's own context data");
        helper.succeed();
    }

    @GameTest(template = "psi110_empty")
    public static void modesSaveSeparatelyAndLegacySpellsStayPartial(GameTestHelper helper) {
        for (String action : new String[] {"deposit", "withdraw"}) for (String type : new String[] {"item", "fluid", "chemical"}) {
            var id = Psitweaks.location("trick_idea_storage_" + action + "_" + type);
            boolean item = type.equals("item");
            String quantity = item ? "_number" : "_power";
            // Exactly what the previous release saved: no transfer-mode key, filter mode only on item spells.
            var legacy = new CompoundTag();
            legacy.putString("key", id.toString());
            var params = new CompoundTag();
            params.putInt("_position", SpellParam.Side.TOP.asInt());
            params.putInt("_direction", SpellParam.Side.BOTTOM.asInt());
            params.putInt(quantity, SpellParam.Side.RIGHT.asInt());
            params.putInt(item ? "psitweaks.spellparam.item" : "psitweaks.spellparam.string", SpellParam.Side.LEFT.asInt());
            legacy.put("params", params);
            if (item) legacy.putString("psitweaksMode", PsitweaksModeOptions.ITEM_STRICT.serializedId());
            var piece = (PieceTrickIdeaStorageResourceBase) SpellPiece.createFromNBT(new Spell(), legacy);
            helper.assertTrue(piece != null && !piece.isExactTransfer()
                            && piece.getModeOption().id().equals(item ? PsitweaksModeOptions.ITEM_STRICT.id() : IdeaStorageTransferModes.PARTIAL.id()),
                    "Legacy spell must load as a partial transfer: " + id);
            assertWiring(helper, piece, item, item ? "psitweaks.spellparam.item" : "psitweaks.spellparam.string");
            var resaved = new CompoundTag();
            piece.writeToNBT(resaved);
            helper.assertTrue(!resaved.contains(IdeaStorageTransferModes.TAG)
                            && resaved.getString("psitweaksMode").equals(item ? PsitweaksModeOptions.ITEM_STRICT.serializedId() : ""),
                    "Partial transfer must save exactly like the previous release: " + id);

            piece.setModeOption(item ? IdeaStorageTransferModes.ITEM_STRICT_EXACT : IdeaStorageTransferModes.EXACT);
            assertWiring(helper, piece, item, item ? "psitweaks.spellparam.item" : "psitweaks.spellparam.string");
            var exact = new CompoundTag();
            piece.writeToNBT(exact);
            helper.assertTrue("exact".equals(exact.getString(IdeaStorageTransferModes.TAG))
                            && exact.getString("psitweaksMode").equals(item ? PsitweaksModeOptions.ITEM_STRICT.serializedId() : ""),
                    "Exact transfer must be saved apart from the unchanged filter mode: " + id);
            var reloaded = (PieceTrickIdeaStorageResourceBase) SpellPiece.createFromNBT(new Spell(), exact);
            helper.assertTrue(reloaded.isExactTransfer() && reloaded.getModeOption().id().equals(piece.getModeOption().id()),
                    "Exact transfer lost on reload: " + id);
            assertWiring(helper, reloaded, item, item ? "psitweaks.spellparam.item" : "psitweaks.spellparam.string");
            reloaded.setModeOption(item ? PsitweaksModeOptions.ITEM_STRICT : IdeaStorageTransferModes.PARTIAL);
            var partialAgain = new CompoundTag();
            reloaded.writeToNBT(partialAgain);
            helper.assertTrue(!partialAgain.contains(IdeaStorageTransferModes.TAG) && !reloaded.isExactTransfer(),
                    "Switching back must drop the exact flag: " + id);
        }
        for (String action : new String[] {"deposit", "withdraw"}) {
            var slot = (PieceTrickIdeaStorageSlotBase) PsiAPI.SPELL_PIECE_REGISTRY
                    .get(Psitweaks.location("trick_idea_storage_" + action + "_slot")).create(new Spell());
            helper.assertTrue(slot.getAvailableModeOptions().equals(IdeaStorageTransferModes.ITEM_FILTERS),
                    "Slot spells must keep only the filter modes");
            slot.setModeOption(IdeaStorageTransferModes.ITEM_EXACT);
            var tag = new CompoundTag();
            tag.putString(IdeaStorageTransferModes.TAG, "exact");
            slot.readFromNBT(tag);
            var saved = new CompoundTag();
            slot.writeToNBT(saved);
            helper.assertTrue(!slot.isExactTransfer() && !saved.contains(IdeaStorageTransferModes.TAG),
                    "Slot spells must never become exact");
        }
        helper.succeed();
    }

    private static void assertWiring(GameTestHelper helper, PieceTrickIdeaStorageResourceBase piece, boolean item, String filter) {
        String quantity = item ? "psi.spellparam.number" : "psi.spellparam.power";
        helper.assertTrue(piece.params.size() == 4 && piece.params.containsKey(filter)
                        && piece.paramSides.get(piece.params.get("psi.spellparam.position")) == SpellParam.Side.TOP
                        && piece.paramSides.get(piece.params.get("psi.spellparam.direction")) == SpellParam.Side.BOTTOM
                        && piece.paramSides.get(piece.params.get(quantity)) == SpellParam.Side.RIGHT
                        && piece.paramSides.get(piece.params.get(filter)) == SpellParam.Side.LEFT,
                "Wiring lost for " + piece.getClass().getSimpleName());
    }

    @GameTest(template = "psi110_empty")
    public static void capacityAndTypeSelectorsFollowStorageLimits(GameTestHelper helper) throws Exception {
        int oldStacks = PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.get();
        int oldItemTypes = PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.get();
        int oldFluidTypes = PsitweaksConfig.COMMON.ideaStorageMaxFluidTypes.get();
        long oldFluidPerType = PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.get();
        long oldEnergy = PsitweaksConfig.COMMON.ideaStorageMaxEnergy.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(1);
            PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.set(2);
            PsitweaksConfig.COMMON.ideaStorageMaxFluidTypes.set(1);
            PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.set(1000L);
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(5000L);
            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "capacity-reader"));
            var context = new SpellContext().setPlayer(player);
            var storage = IdeaStorageService.get(player.server, player.getUUID());
            Object[] input = {"minecraft:apple"};
            var item = new PieceSelectorIdeaStorageItemFreeCapacity(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return input[0]; }
            };
            var fluid = new PieceSelectorIdeaStorageFluidFreeCapacity(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return input[0]; }
            };
            var chemical = new PieceSelectorIdeaStorageChemicalFreeCapacity(new Spell()) {
                @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) { return input[0]; }
            };
            helper.assertTrue(item.execute(context).equals(64.0D), "Empty storage must offer the whole per-type limit");
            storage.insert(new ItemStack(Items.APPLE), 10);
            helper.assertTrue(item.execute(context).equals(54.0D), "Free capacity must subtract the stock");
            input[0] = "minecraft:ender_pearl";
            helper.assertTrue(item.execute(context).equals(16.0D), "Per-type limit follows the item's max stack size");
            storage.insert(new ItemStack(Items.CARROT), 1);
            helper.assertTrue(item.execute(context).equals(0.0D), "A new type must report 0 while every type slot is used");
            input[0] = "minecraft:carrot";
            helper.assertTrue(item.execute(context).equals(63.0D), "Existing types keep their own free capacity");
            for (String invalid : new String[] {"minecraft:air", "missing:item", "INVALID ID", ""}) {
                input[0] = invalid;
                helper.assertTrue(item.execute(context).equals(0.0D), "Expected zero for " + invalid);
            }
            item.setModeOption(PsitweaksModeOptions.ITEM);
            helper.assertTrue(item.params.values().iterator().next() instanceof ParamSpellItemValue, "Item mode must take an Item");
            input[0] = SpellItemValue.snapshot(new ItemStack(Items.APPLE, 30));
            helper.assertTrue(item.execute(context).equals(54.0D), "Item input count must not matter");
            input[0] = SpellItemValue.snapshot(named(1));
            helper.assertTrue(item.execute(context).equals(0.0D), "Item input keeps components, so this variant is a new type");
            input[0] = SpellItemValue.EMPTY;
            helper.assertTrue(item.execute(context).equals(0.0D), "Empty Item must return zero");

            storage.insertFluid(new FluidStack(Fluids.WATER, 1), 300);
            input[0] = "minecraft:water";
            helper.assertTrue(fluid.execute(context).equals(700.0D), "Fluid free capacity must be in mB");
            input[0] = "minecraft:lava";
            helper.assertTrue(fluid.execute(context).equals(0.0D), "A new fluid type must report 0 while the type limit is reached");
            for (String invalid : new String[] {"minecraft:empty", "missing:fluid", "INVALID ID"}) {
                input[0] = invalid;
                helper.assertTrue(fluid.execute(context).equals(0.0D), "Expected zero for " + invalid);
            }
            input[0] = "mekanism:hydrogen";
            double hydrogen = (Double) chemical.execute(context);
            helper.assertTrue(MekanismCompat.isMekanismLoaded()
                            ? hydrogen == (double) PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get() : hydrogen == 0.0D,
                    "Chemical free capacity needs Mekanism and then offers the per-type limit");
            input[0] = "minecraft:stone";
            helper.assertTrue(chemical.execute(context).equals(0.0D), "Unknown chemical must return zero");

            storage.insertEnergy(1234, false);
            helper.assertTrue(selector("selector_idea_storage_energy_free_capacity").execute(context).equals(3766.0D),
                    "FE free capacity must be capacity minus stored FE");
            helper.assertTrue(selector("selector_idea_storage_item_type_count").execute(context).equals(2.0D)
                            && selector("selector_idea_storage_fluid_type_count").execute(context).equals(1.0D)
                            && selector("selector_idea_storage_chemical_type_count").execute(context).equals(0.0D),
                    "Type counts must match the stored types");
            storage.insert(named(1), 1);
            helper.assertTrue(storage.itemTypeCount() == 2, "Type limit must hold");
            helper.assertTrue(selector("selector_idea_storage_item_type_count").execute(new SpellContext()).equals(0.0D)
                            && item.execute(new SpellContext()).equals(0.0D), "Missing caster must return zero");
            long version = storage.getVersion();
            for (String id : NEW_SELECTORS) {
                if (!id.contains("free_capacity") || id.contains("energy")) selector(id).execute(context);
            }
            helper.assertTrue(storage.getVersion() == version, "Reading must not change the storage");
            storage.markLoadFailed();
            input[0] = "minecraft:carrot";
            item.setModeOption(PsitweaksModeOptions.STRING);
            helper.assertTrue(item.execute(context).equals(0.0D)
                    && selector("selector_idea_storage_energy_free_capacity").execute(context).equals(0.0D)
                    && selector("selector_idea_storage_item_type_count").execute(context).equals(0.0D),
                    "Unread storage must return zero");
        } finally {
            PsitweaksConfig.COMMON.ideaStorageItemStacksPerType.set(oldStacks);
            PsitweaksConfig.COMMON.ideaStorageMaxItemTypes.set(oldItemTypes);
            PsitweaksConfig.COMMON.ideaStorageMaxFluidTypes.set(oldFluidTypes);
            PsitweaksConfig.COMMON.ideaStorageMaxFluidPerType.set(oldFluidPerType);
            PsitweaksConfig.COMMON.ideaStorageMaxEnergy.set(oldEnergy);
        }
        helper.succeed();
    }

    private static SpellPiece selector(String id) {
        return PsiAPI.SPELL_PIECE_REGISTRY.get(Psitweaks.location(id)).create(new Spell());
    }

    @GameTest(template = "psi110_empty")
    public static void newSelectorsAreFreeAndSharedResearch(GameTestHelper helper) throws Exception {
        for (String id : NEW_SELECTORS) {
            var piece = selector(id);
            boolean resource = id.contains("item_free") || id.contains("fluid_free") || id.contains("chemical_free");
            helper.assertTrue(piece.getEvaluationType() == Double.class, "Selector must return Number: " + id);
            helper.assertTrue(resource ? piece.params.size() == 1 && piece.params.values().iterator().next() instanceof ParamString
                    && !piece.params.values().iterator().next().canDisable : piece.params.isEmpty(), "Unexpected inputs: " + id);
            helper.assertTrue((piece instanceof PsitweaksModeConfigurable) == id.contains("item_free"),
                    "Only the item capacity selector switches String/Item: " + id);
            var metadata = new SpellMetadata();
            metadata.setStat(EnumSpellStat.COMPLEXITY, 7);
            metadata.setStat(EnumSpellStat.POTENCY, 15);
            metadata.setStat(EnumSpellStat.COST, 20);
            piece.addToMetadata(metadata);
            helper.assertTrue(metadata.getStat(EnumSpellStat.COMPLEXITY) == 8 && metadata.getStat(EnumSpellStat.POTENCY) == 15
                    && metadata.getStat(EnumSpellStat.COST) == 20, "Expected Complexity +1, Potency/Cost +0: " + id);
            helper.assertTrue(PsiAPI.getPieceGroup(helper.getLevel().registryAccess(), Psitweaks.location(id)).orElseThrow()
                    .key().location().equals(Psitweaks.location("idea_storage")), "Selector must be researched with Ideaspace Storage: " + id);
        }
        var item = (PieceSelectorIdeaStorageItemFreeCapacity) selector("selector_idea_storage_item_free_capacity");
        item.paramSides.replaceAll((param, side) -> SpellParam.Side.LEFT);
        item.setModeOption(PsitweaksModeOptions.ITEM);
        var tag = new CompoundTag();
        item.writeToNBT(tag);
        var reloaded = (PieceSelectorIdeaStorageItemFreeCapacity) SpellPiece.createFromNBT(new Spell(), tag);
        helper.assertTrue(reloaded.getModeOption().equals(PsitweaksModeOptions.ITEM)
                && reloaded.params.values().iterator().next() instanceof ParamSpellItemValue
                && reloaded.paramSides.values().iterator().next() == SpellParam.Side.LEFT, "Item mode or its wire lost on reload");
        helper.succeed();
    }
}
