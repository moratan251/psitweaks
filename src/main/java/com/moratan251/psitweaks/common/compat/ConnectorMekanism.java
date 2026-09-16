package com.moratan251.psitweaks.common.compat;
import com.moratan251.psitweaks.common.storage.connector.*;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.*;
import mekanism.api.chemical.gas.*;
import mekanism.api.chemical.infuse.*;
import mekanism.api.chemical.pigment.*;
import mekanism.api.chemical.slurry.*;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.IForgeRegistry;
import java.util.Map;
import java.util.function.BiFunction;
/** Loaded only with Mekanism; Forge preserves all four chemical registry identities. */
public final class ConnectorMekanism {
    private ConnectorMekanism() { }
    public static boolean validChemical(ResourceLocation id) {
        return id != null && IdeaStorageMekanismIntegration.chemical(id) != null;
    }
    public static ConnectorResource ingredient(Object ingredient) {
        if (!(ingredient instanceof ChemicalStack<?> stack) || stack.isEmpty()) return ConnectorResource.EMPTY;
        String kind = stack instanceof GasStack ? "gas" : stack instanceof InfusionStack ? "infuse"
                : stack instanceof PigmentStack ? "pigment" : stack instanceof SlurryStack ? "slurry" : null;
        return kind == null ? ConnectorResource.EMPTY : ConnectorResource.chemical(IdeaStorageMekanismIntegration.key(kind, stack.getTypeRegistryName()));
    }
    public static ConnectorResource containedResource(ItemStack stack) {
        for (Capability<?> cap : new Capability<?>[] {Capabilities.GAS_HANDLER, Capabilities.INFUSION_HANDLER, Capabilities.PIGMENT_HANDLER, Capabilities.SLURRY_HANDLER}) {
            Object value = stack.getCapability(cap).orElse(null);
            if (value instanceof IChemicalHandler<?, ?> handler) for (int tank = 0; tank < handler.getTanks(); tank++) {
                ConnectorResource resource = ingredient(handler.getChemicalInTank(tank));
                if (resource.kind() != ConnectorResource.Kind.EMPTY) return resource;
            }
        }
        return ConnectorResource.EMPTY;
    }
    public static void addCapabilities(IdeaspaceConnectorBlockEntity connector, Direction side, Map<Capability<?>, LazyOptional<?>> caps) {
        caps.put(Capabilities.GAS_HANDLER, LazyOptional.of(() -> new GasHandler(connector, side)));
        caps.put(Capabilities.INFUSION_HANDLER, LazyOptional.of(() -> new InfuseTypeHandler(connector, side)));
        caps.put(Capabilities.PIGMENT_HANDLER, LazyOptional.of(() -> new PigmentHandler(connector, side)));
        caps.put(Capabilities.SLURRY_HANDLER, LazyOptional.of(() -> new SlurryHandler(connector, side)));
    }
    public static long push(PlayerIdeaStorage storage, ResourceLocation id, Level level, BlockPos pos, Direction side, long maximum) {
        if (!validChemical(id)) return 0;
        return switch (id.getPath().substring(0, id.getPath().indexOf('/'))) {
            case "gas" -> pushKind(storage, id, ConnectorCapabilities.get(level, pos, side, Capabilities.GAS_HANDLER), maximum, MekanismAPI.gasRegistry(), GasStack::new);
            case "infuse" -> pushKind(storage, id, ConnectorCapabilities.get(level, pos, side, Capabilities.INFUSION_HANDLER), maximum, MekanismAPI.infuseTypeRegistry(), InfusionStack::new);
            case "pigment" -> pushKind(storage, id, ConnectorCapabilities.get(level, pos, side, Capabilities.PIGMENT_HANDLER), maximum, MekanismAPI.pigmentRegistry(), PigmentStack::new);
            case "slurry" -> pushKind(storage, id, ConnectorCapabilities.get(level, pos, side, Capabilities.SLURRY_HANDLER), maximum, MekanismAPI.slurryRegistry(), SlurryStack::new);
            default -> 0;
        };
    }
    private static ResourceLocation rawId(ResourceLocation id) {
        return new ResourceLocation(id.getNamespace(), id.getPath().substring(id.getPath().indexOf('/') + 1));
    }
    static <C extends Chemical<C>, S extends ChemicalStack<C>> long pushKind(PlayerIdeaStorage storage,
            ResourceLocation id, IChemicalHandler<C, S> target, long maximum, IForgeRegistry<C> registry, BiFunction<C, Long, S> factory) {
        if (target == null) return 0;
        C chemical = registry.getValue(rawId(id));
        long available = storage.simulateExtractChemical(id, maximum);
        if (chemical == null || chemical.isEmptyType() || available <= 0) return 0;
        long planned = Math.max(0, Math.min(available, available - target.insertChemical(factory.apply(chemical, available), Action.SIMULATE).getAmount()));
        var withdrawal = storage.withdrawChemical(id, planned); long accepted = 0;
        try {
            if (withdrawal.amount() > 0) accepted = Math.max(0, Math.min(withdrawal.amount(), withdrawal.amount()
                    - target.insertChemical(factory.apply(chemical, withdrawal.amount()), Action.EXECUTE).getAmount()));
            return accepted;
        } finally { withdrawal.restore(withdrawal.amount() - accepted); }
    }
    private abstract static class Handler<C extends Chemical<C>, S extends ChemicalStack<C>> implements IChemicalHandler<C, S> {
        final IdeaspaceConnectorBlockEntity connector; final Direction side; final String kind;
        final IForgeRegistry<C> registry; final BiFunction<C, Long, S> factory;
        Handler(IdeaspaceConnectorBlockEntity connector, Direction side, String kind, IForgeRegistry<C> registry, BiFunction<C, Long, S> factory) {
            this.connector = connector; this.side = side; this.kind = kind; this.registry = registry; this.factory = factory;
        }
        S stack(ResourceLocation id, long amount) {
            if (id == null || !id.getPath().startsWith(kind + "/") || amount <= 0) return getEmptyStack();
            C chemical = registry.getValue(rawId(id));
            return chemical == null || chemical.isEmptyType() ? getEmptyStack() : factory.apply(chemical, amount);
        }
        @Override public int getTanks() { return IdeaspaceConnectorBlockEntity.SLOTS; }
        @Override public S getChemicalInTank(int tank) {
            var resource = connector.resource(tank);
            return connector.sideMode(tank, side).output ? stack(resource.chemical(), resource.amount(connector.storage())) : getEmptyStack();
        }
        @Override public void setChemicalInTank(int tank, S stack) { throw new UnsupportedOperationException("Use insert/extract on shared storage"); }
        @Override public long getTankCapacity(int tank) { var storage = connector.storage(); return tank >= 0 && tank < getTanks() && storage != null ? storage.maxChemicalPerType() : 0; }
        @Override public boolean isValid(int tank, S stack) { return tank >= 0 && tank < getTanks() && !stack.isEmpty() && connector.allowsInput(side, ingredient(stack)); }
        @Override public S insertChemical(int tank, S stack, Action action) {
            var storage = connector.storage(); if (storage == null || !isValid(tank, stack)) return stack;
            var id = IdeaStorageMekanismIntegration.key(kind, stack.getTypeRegistryName());
            long accepted = action.simulate() ? storage.simulateInsertChemical(id, stack.getAmount()) : storage.insertChemical(id, stack.getAmount());
            return factory.apply(stack.getType(), stack.getAmount() - accepted);
        }
        @Override public S extractChemical(int tank, long amount, Action action) {
            var storage = connector.storage(); var id = connector.resource(tank).chemical();
            if (storage == null || !connector.sideMode(tank, side).output || stack(id, 1).isEmpty()) return getEmptyStack();
            return stack(id, action.simulate() ? storage.simulateExtractChemical(id, amount) : storage.extractChemical(id, amount));
        }
    }
    private static final class GasHandler extends Handler<Gas, GasStack> implements IGasHandler {
        GasHandler(IdeaspaceConnectorBlockEntity c, Direction s) { super(c, s, "gas", MekanismAPI.gasRegistry(), GasStack::new); }
    }
    private static final class InfuseTypeHandler extends Handler<InfuseType, InfusionStack> implements IInfusionHandler {
        InfuseTypeHandler(IdeaspaceConnectorBlockEntity c, Direction s) { super(c, s, "infuse", MekanismAPI.infuseTypeRegistry(), InfusionStack::new); }
    }
    private static final class PigmentHandler extends Handler<Pigment, PigmentStack> implements IPigmentHandler {
        PigmentHandler(IdeaspaceConnectorBlockEntity c, Direction s) { super(c, s, "pigment", MekanismAPI.pigmentRegistry(), PigmentStack::new); }
    }
    private static final class SlurryHandler extends Handler<Slurry, SlurryStack> implements ISlurryHandler {
        SlurryHandler(IdeaspaceConnectorBlockEntity c, Direction s) { super(c, s, "slurry", MekanismAPI.slurryRegistry(), SlurryStack::new); }
    }
}
