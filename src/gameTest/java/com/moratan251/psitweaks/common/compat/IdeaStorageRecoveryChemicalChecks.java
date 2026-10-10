package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.gametest.IdeaStorageRecoveryGameTests;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageSavedData;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * Loaded only when Mekanism is present, never scanned by GameTest. Lives in {@code common.compat} so it can drive the
 * package-private Mekanism 10 deposit path ({@link IdeaStorageLogisticsMekanism#deposit}) with a broken gas handler.
 */
public final class IdeaStorageRecoveryChemicalChecks {
    private IdeaStorageRecoveryChemicalChecks() { }

    private static GasStack gas(ResourceLocation id, long amount) {
        return new GasStack(MekanismAPI.gasRegistry().getValue(id), amount);
    }

    public static void run(GameTestHelper helper) throws Exception {
        for (long extra : new long[] {0, 1, 3_000_000_123L}) {
            boolean excess = extra > 0;
            var hydrogen = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
            var actual = excess ? hydrogen : ResourceLocation.fromNamespaceAndPath("mekanism", "oxygen");
            var data = new IdeaStorageSavedData(UUID.randomUUID());
            long before = extra > 1 ? 4_000_000_000L : 10;
            long[] remaining = {before};
            var handler = new IGasHandler() {
                @Override public int getTanks() { return 1; }
                @Override public GasStack getChemicalInTank(int tank) { return gas(actual, remaining[0]); }
                @Override public void setChemicalInTank(int tank, GasStack stack) { throw new UnsupportedOperationException(); }
                @Override public long getTankCapacity(int tank) { return before; }
                @Override public boolean isValid(int tank, GasStack stack) { return true; }
                @Override public GasStack insertChemical(int tank, GasStack stack, Action action) { return stack; }
                @Override public GasStack extractChemical(int tank, long amount, Action action) {
                    // The preview promises hydrogen within the request; execution returns another gas or too much.
                    if (action.simulate()) return gas(hydrogen, Math.min(4, amount));
                    long taken = Math.min(remaining[0], amount + extra);
                    remaining[0] -= taken;
                    return gas(actual, taken);
                }
            };
            IdeaStorageRecoveryGameTests.runAsSpell(helper, Kind.CHEMICAL,
                    () -> IdeaStorageLogisticsMekanism.deposit(data.storage(), handler, "gas", id -> true, 4));
            long taken = 4 + extra;
            var actualKey = IdeaStorageMekanismIntegration.key("gas", actual);
            helper.assertTrue(remaining[0] == before - taken && data.storage().simulateExtractChemical(actualKey, Long.MAX_VALUE) == taken
                    && data.storage().chemicalTypeCount() == 1, "Chemical extraction was lost, duplicated or changed identity");
            IdeaStorageRecoveryGameTests.assertSaved(helper, data);
        }
    }
}
