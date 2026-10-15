package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.storage.connector.ConnectorCapabilities;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageResourceTransfers;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * No Mekanism types are referenced by the spell implementation. Mekanism 10 keeps gases, infuse types,
 * pigments and slurries in separate handlers; the filter sees the plain registry ID shown to players.
 */
public final class IdeaStorageLogisticsMekanism {
    /** Mekanism 10 chemical kinds in the order every lookup by plain ID tries them. */
    private static final List<String> KINDS = List.of("gas", "infuse", "pigment", "slurry");

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

    /**
     * All-or-nothing for one chemical, simulated on both sides first. Tanks can be extracted individually, so per-tank
     * previews of one chemical add up; insertion is a single handler-level request. One transfer never mixes kinds:
     * depositing tries the gas, infusion, pigment and slurry handlers in that order.
     */
    public static long transferExact(PlayerIdeaStorage storage, Level level, BlockPos pos, Direction face,
                                     Predicate<ResourceLocation> filter, long amount, boolean deposit) {
        if (amount <= 0) return 0;
        if (!deposit) {
            for (var entry : storage.chemicalEntries()) {
                ResourceLocation key = entry.getKey();
                ResourceLocation id = plainId(key);
                if (id == null || entry.getValue() < amount || !filter.test(id)
                        || storage.simulateExtractChemical(key, amount) < amount || !accepts(level, pos, face, key, amount)) continue;
                return ConnectorMekanism.push(storage, key, level, pos, face, amount);
            }
            return 0;
        }
        long moved = depositExact(storage, ConnectorCapabilities.get(level, pos, face, Capabilities.GAS_HANDLER), "gas", filter, amount);
        if (moved == 0)
            moved = depositExact(storage, ConnectorCapabilities.get(level, pos, face, Capabilities.INFUSION_HANDLER), "infuse", filter, amount);
        if (moved == 0)
            moved = depositExact(storage, ConnectorCapabilities.get(level, pos, face, Capabilities.PIGMENT_HANDLER), "pigment", filter, amount);
        if (moved == 0)
            moved = depositExact(storage, ConnectorCapabilities.get(level, pos, face, Capabilities.SLURRY_HANDLER), "slurry", filter, amount);
        return moved;
    }

    static <C extends Chemical<C>, S extends ChemicalStack<C>> long depositExact(PlayerIdeaStorage storage,
            IChemicalHandler<C, S> handler, String kind, Predicate<ResourceLocation> filter, long amount) {
        if (handler == null) return 0;
        Map<ResourceLocation, Long> available = new LinkedHashMap<>();
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            S preview = handler.extractChemical(tank, amount, Action.SIMULATE);
            if (preview.isEmpty() || !filter.test(preview.getTypeRegistryName())) continue;
            available.merge(preview.getTypeRegistryName(), Math.min(preview.getAmount(), amount),
                    (sum, more) -> IdeaStorageResourceTransfers.cappedSum(sum, more, amount));
        }
        for (var entry : available.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (entry.getValue() < amount
                    || storage.simulateInsertChemical(IdeaStorageMekanismIntegration.key(kind, id), amount) < amount) continue;
            return deposit(storage, handler, kind, id::equals, amount);
        }
        return 0;
    }

    private static boolean accepts(Level level, BlockPos pos, Direction face, ResourceLocation key, long amount) {
        ResourceLocation id = plainId(key);
        if (id == null) return false;
        return switch (key.getPath().substring(0, key.getPath().indexOf('/'))) {
            case "gas" -> accepts(ConnectorCapabilities.get(level, pos, face, Capabilities.GAS_HANDLER), MekanismAPI.gasRegistry(), GasStack::new, id, amount);
            case "infuse" -> accepts(ConnectorCapabilities.get(level, pos, face, Capabilities.INFUSION_HANDLER), MekanismAPI.infuseTypeRegistry(), InfusionStack::new, id, amount);
            case "pigment" -> accepts(ConnectorCapabilities.get(level, pos, face, Capabilities.PIGMENT_HANDLER), MekanismAPI.pigmentRegistry(), PigmentStack::new, id, amount);
            case "slurry" -> accepts(ConnectorCapabilities.get(level, pos, face, Capabilities.SLURRY_HANDLER), MekanismAPI.slurryRegistry(), SlurryStack::new, id, amount);
            default -> false;
        };
    }

    private static <C extends Chemical<C>, S extends ChemicalStack<C>> boolean accepts(IChemicalHandler<C, S> handler,
            IForgeRegistry<C> registry, BiFunction<C, Long, S> factory, ResourceLocation id, long amount) {
        if (handler == null) return false;
        C chemical = registry.getValue(id);
        return chemical != null && !chemical.isEmptyType()
                && handler.insertChemical(factory.apply(chemical, amount), Action.SIMULATE).isEmpty();
    }

    /**
     * How much more of this chemical fits (per-type cap and type limit included); unknown IDs have none. A plain ID
     * registered under several kinds means the first of gas, infusion type, pigment and slurry.
     */
    public static long freeCapacity(PlayerIdeaStorage storage, ResourceLocation id) {
        ResourceLocation key = storageKey(id);
        return key == null ? 0 : storage.simulateInsertChemical(key, Long.MAX_VALUE);
    }

    /** The storage key (namespace:kind/path) of a plain chemical ID; a key that is already one stays as it is. */
    @Nullable
    private static ResourceLocation storageKey(ResourceLocation id) {
        if (id == null) return null;
        if (ConnectorMekanism.validChemical(id)) return id;
        for (String kind : KINDS) {
            ResourceLocation key = IdeaStorageMekanismIntegration.key(kind, id);
            if (ConnectorMekanism.validChemical(key)) return key;
        }
        return null;
    }

    /** The plain registry ID of a storage key, or null for a key without a kind. */
    @Nullable
    private static ResourceLocation plainId(ResourceLocation key) {
        int slash = key.getPath().indexOf('/');
        return slash < 0 ? null : ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring(slash + 1));
    }
}
