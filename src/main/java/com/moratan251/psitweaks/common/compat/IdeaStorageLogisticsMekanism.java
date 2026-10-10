package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.storage.connector.ConnectorCapabilities;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * No Mekanism types are referenced by the spell implementation. Mekanism 10 keeps gases, infuse types,
 * pigments and slurries in separate handlers; the filter sees the plain registry ID shown to players.
 */
public final class IdeaStorageLogisticsMekanism {
    private IdeaStorageLogisticsMekanism() { }

    public static long transfer(PlayerIdeaStorage storage, Level level, BlockPos pos, Direction face,
                                Predicate<ResourceLocation> filter, long maximum, boolean deposit) {
        long moved = 0;
        if (!deposit) {
            for (var entry : storage.chemicalEntries()) {
                if (moved >= maximum) break;
                ResourceLocation key = entry.getKey();
                int slash = key.getPath().indexOf('/');
                if (slash < 0 || !filter.test(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring(slash + 1))))
                    continue;
                moved += ConnectorMekanism.push(storage, key, level, pos, face, maximum - moved);
            }
            return moved;
        }
        moved += deposit(storage, ConnectorCapabilities.get(level, pos, face, Capabilities.GAS_HANDLER), "gas", filter, maximum - moved);
        if (moved < maximum)
            moved += deposit(storage, ConnectorCapabilities.get(level, pos, face, Capabilities.INFUSION_HANDLER), "infuse", filter, maximum - moved);
        if (moved < maximum)
            moved += deposit(storage, ConnectorCapabilities.get(level, pos, face, Capabilities.PIGMENT_HANDLER), "pigment", filter, maximum - moved);
        if (moved < maximum)
            moved += deposit(storage, ConnectorCapabilities.get(level, pos, face, Capabilities.SLURRY_HANDLER), "slurry", filter, maximum - moved);
        return moved;
    }

    static <C extends Chemical<C>, S extends ChemicalStack<C>> long deposit(PlayerIdeaStorage storage, IChemicalHandler<C, S> handler,
            String kind, Predicate<ResourceLocation> filter, long maximum) {
        if (handler == null) return 0;
        long moved = 0;
        for (int tank = 0; tank < handler.getTanks() && moved < maximum; tank++) {
            S preview = handler.extractChemical(tank, maximum - moved, Action.SIMULATE);
            if (preview.isEmpty() || !filter.test(preview.getTypeRegistryName())) continue;
            var id = IdeaStorageMekanismIntegration.key(kind, preview.getTypeRegistryName());
            try (var reservation = storage.reserveChemicalInsertion(id, Math.min(preview.getAmount(), maximum - moved))) {
                if (reservation == null) continue;
                S extracted = handler.extractChemical(tank, reservation.amount(), Action.EXECUTE);
                long actualAmount = extracted.isEmpty() ? 0 : extracted.getAmount();
                var actualId = extracted.isEmpty() ? null : IdeaStorageMekanismIntegration.key(kind, extracted.getTypeRegistryName());
                if (!reservation.commit(actualId, actualAmount))
                    throw new IllegalStateException("Unexpected chemical extraction; actual resource preserved in Ideaspace Storage");
                moved += actualAmount;
            }
        }
        return moved;
    }
}
