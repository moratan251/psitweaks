package com.moratan251.psitweaks.common.compat;

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
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

/** Kept out of the GameTest scanner so Mekanism-free dedicated servers never load this class. */
public final class ConnectorChemicalChecks {
    private static GasStack stack(ResourceLocation key, long amount) {
        return new GasStack(MekanismAPI.gasRegistry().getValue(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring(4))), amount);
    }
    public static void checkConfiguredAmounts(GameTestHelper helper, IdeaspaceConnectorBlockEntity source, IdeaspaceConnectorBlockEntity target) {
        var id = ResourceLocation.fromNamespaceAndPath("mekanism", "gas/hydrogen");
        source.setResource(3, ConnectorResource.chemical(id));
        source.storage().insertChemical(id, 300);
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(target.storage().simulateExtractChemical(id, 1000) == 175, "Chemical export ignored its configured amount");
        ConnectorTransfers.push(source, source.storage(), Direction.EAST);
        helper.assertTrue(target.storage().simulateExtractChemical(id, 1000) == 300
                && source.storage().simulateExtractChemical(id, 1000) == 0, "Configured chemical transfer lost a partial remainder");
    }

    public static void checkIndividualFaces(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector) {
        var hydrogen = ResourceLocation.fromNamespaceAndPath("mekanism", "gas/hydrogen");
        var oxygen = ResourceLocation.fromNamespaceAndPath("mekanism", "gas/oxygen");
        connector.setResource(5, ConnectorResource.chemical(hydrogen));
        connector.setResource(6, ConnectorResource.chemical(oxygen));
        connector.storage().insertChemical(hydrogen, 2500);
        connector.storage().insertChemical(oxygen, 1000);
        var handler = connector.getCapability(Capabilities.GAS_HANDLER, Direction.NORTH).orElseThrow(() -> new AssertionError("Missing chemical capability"));
        connector.setUsesCommonSettings(5, false);
        connector.setSideMode(5, Direction.NORTH, ConnectorSideMode.OUTPUT);
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.INPUT);
        long version = connector.storage().getVersion();
        helper.assertTrue(handler.getChemicalInTank(5).getAmount() == 2500 && handler.getChemicalInTank(6).isEmpty(),
                "Chemical views ignored per-resource output settings");
        for (int tank = 0; tank < handler.getTanks(); tank++)
            helper.assertTrue(!handler.isValid(tank, stack(hydrogen, 10))
                    && handler.insertChemical(tank, stack(hydrogen, 10), Action.EXECUTE).getAmount() == 10,
                    "Chemical input bypassed its settings through tank " + tank);
        helper.assertTrue(handler.insertChemical(stack(hydrogen, 10), Action.SIMULATE).getAmount() == 10
                && handler.insertChemical(stack(oxygen, 10), Action.SIMULATE).isEmpty()
                && connector.storage().getVersion() == version, "Chemical bulk input ignored inheritance or changed stock");
        helper.assertTrue(handler.extractChemical(stack(hydrogen, 123), Action.EXECUTE).getAmount() == 123
                && connector.storage().simulateExtractChemical(hydrogen, 3000) == 2377, "Chemical individual output lost stock");
        connector.setSideMode(5, Direction.NORTH, ConnectorSideMode.INPUT);
        connector.setSideMode(Direction.NORTH, ConnectorSideMode.DISABLED);
        helper.assertTrue(handler.getChemicalInTank(5).isEmpty() && handler.extractChemical(5000, Action.EXECUTE).isEmpty()
                && handler.insertChemical(8, stack(hydrogen, 10), Action.SIMULATE).isEmpty()
                && !handler.insertChemical(8, stack(oxygen, 10), Action.SIMULATE).isEmpty(),
                "Cached chemical handler ignored changed per-slot/common modes");
        connector.setUsesCommonSettings(5, true);
        helper.assertTrue(!handler.insertChemical(8, stack(hydrogen, 10), Action.SIMULATE).isEmpty(),
                "Chemical handler ignored return to common settings");
    }

    public static void checkIndividualAutomatic(GameTestHelper helper, IdeaspaceConnectorBlockEntity source,
                                         IdeaspaceConnectorBlockEntity east, IdeaspaceConnectorBlockEntity north) {
        var id = ResourceLocation.fromNamespaceAndPath("mekanism", "gas/hydrogen");
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

    public static void run(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector) {
        var id = ResourceLocation.fromNamespaceAndPath("mekanism", "gas/hydrogen");
        var handler = connector.getCapability(Capabilities.GAS_HANDLER, Direction.UP).orElseThrow(() -> new AssertionError("Missing chemical capability"));
        helper.assertTrue(handler != null, "Chemical capability missing");
        long version = connector.storage().getVersion();
        helper.assertTrue(handler.insertChemical(stack(id, 2500), Action.SIMULATE).isEmpty()
                && version == connector.storage().getVersion(), "Chemical simulation mutated stock");
        handler.insertChemical(stack(id, 2500), Action.EXECUTE);
        helper.assertTrue(handler.extractChemical(1000, Action.EXECUTE).isEmpty(), "Unpublished chemical escaped");
        connector.setResource(0, ConnectorResource.chemical(id));
        helper.assertTrue(handler.getChemicalInTank(0).getAmount() == 2500, "Chemical stock view wrong");
        helper.assertTrue(handler.extractChemical(5000, Action.SIMULATE).getAmount() == 2500
                && connector.storage().simulateExtractChemical(id, 9999) == 2500, "Chemical extraction simulation changed stock");
        connector.setSideMode(Direction.UP, ConnectorSideMode.DISABLED);
        helper.assertTrue(handler.extractChemical(5000, Action.EXECUTE).isEmpty(), "Cached chemical handler bypassed disabled face");
        connector.setSideMode(Direction.UP, ConnectorSideMode.OUTPUT);
        helper.assertTrue(handler.insertChemical(stack(id, 10), Action.EXECUTE).getAmount() == 10,
                "Output face accepted chemicals");
        helper.assertTrue(handler.extractChemical(5000, Action.EXECUTE).getAmount() == 2500
                && connector.storage().simulateExtractChemical(id, 9999) == 0, "Chemical shortage/conservation failed");
        connector.storage().insertChemical(id, 2500);
        long[] received = {0};
        IGasHandler target = new IGasHandler() {
            @Override public int getTanks() { return 1; }
            @Override public GasStack getChemicalInTank(int tank) { return stack(id, received[0]); }
            @Override public void setChemicalInTank(int tank, GasStack stack) { throw new UnsupportedOperationException(); }
            @Override public long getTankCapacity(int tank) { return 1000; }
            @Override public boolean isValid(int tank, GasStack stack) { return true; }
            @Override public GasStack insertChemical(int tank, GasStack stack, Action action) {
                long accepted = Math.min(stack.getAmount(), action.simulate() ? 1000 : 37);
                if (action.execute()) received[0] += accepted;
                return new GasStack(stack.getType(), stack.getAmount() - accepted);
            }
            @Override public GasStack extractChemical(int tank, long amount, Action action) { return GasStack.EMPTY; }
        };
        helper.assertTrue(ConnectorMekanism.pushKind(connector.storage(), id, target, 1000, MekanismAPI.gasRegistry(), GasStack::new) == 37
                && received[0] == 37 && connector.storage().simulateExtractChemical(id, 9999) == 2463,
                "Chemical automatic output failed to refund actual unaccepted amount");
        checkContainerFilters(helper, connector, id);
    }

    private static void checkContainerFilters(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector, ResourceLocation id) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(connector.owner(), "tank-filter"));
        player.setPos(Vec3.atCenterOf(connector.getBlockPos()));
        var menu = new IdeaspaceConnectorMenu(19, player.getInventory(), connector);
        ItemStack chemicalTank = new ItemStack(MekanismBlocks.BASIC_CHEMICAL_TANK.asItem());
        var chemicals = chemicalTank.getCapability(Capabilities.GAS_HANDLER).orElse(null);
        helper.assertTrue(chemicals != null && chemicals.insertChemical(stack(id, 750), Action.EXECUTE).isEmpty(),
                "Test chemical tank could not be filled");
        ItemStack fluidTank = new ItemStack(MekanismBlocks.BASIC_FLUID_TANK.asItem());
        var fluids = fluidTank.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
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
    public static void checkAllKinds(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector) {
        checkKind(helper, connector, Capabilities.GAS_HANDLER, MekanismAPI.gasRegistry(), mekanism.api.chemical.gas.GasStack::new);
        checkKind(helper, connector, Capabilities.INFUSION_HANDLER, MekanismAPI.infuseTypeRegistry(), mekanism.api.chemical.infuse.InfusionStack::new);
        checkKind(helper, connector, Capabilities.PIGMENT_HANDLER, MekanismAPI.pigmentRegistry(), mekanism.api.chemical.pigment.PigmentStack::new);
        checkKind(helper, connector, Capabilities.SLURRY_HANDLER, MekanismAPI.slurryRegistry(), mekanism.api.chemical.slurry.SlurryStack::new);
    }
    private static <C extends mekanism.api.chemical.Chemical<C>, S extends mekanism.api.chemical.ChemicalStack<C>, H extends mekanism.api.chemical.IChemicalHandler<C, S>>
    void checkKind(GameTestHelper helper, IdeaspaceConnectorBlockEntity connector,
            net.minecraftforge.common.capabilities.Capability<H> capability,
            net.minecraftforge.registries.IForgeRegistry<C> registry, java.util.function.BiFunction<C, Long, S> factory) {
        var chemical = registry.getValues().stream().filter(c -> !c.isEmptyType() && c.getAttributes().isEmpty()).findFirst().orElseThrow();
        var stack = factory.apply(chemical, 2000L);
        var resource = ConnectorMekanism.ingredient(stack);
        connector.setResource(0, resource);
        var handler = connector.getCapability(capability, Direction.NORTH).orElseThrow(AssertionError::new);
        helper.assertTrue(handler.insertChemical(stack, Action.SIMULATE).isEmpty() && resource.amount(connector.storage()) == 0,
                "Chemical simulation mutated inventory: " + resource);
        helper.assertTrue(handler.insertChemical(stack, Action.EXECUTE).isEmpty()
                && handler.getChemicalInTank(0).getAmount() == 2000, "Chemical insertion/view failed: " + resource);
        helper.assertTrue(connector.storage().chemicalAmountById(stack.getTypeRegistryName()) == 2000,
                "Raw chemical ID amount failed: " + resource);
        helper.assertTrue(handler.extractChemical(0, 123, Action.EXECUTE).getAmount() == 123
                && resource.amount(connector.storage()) == 1877, "Chemical extraction failed: " + resource);
        var tank = new ItemStack(MekanismBlocks.BASIC_CHEMICAL_TANK.asItem());
        tank.getCapability(capability).orElseThrow(AssertionError::new).insertChemical(factory.apply(chemical, 10L), Action.EXECUTE);
        helper.assertTrue(ConnectorMekanism.containedResource(tank).equals(resource), "Container filter lost chemical kind: " + resource);
    }

}
