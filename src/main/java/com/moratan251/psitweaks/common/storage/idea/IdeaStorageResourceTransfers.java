package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.storage.connector.ConnectorTransfers;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/** Bounded scans of an endpoint or the warehouse's type list; never loops once per requested unit. */
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
                if (!extracted.isEmpty() && !ItemStack.isSameItemSameComponents(preview, extracted))
                    throw new IllegalStateException("Item handler changed resource identity during extraction");
                reservation.commit(extracted.getCount());
                moved += extracted.getCount();
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
                if (!extracted.isEmpty() && !FluidResourceKey.of(preview).equals(FluidResourceKey.of(extracted)))
                    throw new IllegalStateException("Fluid handler changed resource identity during extraction");
                reservation.commit(extracted.getAmount());
                moved += extracted.getAmount();
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
}
