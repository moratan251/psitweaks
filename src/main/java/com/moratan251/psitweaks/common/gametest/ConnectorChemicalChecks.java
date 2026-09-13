package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

/** Kept out of the GameTest scanner so Mekanism-free dedicated servers never load this class. */
final class ConnectorChemicalChecks {
    static void run(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector) {
        var id = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
        var handler = helper.getLevel().getCapability(Capabilities.CHEMICAL.block(), connector.getBlockPos(), Direction.UP);
        helper.assertTrue(handler != null, "Chemical capability missing");
        long version = connector.storage().getVersion();
        helper.assertTrue(handler.insertChemical(ConnectorMekanism.stack(id, 2500), Action.SIMULATE).isEmpty()
                && version == connector.storage().getVersion(), "Chemical simulation mutated stock");
        handler.insertChemical(ConnectorMekanism.stack(id, 2500), Action.EXECUTE);
        helper.assertTrue(handler.extractChemical(1000, Action.EXECUTE).isEmpty(), "Unpublished chemical escaped");
        connector.setResource(0, ConnectorResource.chemical(id));
        helper.assertTrue(handler.getChemicalInTank(0).getAmount() == 2500, "Chemical stock view wrong");
        helper.assertTrue(handler.extractChemical(5000, Action.SIMULATE).getAmount() == 2500
                && connector.storage().simulateExtractChemical(id, 9999) == 2500, "Chemical extraction simulation changed stock");
        connector.setSideMode(Direction.UP, ConnectorSideMode.DISABLED);
        helper.assertTrue(handler.extractChemical(5000, Action.EXECUTE).isEmpty(), "Cached chemical handler bypassed disabled face");
        connector.setSideMode(Direction.UP, ConnectorSideMode.OUTPUT);
        helper.assertTrue(handler.insertChemical(ConnectorMekanism.stack(id, 10), Action.EXECUTE).getAmount() == 10,
                "Output face accepted chemicals");
        helper.assertTrue(handler.extractChemical(5000, Action.EXECUTE).getAmount() == 2500
                && connector.storage().simulateExtractChemical(id, 9999) == 0, "Chemical shortage/conservation failed");
        connector.storage().insertChemical(id, 2500);
        long[] received = {0};
        IChemicalHandler target = new IChemicalHandler() {
            @Override public int getChemicalTanks() { return 1; }
            @Override public ChemicalStack getChemicalInTank(int tank) { return ConnectorMekanism.stack(id, received[0]); }
            @Override public void setChemicalInTank(int tank, ChemicalStack stack) { throw new UnsupportedOperationException(); }
            @Override public long getChemicalTankCapacity(int tank) { return 1000; }
            @Override public boolean isValid(int tank, ChemicalStack stack) { return true; }
            @Override public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
                long accepted = Math.min(stack.getAmount(), action.simulate() ? 1000 : 37);
                if (action.execute()) received[0] += accepted;
                return stack.copyWithAmount(stack.getAmount() - accepted);
            }
            @Override public ChemicalStack extractChemical(int tank, long amount, Action action) { return ChemicalStack.EMPTY; }
        };
        helper.assertTrue(ConnectorMekanism.push(connector.storage(), id, target, 1000) == 37
                && received[0] == 37 && connector.storage().simulateExtractChemical(id, 9999) == 2463,
                "Chemical automatic output failed to refund actual unaccepted amount");
    }
}
