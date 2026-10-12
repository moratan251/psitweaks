package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.moratan251.psitweaks.common.compat.IdeaStorageLogisticsMekanism;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageTransferModes;
import com.moratan251.psitweaks.common.spells.spellpiece.selector.PieceSelectorIdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.storage.connector.*;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageDepositChemical;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageWithdrawChemical;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import vazkii.psi.api.spell.*;

/** Intentionally outside the GameTest scanner: loaded only when Mekanism is installed. */
final class IdeaLogisticsChemicalChecks {
    static void run(GameTestHelper helper) throws Exception {
        var hydrogen = ResourceLocation.parse("mekanism:hydrogen");
        var oxygen = ResourceLocation.parse("mekanism:oxygen");
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
        var cached = helper.getLevel().getCapability(Capabilities.CHEMICAL.block(), target.getBlockPos(), Direction.WEST);
        target.setInputMode(ConnectorInputMode.ALLOW_LIST);
        target.setInputFilter(8, ConnectorResource.chemical(oxygen));
        helper.assertTrue(!cached.insertChemical(ConnectorMekanism.stack(hydrogen, 10), Action.EXECUTE).isEmpty()
                && cached.insertChemical(ConnectorMekanism.stack(oxygen, 10), Action.EXECUTE).isEmpty(),
                "Cached chemical input bypassed list through unpublished tank");
        target.setInputMode(ConnectorInputMode.EXISTING);
        helper.assertTrue(cached.insertChemical(ConnectorMekanism.stack(hydrogen, 10), Action.SIMULATE).isEmpty(), "Existing chemical rejected");
        target.storage().extractChemical(hydrogen, 333);
        helper.assertTrue(!cached.insertChemical(ConnectorMekanism.stack(hydrogen, 10), Action.SIMULATE).isEmpty(), "Zero chemical stock accepted");

        var storage = new PlayerIdeaStorage();
        long[] amount = {4_000_000_123L};
        long old = PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get();
        IChemicalHandler handler = new IChemicalHandler() {
            @Override public int getChemicalTanks() { return 1; }
            @Override public ChemicalStack getChemicalInTank(int tank) { return ConnectorMekanism.stack(hydrogen, amount[0]); }
            @Override public void setChemicalInTank(int tank, ChemicalStack stack) { throw new UnsupportedOperationException(); }
            @Override public long getChemicalTankCapacity(int tank) { return Long.MAX_VALUE; }
            @Override public boolean isValid(int tank, ChemicalStack stack) { return true; }
            @Override public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
                long accepted = action.simulate() ? stack.getAmount() : Math.min(37, stack.getAmount());
                if (action.execute()) amount[0] += accepted;
                return stack.copyWithAmount(stack.getAmount() - accepted);
            }
            @Override public ChemicalStack extractChemical(int tank, long maximum, Action action) {
                long extracted = Math.min(maximum, amount[0]);
                if (action.execute()) {
                    extracted = Math.min(extracted, 3_000_000_123L);
                    helper.assertTrue(storage.insertChemical(oxygen, 1) == 0, "Chemical reservation allowed reentrant insertion");
                    PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(1L);
                    amount[0] -= extracted;
                }
                return ConnectorMekanism.stack(hydrogen, extracted);
            }
        };
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(Long.MAX_VALUE);
            helper.assertTrue(IdeaStorageLogisticsMekanism.transfer(storage, handler, oxygen::equals, Long.MAX_VALUE, true) == 0,
                    "Chemical spell filter ignored");
            helper.assertTrue(IdeaStorageLogisticsMekanism.transfer(storage, handler, hydrogen::equals, Long.MAX_VALUE, true) == 3_000_000_123L
                    && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 3_000_000_123L && amount[0] == 1_000_000_000L,
                    "Long chemical deposit used simulated quantity or lost resources after config change");
            helper.assertTrue(IdeaStorageLogisticsMekanism.transfer(storage, handler, hydrogen::equals, Long.MAX_VALUE, false) == 37
                    && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 3_000_000_086L && amount[0] == 1_000_000_037L,
                    "Partial chemical withdrawal did not refund under reduced capacity");
        } finally { PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(old); }

        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "chemical-logistics"));
        player.setPos(Vec3.atCenterOf(source.getBlockPos()));
        var context = new SpellContext().setPlayer(player);
        var warehouse = IdeaStorageService.get(player.server, player.getUUID());
        var deposit = new PieceTrickIdeaStorageDepositChemical(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, source.getBlockPos(), 0.125, "mekanism:hydro*");
            }
        };
        var withdraw = new PieceTrickIdeaStorageWithdrawChemical(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, source.getBlockPos(), 0.1, null);
            }
        };
        deposit.execute(context);
        helper.assertTrue(warehouse.simulateExtractChemical(hydrogen, 1000) == 125, "Chemical spell did not read its endpoint/filter/power");
        withdraw.execute(context);
        helper.assertTrue(warehouse.simulateExtractChemical(hydrogen, 1000) == 25
                && source.storage().simulateExtractChemical(hydrogen, 1000) == 892, "Chemical spell withdrawal lost material");
        exact(helper, hydrogen, oxygen);
        double[] power = {1};
        var exactDeposit = new PieceTrickIdeaStorageDepositChemical(new Spell()) {
            @Override public Object getRawParamValue(SpellContext ignored, SpellParam<?> param) {
                return IdeaLogisticsGameTests.paramValue(param, source.getBlockPos(), power[0], "mekanism:hydrogen");
            }
        };
        exactDeposit.setModeOption(IdeaStorageTransferModes.EXACT);
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
    private static void exact(GameTestHelper helper, ResourceLocation hydrogen, ResourceLocation oxygen) {
        long[] tanks = {300, 300, 100};
        ResourceLocation[] contents = {hydrogen, hydrogen, oxygen};
        long[] capacity = {1000};
        IChemicalHandler device = new IChemicalHandler() {
            @Override public int getChemicalTanks() { return tanks.length; }
            @Override public ChemicalStack getChemicalInTank(int tank) { return ConnectorMekanism.stack(contents[tank], tanks[tank]); }
            @Override public void setChemicalInTank(int tank, ChemicalStack stack) { throw new UnsupportedOperationException(); }
            @Override public long getChemicalTankCapacity(int tank) { return capacity[0]; }
            @Override public boolean isValid(int tank, ChemicalStack stack) {
                return contents[tank].equals(MekanismAPI.CHEMICAL_REGISTRY.getKey(stack.getChemical()));
            }
            @Override public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
                if (stack.isEmpty() || !isValid(tank, stack)) return stack;
                long accepted = Math.min(stack.getAmount(), capacity[0] - tanks[tank]);
                if (action.execute()) tanks[tank] += accepted;
                return stack.copyWithAmount(stack.getAmount() - accepted);
            }
            @Override public ChemicalStack extractChemical(int tank, long maximum, Action action) {
                long extracted = Math.min(maximum, tanks[tank]);
                if (action.execute()) tanks[tank] -= extracted;
                return ConnectorMekanism.stack(contents[tank], extracted);
            }
        };
        var storage = new PlayerIdeaStorage();
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, device, id -> true, 700, true) == 0
                && tanks[0] == 300 && tanks[1] == 300 && tanks[2] == 100 && storage.chemicalTypeCount() == 0,
                "Exact chemical supply shortfall changed a side");
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, device, id -> true, 500, true) == 500
                && tanks[0] == 0 && tanks[1] == 100 && tanks[2] == 100 && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 500,
                "Exact chemical deposit must add up tanks of one chemical");
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, device, id -> true, 150, true) == 0
                && tanks[1] == 100 && tanks[2] == 100, "Exact chemical deposit must not combine different chemicals");
        int oldTypes = PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.get();
        long oldPerType = PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.get();
        try {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.set(1);
            helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, device, oxygen::equals, 100, true) == 0
                    && tanks[2] == 100, "A new chemical type bypassed the type limit");
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.set(oldTypes);
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(550L);
            helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, device, hydrogen::equals, 100, true) == 0
                    && tanks[1] == 100, "Per-type cap shortfall changed a side");
        } finally {
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalTypes.set(oldTypes);
            PsitweaksConfig.COMMON.ideaStorageMaxChemicalPerType.set(oldPerType);
        }
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, device, hydrogen::equals, 501, false) == 0
                && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 500, "Exact chemical stock shortfall changed a side");
        capacity[0] = 250; // hydrogen tanks can take 250 + 150 = 400 more, the oxygen tank none
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, device, hydrogen::equals, 500, false) == 0
                && tanks[0] == 0 && tanks[1] == 100 && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 500,
                "Exact chemical destination shortfall changed a side");
        capacity[0] = 1000;
        helper.assertTrue(IdeaStorageLogisticsMekanism.transferExact(storage, device, hydrogen::equals, 500, false) == 500
                && tanks[0] + tanks[1] == 600 && storage.simulateExtractChemical(hydrogen, Long.MAX_VALUE) == 0,
                "Exact chemical withdrawal must move the whole amount");
    }
}
