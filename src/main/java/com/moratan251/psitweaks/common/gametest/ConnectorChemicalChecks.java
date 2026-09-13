package com.moratan251.psitweaks.common.gametest;

import com.moratan251.psitweaks.common.compat.ConnectorMekanism;
import com.mojang.authlib.GameProfile;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.network.MessageConnectorAction;
import com.moratan251.psitweaks.common.storage.idea.FluidResourceKey;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSideMode;
import com.moratan251.psitweaks.common.storage.connector.ConnectorTransfers;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Kept out of the GameTest scanner so Mekanism-free dedicated servers never load this class. */
final class ConnectorChemicalChecks {
    static void checkConfiguredAmounts(GameTestHelper helper, IdeaspaceConnectorBlockEntity source, IdeaspaceConnectorBlockEntity target) {
        var id = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
        source.setResource(3, ConnectorResource.chemical(id));
        source.storage().insertChemical(id, 300);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(target.storage().simulateExtractChemical(id, 1000) == 175, "Chemical export ignored its configured amount");
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(target.storage().simulateExtractChemical(id, 1000) == 300
                && source.storage().simulateExtractChemical(id, 1000) == 0, "Configured chemical transfer lost a partial remainder");
    }

    static void checkIndividualFaces(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector) {
        var hydrogen = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
        var oxygen = ResourceLocation.fromNamespaceAndPath("mekanism", "oxygen");
        connector.setResource(5, ConnectorResource.chemical(hydrogen));
        connector.setResource(6, ConnectorResource.chemical(oxygen));
        connector.storage().insertChemical(hydrogen, 2500);
        connector.storage().insertChemical(oxygen, 1000);
        var handler = helper.getLevel().getCapability(Capabilities.CHEMICAL.block(), connector.getBlockPos(), Direction.NORTH);
        connector.setUsesCommonSettings(5, false);
        connector.setSideMode(5, Direction.NORTH, ConnectorSideMode.OUTPUT);
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.INPUT);
        long version = connector.storage().getVersion();
        helper.assertTrue(handler.getChemicalInTank(5).getAmount() == 2500 && handler.getChemicalInTank(6).isEmpty(),
                "Chemical views ignored per-resource output settings");
        for (int tank = 0; tank < handler.getChemicalTanks(); tank++)
            helper.assertTrue(!handler.isValid(tank, ConnectorMekanism.stack(hydrogen, 10))
                    && handler.insertChemical(tank, ConnectorMekanism.stack(hydrogen, 10), Action.EXECUTE).getAmount() == 10,
                    "Chemical input bypassed its settings through tank " + tank);
        helper.assertTrue(handler.insertChemical(ConnectorMekanism.stack(hydrogen, 10), Action.SIMULATE).getAmount() == 10
                && handler.insertChemical(ConnectorMekanism.stack(oxygen, 10), Action.SIMULATE).isEmpty()
                && connector.storage().getVersion() == version, "Chemical bulk input ignored inheritance or changed stock");
        helper.assertTrue(handler.extractChemical(ConnectorMekanism.stack(hydrogen, 123), Action.EXECUTE).getAmount() == 123
                && connector.storage().simulateExtractChemical(hydrogen, 3000) == 2377, "Chemical individual output lost stock");
        connector.setSideMode(5, Direction.NORTH, ConnectorSideMode.INPUT);
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.DISABLED);
        helper.assertTrue(handler.getChemicalInTank(5).isEmpty() && handler.extractChemical(5000, Action.EXECUTE).isEmpty()
                && handler.insertChemical(8, ConnectorMekanism.stack(hydrogen, 10), Action.SIMULATE).isEmpty()
                && !handler.insertChemical(8, ConnectorMekanism.stack(oxygen, 10), Action.SIMULATE).isEmpty(),
                "Cached chemical handler ignored changed per-slot/common modes");
        connector.setUsesCommonSettings(5, true);
        helper.assertTrue(!handler.insertChemical(8, ConnectorMekanism.stack(hydrogen, 10), Action.SIMULATE).isEmpty(),
                "Chemical handler ignored return to common settings");
    }

    static void checkIndividualAutomatic(GameTestHelper helper, IdeaspaceConnectorBlockEntity source,
                                         IdeaspaceConnectorBlockEntity east, IdeaspaceConnectorBlockEntity north) {
        var id = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
        source.setResource(3, ConnectorResource.chemical(id));
        source.storage().insertChemical(id, 2500);
        source.setUsesCommonSettings(3, false);
        source.setSideMode(3, Direction.NORTH, ConnectorSideMode.OUTPUT);
        source.setAutomatic(3, Direction.NORTH, true);
        ConnectorTransfers.push(source, source.storage(), Direction.NORTH);
        helper.assertTrue(north.storage().simulateExtractChemical(id, 3000) == 1000
                && east.storage().simulateExtractChemical(id, 3000) == 0, "Chemical auto output went to the wrong face");
        source.setSideMode(3, Direction.NORTH, ConnectorSideMode.INPUT);
        source.setSideMode(3, Direction.EAST, ConnectorSideMode.OUTPUT);
        source.setAutomatic(3, Direction.EAST, true);
        ConnectorTransfers.push(source, source.storage(), Direction.NORTH);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(north.storage().simulateExtractChemical(id, 3000) == 1000
                && east.storage().simulateExtractChemical(id, 3000) == 1000
                && source.storage().simulateExtractChemical(id, 3000) == 500, "Chemical auto output ignored changed slot faces");
    }

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
        checkContainerFilters(helper, connector, id);
    }

    private static void checkContainerFilters(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector, ResourceLocation id) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(connector.owner(), "tank-filter"));
        player.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(19, player.getInventory(), connector);
        ItemStack chemicalTank = new ItemStack(MekanismBlocks.BASIC_CHEMICAL_TANK.get());
        var chemicals = Capabilities.CHEMICAL.getCapability(chemicalTank);
        helper.assertTrue(chemicals != null && chemicals.insertChemical(ConnectorMekanism.stack(id, 750), Action.EXECUTE).isEmpty(),
                "Test chemical tank could not be filled");
        ItemStack fluidTank = new ItemStack(MekanismBlocks.BASIC_FLUID_TANK.get());
        var fluids = fluidTank.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        helper.assertTrue(fluids != null && fluids.fill(new FluidStack(Fluids.WATER, 750), FluidAction.EXECUTE) == 750,
                "Test fluid tank could not be filled");
        ConnectorResource[] expected = {ConnectorResource.chemical(id),
                ConnectorResource.fluid(FluidResourceKey.of(new FluidStack(Fluids.WATER, 1)).orElseThrow())};
        ItemStack[] tanks = {chemicalTank, fluidTank};
        long version = connector.storage().getVersion();
        for (int i = 0; i < tanks.length; i++) {
            ItemStack before = tanks[i].copy();
            menu.setCarried(tanks[i]);
            menu.handleAction(player, new MessageConnectorAction(19, menu.session(), menu.revision(), IdeaspaceConnectorMenu.ASSIGN, 4 + i, 1));
            helper.assertTrue(connector.resource(4 + i).equals(expected[i]) && ItemStack.matches(before, menu.getCarried()),
                    "Tank filter did not select contents, or changed the tank/count/components");
        }
        helper.assertTrue(connector.storage().getVersion() == version, "Tank filter transferred stored resources");
    }
}
