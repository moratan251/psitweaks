package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.moratan251.psitweaks.common.storage.connector.ConnectorRedstoneMode;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import mekanism.api.Action;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

/** Only loaded behind the Mekanism guard; never scanned as a GameTest holder. */
final class ConnectorRedstoneChemicalChecks {
    private static final ResourceLocation HYDROGEN = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
    private static final ResourceLocation OXYGEN = ResourceLocation.fromNamespaceAndPath("mekanism", "oxygen");

    static void seed(IdeaspaceConnectorBlockEntity connector) {
        connector.setResource(3, ConnectorResource.chemical(HYDROGEN));
        connector.storage().insertChemical(HYDROGEN, 10000);
    }

    static void assertAmount(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector, long expected) {
        helper.assertTrue(connector.storage().simulateExtractChemical(HYDROGEN, 10000) == expected,
                "Chemical automatic redstone gate failed; expected " + expected);
    }

    static void checkCachedHandler(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector) {
        seed(connector);
        var handler = helper.getLevel().getCapability(Capabilities.CHEMICAL.block(), connector.getBlockPos(), Direction.NORTH);
        long version = connector.storage().getVersion();
        for (Action action : Action.values()) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++)
                helper.assertTrue(handler.insertChemical(tank, ConnectorMekanism.stack(HYDROGEN, 20), action).getAmount() == 20
                        && handler.extractChemical(tank, 20, action).isEmpty(), "Inactive chemical tank accepted a transfer");
            helper.assertTrue(handler.insertChemical(ConnectorMekanism.stack(OXYGEN, 20), action).getAmount() == 20
                    && handler.extractChemical(20, action).isEmpty(), "Inactive chemical bulk transfer succeeded");
        }
        helper.assertTrue(handler.getChemicalInTank(3).isEmpty() && connector.storage().getVersion() == version,
                "Inactive chemical handler exposed or mutated stock");
        var powerPos = connector.getBlockPos().above();
        helper.getLevel().setBlockAndUpdate(powerPos, Blocks.REDSTONE_BLOCK.defaultBlockState());
        helper.assertTrue(handler.insertChemical(ConnectorMekanism.stack(OXYGEN, 20), Action.EXECUTE).isEmpty()
                && handler.extractChemical(3, 20, Action.EXECUTE).getAmount() == 20, "Cached chemical handler did not resume");
        connector.setUsesCommonSettings(3, false);
        connector.setRedstoneMode(3, Direction.NORTH, ConnectorRedstoneMode.LOW);
        helper.assertTrue(handler.insertChemical(9, ConnectorMekanism.stack(HYDROGEN, 20), Action.EXECUTE).getAmount() == 20
                && handler.extractChemical(3, 20, Action.EXECUTE).isEmpty()
                && handler.insertChemical(ConnectorMekanism.stack(OXYGEN, 20), Action.SIMULATE).isEmpty(),
                "Chemical resource identity bypassed its individual redstone condition");
        helper.getLevel().setBlockAndUpdate(powerPos, Blocks.AIR.defaultBlockState());
        helper.assertTrue(handler.extractChemical(3, 20, Action.SIMULATE).getAmount() == 20
                && handler.insertChemical(ConnectorMekanism.stack(OXYGEN, 20), Action.SIMULATE).getAmount() == 20,
                "Chemical LOW/common HIGH settings did not follow signal removal");
    }
}
