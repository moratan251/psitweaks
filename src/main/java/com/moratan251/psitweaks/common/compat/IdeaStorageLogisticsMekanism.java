package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** No Mekanism types are referenced by the spell implementation. */
public final class IdeaStorageLogisticsMekanism {
    private IdeaStorageLogisticsMekanism() { }

    public static long transfer(PlayerIdeaStorage storage, Level level, BlockPos pos, Direction face,
                                Predicate<ResourceLocation> filter, long maximum, boolean deposit) {
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapabilityIfLoaded(level, pos, face);
        return handler == null ? 0 : transfer(storage, handler, filter, maximum, deposit);
    }

    public static long transfer(PlayerIdeaStorage storage, IChemicalHandler handler,
                                Predicate<ResourceLocation> filter, long maximum, boolean deposit) {
        long moved = 0;
        if (!deposit) {
            for (var entry : storage.chemicalEntries()) {
                if (moved >= maximum) break;
                if (filter.test(entry.getKey())) moved += ConnectorMekanism.push(storage, entry.getKey(), handler, maximum - moved);
            }
            return moved;
        }
        for (int tank = 0; tank < handler.getChemicalTanks() && moved < maximum; tank++) {
            var preview = handler.extractChemical(tank, maximum - moved, Action.SIMULATE);
            if (preview.isEmpty()) continue;
            var id = MekanismAPI.CHEMICAL_REGISTRY.getKey(preview.getChemical());
            if (!filter.test(id)) continue;
            try (var reservation = storage.reserveChemicalInsertion(id, Math.min(preview.getAmount(), maximum - moved))) {
                if (reservation == null) continue;
                var extracted = handler.extractChemical(tank, reservation.amount(), Action.EXECUTE);
                if (!extracted.isEmpty() && extracted.getChemical() != preview.getChemical())
                    throw new IllegalStateException("Chemical handler changed resource identity during extraction");
                reservation.commit(extracted.getAmount());
                moved += extracted.getAmount();
            }
        }
        return moved;
    }
}
