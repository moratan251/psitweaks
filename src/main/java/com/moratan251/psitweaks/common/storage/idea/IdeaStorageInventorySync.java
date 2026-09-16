package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync.QuantityUpdate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

/** Last sent inventory, owned by one menu. Resource keys are immutable; quantities must be snapshotted. */
public final class IdeaStorageInventorySync {
    private List<Map.Entry<ItemResourceKey, Long>> items = List.of();
    private List<Map.Entry<FluidResourceKey, Long>> fluids = List.of();
    private List<Map.Entry<ResourceLocation, Long>> chemicals = List.of();

    public MessageIdeaStorageSync createUpdate(PlayerIdeaStorage storage, int containerId, UUID session,
                                              long revision, boolean forceFull) {
        var currentItems = snapshot(storage.itemEntries());
        var currentFluids = snapshot(storage.fluidEntries());
        var currentChemicals = snapshot(storage.chemicalEntries());
        MessageIdeaStorageSync message;
        if (forceFull || !sameKeys(items, currentItems) || !sameKeys(fluids, currentFluids)
                || !sameKeys(chemicals, currentChemicals)) {
            message = new MessageIdeaStorageSync(containerId, session, revision, true,
                    currentItems.stream().map(e -> new MessageIdeaStorageSync.Entry(e.getKey().template(), e.getValue())).toList(),
                    currentFluids.stream().map(e -> new MessageIdeaStorageSync.FluidEntry(e.getKey().template(), e.getValue())).toList(),
                    currentChemicals.stream().map(e -> new MessageIdeaStorageSync.ChemicalEntry(e.getKey(), e.getValue())).toList(),
                    storage.maxItemTypes(), storage.maxFluidTypes(), storage.maxChemicalTypes(),
                    storage.energy(), storage.maxEnergy(), storage.isLoadFailed());
        } else {
            message = MessageIdeaStorageSync.quantityUpdate(containerId, session, revision, storage.energy(), storage.maxEnergy(),
                    changes(items, currentItems), changes(fluids, currentFluids), changes(chemicals, currentChemicals));
        }
        items = currentItems;
        fluids = currentFluids;
        chemicals = currentChemicals;
        return message;
    }

    private static <K> List<Map.Entry<K, Long>> snapshot(List<Map.Entry<K, Long>> entries) {
        // The storage's Map.Entry instances are live views; retaining them would lose the old amounts.
        return entries.stream().map(Map.Entry::copyOf).toList();
    }

    private static <K> boolean sameKeys(List<Map.Entry<K, Long>> previous, List<Map.Entry<K, Long>> current) {
        if (previous.size() != current.size()) return false;
        for (int i = 0; i < previous.size(); i++) {
            if (!previous.get(i).getKey().equals(current.get(i).getKey())) return false;
        }
        return true;
    }

    private static <K> List<QuantityUpdate> changes(List<Map.Entry<K, Long>> previous, List<Map.Entry<K, Long>> current) {
        var updates = new ArrayList<QuantityUpdate>();
        for (int i = 0; i < current.size(); i++) {
            if (!previous.get(i).getValue().equals(current.get(i).getValue())) {
                updates.add(new QuantityUpdate(i, current.get(i).getValue()));
            }
        }
        return updates;
    }
}
