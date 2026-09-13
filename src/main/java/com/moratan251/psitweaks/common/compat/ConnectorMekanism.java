package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.registries.PsitweaksBlockEntityTypes;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Loaded only when Mekanism is present; no Mekanism types cross the common API. */
public final class ConnectorMekanism {
    private ConnectorMekanism() { }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.CHEMICAL.block(), PsitweaksBlockEntityTypes.IDEASPACE_CONNECTOR.get(),
                (connector, side) -> side == null ? null : new Handler(connector, side));
    }

    public static ChemicalStack stack(ResourceLocation id, long amount) {
        var chemical = id == null ? null : MekanismAPI.CHEMICAL_REGISTRY.getHolder(id).orElse(null);
        return chemical == null || amount <= 0 ? ChemicalStack.EMPTY : new ChemicalStack(chemical, amount);
    }

    public static boolean validChemical(ResourceLocation id) {
        return !stack(id, 1).isEmpty();
    }

    public static ConnectorResource containedResource(ItemStack stack) {
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
        if (handler != null) for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
            ChemicalStack chemical = handler.getChemicalInTank(tank);
            if (!chemical.isEmpty()) return ConnectorResource.chemical(MekanismAPI.CHEMICAL_REGISTRY.getKey(chemical.getChemical()));
        }
        return ConnectorResource.EMPTY;
    }

    public static long push(PlayerIdeaStorage storage, ResourceLocation id, Level level,
                            BlockPos pos, Direction face, long maximum) {
        IChemicalHandler target = Capabilities.CHEMICAL.getCapabilityIfLoaded(level, pos, face);
        return target == null ? 0 : push(storage, id, target, maximum);
    }

    public static long push(PlayerIdeaStorage storage, ResourceLocation id, IChemicalHandler target, long maximum) {
        ChemicalStack offered = stack(id, storage.simulateExtractChemical(id, maximum));
        if (offered.isEmpty()) return 0;
        long planned = Math.max(0, offered.getAmount() - target.insertChemical(offered, Action.SIMULATE).getAmount());
        var withdrawal = storage.withdrawChemical(id, planned);
        long accepted = 0;
        try {
            if (withdrawal.amount() > 0) {
                long remaining = target.insertChemical(stack(id, withdrawal.amount()), Action.EXECUTE).getAmount();
                accepted = Math.max(0, Math.min(withdrawal.amount(), withdrawal.amount() - remaining));
            }
            return accepted;
        } finally {
            withdrawal.restore(withdrawal.amount() - accepted);
        }
    }

    private record Handler(IdeaspaceConnectorBlockEntity connector, Direction side) implements IChemicalHandler {
        private boolean valid(int tank) { return tank >= 0 && tank < getChemicalTanks(); }
        @Override public int getChemicalTanks() { return IdeaspaceConnectorBlockEntity.SLOTS; }
        @Override public ChemicalStack getChemicalInTank(int tank) {
            var resource = connector.resource(tank);
            return connector.sideMode(tank, side).output ? stack(resource.chemical(), resource.amount(connector.storage())) : ChemicalStack.EMPTY;
        }
        @Override public void setChemicalInTank(int tank, ChemicalStack stack) {
            // This is a view of shared storage, not a mutable standalone tank.
            throw new UnsupportedOperationException("Use insertChemical/extractChemical on an Ideaspace Connector");
        }
        @Override public long getChemicalTankCapacity(int tank) {
            PlayerIdeaStorage storage = connector.storage();
            return valid(tank) && storage != null ? storage.maxChemicalPerType() : 0;
        }
        @Override public boolean isValid(int tank, ChemicalStack stack) {
            return valid(tank) && !stack.isEmpty() && connector.allowsInput(side,
                    ConnectorResource.chemical(MekanismAPI.CHEMICAL_REGISTRY.getKey(stack.getChemical())));
        }
        @Override public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
            PlayerIdeaStorage storage = connector.storage();
            if (!isValid(tank, stack) || storage == null) return stack;
            ResourceLocation id = MekanismAPI.CHEMICAL_REGISTRY.getKey(stack.getChemical());
            long accepted = action.simulate() ? storage.simulateInsertChemical(id, stack.getAmount())
                    : storage.insertChemical(id, stack.getAmount());
            return stack.copyWithAmount(stack.getAmount() - accepted);
        }
        @Override public ChemicalStack extractChemical(int tank, long amount, Action action) {
            PlayerIdeaStorage storage = connector.storage();
            ResourceLocation id = connector.resource(tank).chemical();
            if (!connector.sideMode(tank, side).output || storage == null || id == null || stack(id, 1).isEmpty()) return ChemicalStack.EMPTY;
            return stack(id, action.simulate() ? storage.simulateExtractChemical(id, amount) : storage.extractChemical(id, amount));
        }
    }
}
