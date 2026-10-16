package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.storage.connector.ConnectorHandlers;
import com.moratan251.psitweaks.common.storage.connector.ConnectorTransfers;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Bounded scans of an endpoint or the warehouse's type list; never loops once per requested unit.
 * <p>
 * The {@code *Exact} variants move the full amount of a single resource type (item or fluid including its data
 * components) or nothing. Both sides are simulated first: the first matching type whose whole amount the source can
 * give and the destination can take (per-type cap and type limit included) is moved by the ordinary transfer.
 * Several types are never combined, because handler simulations do not accumulate across different resources.
 * A handler that breaks its own simulation can still end with less; what really moved is kept, never discarded.
 */
public final class IdeaStorageResourceTransfers {
    private IdeaStorageResourceTransfers() { }

    public static long amountForPower(double power, int units, long maximum) {
        if (!Double.isFinite(power) || power <= 0) return 0;
        double amount = power * units;
        return amount >= maximum ? maximum : (long) amount;
    }

    public static int depositItems(PlayerIdeaStorage storage, IItemHandler source, Predicate<ItemStack> filter, int maximum) {
        int moved = 0;
        for (int slot = 0; slot < source.getSlots() && moved < maximum; slot++) {
            ItemStack preview = source.extractItem(slot, maximum - moved, true);
            if (preview.isEmpty() || !filter.test(preview)) continue;
            try (var reservation = storage.reserveItemInsertion(preview, Math.min(preview.getCount(), maximum - moved))) {
                if (reservation == null) continue;
                ItemStack extracted = source.extractItem(slot, (int) reservation.amount(), false);
                int actualAmount = extracted.isEmpty() ? 0 : extracted.getCount();
                if (!reservation.commit(ItemResourceKey.of(extracted).orElse(null), actualAmount))
                    throw new IllegalStateException("Unexpected item extraction; actual resource preserved in Ideaspace Storage");
                moved += actualAmount;
            }
        }
        return moved;
    }

    public static int withdrawItems(PlayerIdeaStorage storage, IItemHandler target, Predicate<ItemStack> filter, int maximum) {
        int moved = 0;
        for (var entry : storage.itemEntries()) {
            if (moved >= maximum) break;
            if (filter.test(entry.getKey().template()))
                moved += ConnectorTransfers.pushItemBatch(storage, entry.getKey(), target, maximum - moved);
        }
        return moved;
    }

    public static int depositFluids(PlayerIdeaStorage storage, IFluidHandler source, Predicate<FluidStack> filter, int maximum) {
        int moved = 0;
        for (int tank = 0; tank < source.getTanks() && moved < maximum; tank++) {
            FluidStack candidate = source.getFluidInTank(tank);
            if (candidate.isEmpty() || !filter.test(candidate)) continue;
            FluidStack preview = source.drain(candidate.copyWithAmount(maximum - moved), IFluidHandler.FluidAction.SIMULATE);
            if (preview.isEmpty() || !filter.test(preview)) continue;
            try (var reservation = storage.reserveFluidInsertion(preview, Math.min(preview.getAmount(), maximum - moved))) {
                if (reservation == null) continue;
                FluidStack extracted = source.drain(preview.copyWithAmount((int) reservation.amount()), IFluidHandler.FluidAction.EXECUTE);
                int actualAmount = extracted.isEmpty() ? 0 : extracted.getAmount();
                if (!reservation.commit(FluidResourceKey.of(extracted).orElse(null), actualAmount))
                    throw new IllegalStateException("Unexpected fluid extraction; actual resource preserved in Ideaspace Storage");
                moved += actualAmount;
            }
        }
        return moved;
    }

    public static int withdrawFluids(PlayerIdeaStorage storage, IFluidHandler target, Predicate<FluidStack> filter, int maximum) {
        int moved = 0;
        for (var entry : storage.fluidEntries()) {
            if (moved >= maximum) break;
            if (filter.test(entry.getKey().template()))
                moved += ConnectorTransfers.pushFluid(storage, entry.getKey(), target, maximum - moved);
        }
        return moved;
    }

    /** Slots are independent, so the per-slot previews of one item type add up to what the deposit loop extracts. */
    public static int depositItemsExact(PlayerIdeaStorage storage, IItemHandler source, Predicate<ItemStack> filter, int amount) {
        if (amount <= 0) return 0;
        Map<ItemResourceKey, Long> available = new LinkedHashMap<>();
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack preview = source.extractItem(slot, amount, true);
            if (preview.isEmpty() || !filter.test(preview)) continue;
            long count = Math.min(preview.getCount(), amount);
            ItemResourceKey.of(preview).ifPresent(key -> available.merge(key, count, (sum, more) -> cappedSum(sum, more, amount)));
        }
        for (var entry : available.entrySet()) {
            ItemStack template = entry.getKey().template();
            if (entry.getValue() < amount || storage.simulateInsert(template, amount) < amount) continue;
            return depositItems(storage, source, stack -> ItemStack.isSameItemSameComponents(template, stack) && filter.test(stack), amount);
        }
        return 0;
    }

    public static int withdrawItemsExact(PlayerIdeaStorage storage, IItemHandler target, Predicate<ItemStack> filter, int amount) {
        if (amount <= 0) return 0;
        for (var entry : storage.itemEntries()) {
            ItemStack template = entry.getKey().template();
            if (entry.getValue() < amount || !filter.test(template) || storage.simulateExtract(entry.getKey(), amount) < amount) continue;
            // One stacked insertion of one item type spreads over independent slots, so its simulation is reliable;
            // a connector's slots share one storage and are simulated through its input slot.
            if (!ConnectorHandlers.simulateInsertStacked(target, template.copyWithCount(amount)).isEmpty()) continue;
            return ConnectorTransfers.pushItemBatch(storage, entry.getKey(), target, amount);
        }
        return 0;
    }

    /**
     * IFluidHandler cannot drain a chosen tank, so the amount must come from one drain request; the same fluid split
     * across several tanks is not added up.
     */
    public static int depositFluidsExact(PlayerIdeaStorage storage, IFluidHandler source, Predicate<FluidStack> filter, int amount) {
        if (amount <= 0) return 0;
        Set<FluidResourceKey> tried = new HashSet<>();
        for (int tank = 0; tank < source.getTanks(); tank++) {
            FluidStack candidate = source.getFluidInTank(tank);
            if (candidate.isEmpty() || !filter.test(candidate)) continue;
            FluidResourceKey key = FluidResourceKey.of(candidate).orElse(null);
            if (key == null || !tried.add(key)) continue;
            FluidStack request = candidate.copyWithAmount(amount);
            FluidStack preview = source.drain(request, IFluidHandler.FluidAction.SIMULATE);
            if (preview.getAmount() < amount || !FluidStack.isSameFluidSameComponents(preview, request)
                    || storage.simulateInsertFluid(request, amount) < amount) continue;
            try (var reservation = storage.reserveFluidInsertion(request, amount)) {
                if (reservation == null || reservation.amount() < amount) return 0;
                FluidStack extracted = source.drain(request, IFluidHandler.FluidAction.EXECUTE);
                int actualAmount = extracted.isEmpty() ? 0 : extracted.getAmount();
                if (!reservation.commit(FluidResourceKey.of(extracted).orElse(null), actualAmount))
                    throw new IllegalStateException("Unexpected fluid extraction; actual resource preserved in Ideaspace Storage");
                return actualAmount;
            }
        }
        return 0;
    }

    public static int withdrawFluidsExact(PlayerIdeaStorage storage, IFluidHandler target, Predicate<FluidStack> filter, int amount) {
        if (amount <= 0) return 0;
        for (var entry : storage.fluidEntries()) {
            FluidStack template = entry.getKey().template();
            if (entry.getValue() < amount || !filter.test(template) || storage.simulateExtractFluid(entry.getKey(), amount) < amount) continue;
            if (target.fill(template.copyWithAmount(amount), IFluidHandler.FluidAction.SIMULATE) < amount) continue;
            return ConnectorTransfers.pushFluid(storage, entry.getKey(), target, amount);
        }
        return 0;
    }

    /** Adds without overflowing and never beyond {@code cap}; both inputs are expected to be non-negative. */
    public static long cappedSum(long sum, long more, long cap) {
        if (more <= 0) return Math.min(sum, cap);
        return sum >= cap || more >= cap - sum ? cap : sum + more;
    }
}
