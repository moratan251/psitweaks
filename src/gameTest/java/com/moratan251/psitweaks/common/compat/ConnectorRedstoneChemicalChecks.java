package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.storage.connector.ConnectorCapabilities;
import com.moratan251.psitweaks.common.storage.connector.ConnectorRedstoneMode;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

/** Only loaded behind the Mekanism guard; never scanned as a GameTest holder. Mekanism 10: gas kind only. */
public final class ConnectorRedstoneChemicalChecks {
    private static final ResourceLocation HYDROGEN_ID = new ResourceLocation("mekanism", "hydrogen");
    private static final ResourceLocation OXYGEN_ID = new ResourceLocation("mekanism", "oxygen");
    // Warehouse and connector resources use kind-prefixed keys (mekanism:gas/hydrogen).
    private static final ResourceLocation HYDROGEN = IdeaStorageMekanismIntegration.key("gas", HYDROGEN_ID);

    private ConnectorRedstoneChemicalChecks() { }

    private static GasStack stack(ResourceLocation rawId, long amount) {
        return new GasStack(MekanismAPI.gasRegistry().getValue(rawId), amount);
    }

    public static void seed(IdeaspaceConnectorBlockEntity connector) {
        connector.setResource(3, ConnectorResource.chemical(HYDROGEN));
        connector.storage().insertChemical(HYDROGEN, 10000);
    }

    public static void assertAmount(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector, long expected) {
        helper.assertTrue(connector.storage().simulateExtractChemical(HYDROGEN, 10000) == expected,
                "Chemical automatic redstone gate failed; expected " + expected);
    }

    public static void checkCachedHandler(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector) {
        seed(connector);
        // Resolve once and keep the handler instance; signal changes renew the LazyOptional, but this
        // cached object must still follow the live signal.
        IGasHandler handler = ConnectorCapabilities.get(helper.getLevel(), connector.getBlockPos(), Direction.NORTH, Capabilities.GAS_HANDLER);
        helper.assertTrue(handler != null, "Missing gas capability");
        long version = connector.storage().getVersion();
        for (Action action : Action.values()) {
            for (int tank = 0; tank < handler.getTanks(); tank++)
                helper.assertTrue(handler.insertChemical(tank, stack(HYDROGEN_ID, 20), action).getAmount() == 20
                        && handler.extractChemical(tank, 20, action).isEmpty(), "Inactive chemical tank accepted a transfer");
            helper.assertTrue(handler.insertChemical(stack(OXYGEN_ID, 20), action).getAmount() == 20
                    && handler.extractChemical(20, action).isEmpty(), "Inactive chemical bulk transfer succeeded");
        }
        helper.assertTrue(handler.getChemicalInTank(3).isEmpty() && connector.storage().getVersion() == version,
                "Inactive chemical handler exposed or mutated stock");
        var powerPos = connector.getBlockPos().above();
        helper.getLevel().setBlockAndUpdate(powerPos, Blocks.REDSTONE_BLOCK.defaultBlockState());
        helper.assertTrue(handler.insertChemical(stack(OXYGEN_ID, 20), Action.EXECUTE).isEmpty()
                && handler.extractChemical(3, 20, Action.EXECUTE).getAmount() == 20, "Cached chemical handler did not resume");
        connector.setUsesCommonSettings(3, false);
        connector.setRedstoneMode(3, Direction.NORTH, ConnectorRedstoneMode.LOW);
        helper.assertTrue(handler.insertChemical(9, stack(HYDROGEN_ID, 20), Action.EXECUTE).getAmount() == 20
                && handler.extractChemical(3, 20, Action.EXECUTE).isEmpty()
                && handler.insertChemical(stack(OXYGEN_ID, 20), Action.SIMULATE).isEmpty(),
                "Chemical resource identity bypassed its individual redstone condition");
        helper.getLevel().setBlockAndUpdate(powerPos, Blocks.AIR.defaultBlockState());
        helper.assertTrue(handler.extractChemical(3, 20, Action.SIMULATE).getAmount() == 20
                && handler.insertChemical(stack(OXYGEN_ID, 20), Action.SIMULATE).getAmount() == 20,
                "Chemical LOW/common HIGH settings did not follow signal removal");
    }
}
