package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.moratan251.psitweaks.common.compat.IdeaStorageLogisticsMekanism;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageSavedData;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

/** Loaded only when Mekanism is present, never directly scanned by GameTest. */
final class IdeaStorageRecoveryChemicalChecks {
    static void run(GameTestHelper helper) throws Exception {
        for (long extra : new long[] {0, 1, 3_000_000_123L}) {
            boolean excess = extra > 0;
            var hydrogen = ResourceLocation.parse("mekanism:hydrogen");
            var actual = ResourceLocation.parse(excess ? "mekanism:hydrogen" : "mekanism:oxygen");
            var data = IdeaStorageSavedData.factory(UUID.randomUUID()).constructor().get();
            long before = extra > 1 ? 4_000_000_000L : 10;
            long[] remaining = {before};
            var handler = new IChemicalHandler() {
                @Override public int getChemicalTanks() { return 1; }
                @Override public ChemicalStack getChemicalInTank(int tank) { return ConnectorMekanism.stack(actual, remaining[0]); }
                @Override public void setChemicalInTank(int tank, ChemicalStack stack) { throw new UnsupportedOperationException(); }
                @Override public long getChemicalTankCapacity(int tank) { return before; }
                @Override public boolean isValid(int tank, ChemicalStack stack) { return true; }
                @Override public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) { return stack; }
                @Override public ChemicalStack extractChemical(int tank, long amount, Action action) {
                    if (action.simulate()) return ConnectorMekanism.stack(hydrogen, Math.min(4, amount));
                    long taken = Math.min(remaining[0], amount + extra);
                    remaining[0] -= taken;
                    return ConnectorMekanism.stack(actual, taken);
                }
            };
            IdeaStorageRecoveryGameTests.runAsSpell(helper, Kind.CHEMICAL,
                    () -> IdeaStorageLogisticsMekanism.transfer(data.storage(), handler, id -> true, 4, true));
            long taken = 4 + extra;
            helper.assertTrue(remaining[0] == before - taken && data.storage().simulateExtractChemical(actual, Long.MAX_VALUE) == taken
                    && data.storage().chemicalTypeCount() == 1, "Chemical extraction was lost, duplicated or changed identity");
            IdeaStorageRecoveryGameTests.assertSaved(helper, data);
        }
    }
}
