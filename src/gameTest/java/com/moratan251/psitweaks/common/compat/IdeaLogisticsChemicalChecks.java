package com.moratan251.psitweaks.common.compat;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.gametest.IdeaLogisticsGameTests;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageResourceModes;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.storage.connector.ConnectorCapabilities;
import com.moratan251.psitweaks.common.storage.connector.ConnectorExportSettings;
import com.moratan251.psitweaks.common.storage.connector.ConnectorInputMode;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorTransfers;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

/**
 * Mekanism 10 half of {@code IdeaLogisticsGameTests}; kept out of the GameTest scanner and only invoked behind
 * {@link MekanismCompat#isMekanismLoaded()}. Warehouse/connector keys are kind-prefixed ({@code mekanism:gas/hydrogen});
 * spell filters see the plain registry ID ({@code mekanism:hydrogen}).
 */
public final class IdeaLogisticsChemicalChecks {
    private IdeaLogisticsChemicalChecks() { }

    private static GasStack gas(ResourceLocation plainId, long amount) {
        return new GasStack(MekanismAPI.gasRegistry().getValue(plainId), amount);
    }

    public static void run(GameTestHelper helper) throws Exception {
        var hydrogenId = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
        var oxygenId = ResourceLocation.fromNamespaceAndPath("mekanism", "oxygen");
        var hydrogen = IdeaStorageMekanismIntegration.key("gas", hydrogenId);
        var oxygen = IdeaStorageMekanismIntegration.key("gas", oxygenId);
        var source = IdeaLogisticsGameTests.place(helper, new BlockPos(1, 1, 1), UUID.randomUUID());
        var target = IdeaLogisticsGameTests.place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
        source.setResource(0, ConnectorResource.chemical(hydrogen));
        target.setResource(0, ConnectorResource.chemical(hydrogen));
        source.storage().insertChemical(hydrogen, 1000);
        target.storage().insertChemical(hydrogen, 250);
        var settings = IdeaLogisticsGameTests.rates();
        settings[2] = new ConnectorExportSettings(1000, 5, 1000, 333);
        source.setExportSettings(-1, settings);
        source.setAutomatic(Direction.EAST, true);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(source.storage().simulateExtractChemical(hydrogen, 2000) == 917
                && target.storage().simulateExtractChemical(hydrogen, 2000) == 333, "Chemical stock gate/target lost or overshot quantity");
        // Forge equivalent of the cached NeoForge block capability: keep the handler instance across setting changes.
        IGasHandler cached = target.getCapability(Capabilities.GAS_HANDLER, Direction.WEST)
                .orElseThrow(() -> new AssertionError("Missing gas capability"));
        target.setInputMode(ConnectorInputMode.ALLOW_LIST);
        target.setInputFilter(8, ConnectorResource.chemical(oxygen));
        helper.assertTrue(!cached.insertChemical(gas(hydrogenId, 10), Action.EXECUTE).isEmpty()
                && cached.insertChemical(gas(oxygenId, 10), Action.EXECUTE).isEmpty(),
                "Cached chemical input bypassed list through unpublished tank");
        target.setInputMode(ConnectorInputMode.EXISTING);
        helper.assertTrue(cached.insertChemical(gas(hydrogenId, 10), Action.SIMULATE).isEmpty(), "Existing chemical rejected");
        target.storage().extractChemical(hydrogen, 333);
        helper.assertTrue(!cached.insertChemical(gas(hydrogenId, 10), Action.SIMULATE).isEmpty(), "Zero chemical stock accepted");

        var storage = new PlayerIdeaStorage();
        long[] amount = {4_000_000_123L};
        long old = PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get();
        IGasHandler handler = new IGasHandler() {
            @Override public int getTanks() { return 1; }
            @Override public GasStack getChemicalInTank(int tank) { return gas(hydrogenId, amount[0]); }
            @Override public void setChemicalInTank(int tank, GasStack stack) { throw new UnsupportedOperationException(); }
            @Override public long getTankCapacity(int tank) { return Long.MAX_VALUE; }
            @Override public boolean isValid(int tank, GasStack stack) { return true; }
            @Override public GasStack insertChemical(int tank, GasStack stack, Action action) {
                long accepted = action.simulate() ? stack.getAmount() : Math.min(37, stack.getAmount());
                if (action.execute()) amount[0] += accepted;
                return new GasStack(stack, stack.getAmount() - accepted);
            }
            @Override public GasStack extractChemical(int tank, long maximum, Action action) {
                long extracted = Math.min(maximum, amount[0]);
                if (action.execute()) {
                    extracted = Math.min(extracted, 3_000_000_123L);
                    helper.assertTrue(storage.insertChemical(oxygen, 1) == 0, "Chemical reservation allowed reentrant insertion");
                    PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(1L);
                    amount[0] -= extracted;
                }
                return gas(hydrogenId, extracted);
            }
        };
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(Long.MAX_VALUE);
            // 1.21.1 called a handler-level IdeaStorageLogisticsMekanism.transfer; on Mekanism 10 the per-kind
            // handler paths are IdeaStorageLogisticsMekanism.deposit (spell deposit) and ConnectorMekanism.pushKind (withdrawal).
            helper.assertTrue(IdeaStorageLogisticsMekanism.deposit(storage, handler, "gas", oxygenId::equals, Long.MAX_VALUE) == 0,
                    "Chemical spell filter ignored");
            helper.assertTrue(IdeaStorageLogisticsMekanism.deposit(storage, handler, "gas", hydrogenId::equals, Long.MAX_VALUE) == 3_000_000_123L
                    && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 3_000_000_123L && amount[0] == 1_000_000_000L,
                    "Long chemical deposit used simulated quantity or lost resources after config change");
            helper.assertTrue(ConnectorMekanism.pushKind(storage, hydrogen, handler, Long.MAX_VALUE, MekanismAPI.gasRegistry(), GasStack::new) == 37
                    && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 3_000_000_086L && amount[0] == 1_000_000_037L,
                    "Partial chemical withdrawal did not refund under reduced capacity");
        } finally { PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(old); }

        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "chemical-logistics"));
        player.setPos(Vec3.atCenterOf(source.getBlockPos()));
        var context = new SpellContext().setPlayer(player);
        var warehouse = IdeaStorageService.get(player.server, player.getUUID());
        var deposit = IdeaLogisticsGameTests.trick(true, BlockResourceModes.CHEMICAL, param -> IdeaLogisticsGameTests.paramValue(param, source.getBlockPos(), 0.125, "mekanism:hydro*"));
        var withdraw = IdeaLogisticsGameTests.trick(false, BlockResourceModes.CHEMICAL, param -> IdeaLogisticsGameTests.paramValue(param, source.getBlockPos(), 0.1, null));
        deposit.execute(context);
        helper.assertTrue(warehouse.simulateExtractChemical(hydrogen, 1000) == 125, "Chemical spell did not read its endpoint/filter/power");
        withdraw.execute(context);
        helper.assertTrue(warehouse.simulateExtractChemical(hydrogen, 1000) == 25
                && source.storage().simulateExtractChemical(hydrogen, 1000) == 892, "Chemical spell withdrawal lost material");
        exact(helper, hydrogenId, oxygenId, hydrogen);
        double[] power = {1};
        var exactDeposit = IdeaLogisticsGameTests.trick(true, BlockResourceModes.CHEMICAL, param -> IdeaLogisticsGameTests.paramValue(param, source.getBlockPos(), power[0], "mekanism:hydrogen"));
        exactDeposit.setModeOption(IdeaStorageResourceModes.CHEMICAL_EXACT);
        var lastTransfer = new PieceSelectorIdeaStorageLastTransfer(new Spell());
        exactDeposit.execute(context);
        helper.assertTrue(warehouse.simulateExtractChemical(hydrogen, 1000) == 25 && source.storage().simulateExtractChemical(hydrogen, 1000) == 892
                && lastTransfer.execute(context).equals(0.0D), "Exact chemical shortfall must be a silent no-op");
        power[0] = 0.5;
        exactDeposit.execute(context);
        helper.assertTrue(warehouse.simulateExtractChemical(hydrogen, 1000) == 525 && source.storage().simulateExtractChemical(hydrogen, 1000) == 392
                && lastTransfer.execute(context).equals(500.0D), "Exact chemical deposit must move Power * 1000 mB");
    }

    /** One chemical type, all or nothing; tanks holding the same chemical add up on the source side. */
    private static void exact(GameTestHelper helper, ResourceLocation hydrogenId, ResourceLocation oxygenId, ResourceLocation hydrogen) {
        long[] tanks = {300, 300, 100};
        ResourceLocation[] contents = {hydrogenId, hydrogenId, oxygenId};
        IGasHandler device = new IGasHandler() {
            @Override public int getTanks() { return tanks.length; }
            @Override public GasStack getChemicalInTank(int tank) { return gas(contents[tank], tanks[tank]); }
            @Override public void setChemicalInTank(int tank, GasStack stack) { throw new UnsupportedOperationException(); }
            @Override public long getTankCapacity(int tank) { return 1000; }
            @Override public boolean isValid(int tank, GasStack stack) { return contents[tank].equals(stack.getTypeRegistryName()); }
            @Override public GasStack insertChemical(int tank, GasStack stack, Action action) {
                if (stack.isEmpty() || !isValid(tank, stack)) return stack;
                long accepted = Math.min(stack.getAmount(), 1000 - tanks[tank]);
                if (action.execute()) tanks[tank] += accepted;
                return new GasStack(stack, stack.getAmount() - accepted);
            }
            @Override public GasStack extractChemical(int tank, long maximum, Action action) {
                long extracted = Math.min(maximum, tanks[tank]);
                if (action.execute()) tanks[tank] -= extracted;
                return extracted <= 0 ? GasStack.EMPTY : gas(contents[tank], extracted);
            }
        };
        // Mekanism 10 keeps one handler per chemical kind, so the deposit half is checked on the gas handler directly.
        var storage = new PlayerIdeaStorage();
        helper.assertTrue(IdeaStorageLogisticsMekanism.depositExact(storage, device, "gas", id -> true, 700) == 0
                && tanks[0] == 300 && tanks[1] == 300 && tanks[2] == 100 && storage.chemicalTypeCount() == 0,
                "Exact chemical supply shortfall changed a side");
        helper.assertTrue(IdeaStorageLogisticsMekanism.depositExact(storage, device, "gas", id -> true, 500) == 500
                && tanks[0] == 0 && tanks[1] == 100 && tanks[2] == 100 && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 500,
                "Exact chemical deposit must add up tanks of one chemical");
        helper.assertTrue(IdeaStorageLogisticsMekanism.depositExact(storage, device, "gas", id -> true, 150) == 0
                && tanks[1] == 100 && tanks[2] == 100, "Exact chemical deposit must not combine different chemicals");
        int oldTypes = PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.get();
        long oldPerType = PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.set(1);
            helper.assertTrue(IdeaStorageLogisticsMekanism.depositExact(storage, device, "gas", oxygenId::equals, 100) == 0
                    && tanks[2] == 100, "A new chemical type bypassed the type limit");
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.set(oldTypes);
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(550L);
            helper.assertTrue(IdeaStorageLogisticsMekanism.depositExact(storage, device, "gas", hydrogenId::equals, 100) == 0
                    && tanks[1] == 100, "Per-type cap shortfall changed a side");
        } finally {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.set(oldTypes);
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(oldPerType);
        }
        // The supply half resolves its target through the world on 1.20.1. A Basic Chemical Tank (input on every face
        // but the front) filled to 400 mB below capacity plays the device's limited tanks; checkConnectorTarget covers
        // an Ideaspace Connector, whose tanks share one storage.
        var level = helper.getLevel();
        var at = helper.absolutePos(new BlockPos(3, 1, 1));
        level.setBlockAndUpdate(at, MekanismBlocks.BASIC_CHEMICAL_TANK.getBlock().defaultBlockState());
        IGasHandler tank = ConnectorCapabilities.get(level, at, Direction.UP, Capabilities.GAS_HANDLER);
        helper.assertTrue(tank != null && tank.getTanks() > 0, "The chemical tank has no input on its top face");
        long room = 400;
        long capacity = tank.getTankCapacity(0);
        helper.assertTrue(tank.insertChemical(gas(hydrogenId, capacity - room), Action.EXECUTE).isEmpty(),
                "Could not prefill the chemical tank");
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, level, at, Direction.UP, hydrogenId::equals, 501, false) == 0
                && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 500, "Exact chemical stock shortfall changed a side");
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, level, at, Direction.UP, hydrogenId::equals, 500, false) == 0
                && tank.getChemicalInTank(0).getAmount() == capacity - room
                && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 500,
                "Exact chemical destination shortfall changed a side");
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, level, at, Direction.UP, hydrogenId::equals, room, false) == room
                && tank.getChemicalInTank(0).getAmount() == capacity
                && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 500 - room,
                "Exact chemical withdrawal must move the whole amount");
    }

    /** Exact withdrawal into another player's connector whose storage has room for 400 of the 500 mB. */
    public static void checkConnectorTarget(GameTestHelper helper) {
        var hydrogenId = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
        var hydrogen = IdeaStorageMekanismIntegration.key("gas", hydrogenId);
        long oldPerType = PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(1000L);
            var connector = IdeaLogisticsGameTests.place(helper, new BlockPos(2, 1, 1), UUID.randomUUID());
            connector.storage().insertChemical(hydrogen, 600);
            var level = helper.getLevel();
            var at = connector.getBlockPos();
            IGasHandler handler = ConnectorCapabilities.get(level, at, Direction.UP, Capabilities.GAS_HANDLER);
            helper.assertTrue(handler != null && handler.insertChemical(gas(hydrogenId, 500), Action.SIMULATE).getAmount() == 100,
                    "Connector bulk chemical simulation counted the shared storage once per tank");
            var storage = new PlayerIdeaStorage();
            storage.insertChemical(hydrogen, 500);
            for (boolean published : new boolean[] {false, true}) {
                if (published) connector.setResource(0, ConnectorResource.chemical(hydrogen));
                helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, level, at, Direction.UP, hydrogenId::equals, 500, false) == 0
                                && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 500
                                && connector.storage().simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 600,
                        "Exact chemical withdrawal into a connector moved part of the amount (published=" + published + ")");
            }
            helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, level, at, Direction.UP, hydrogenId::equals, 400, false) == 400
                            && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 100
                            && connector.storage().simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 1000,
                    "Exact chemical withdrawal into a connector must fill the remaining space");
        } finally {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(oldPerType);
        }
    }
}
