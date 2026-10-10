package com.moratan251.psitweaks.common.compat;

import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.gametest.IdeaLogisticsGameTests;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageDepositChemical;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickIdeaStorageWithdrawChemical;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;

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
    }
}
