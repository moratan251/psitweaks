package com.moratan251.psitweaks.common.network;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Completed client view assembled from bounded transport frames; never sent as one packet. */
public record MessageIdeaStorageSync(List<Entry> entries, List<FluidEntry> fluidEntries,
                                     List<ChemicalEntry> chemicalEntries,
                                     int maxItemTypes, int maxFluidTypes, int maxChemicalTypes,
                                     boolean loadFailed) {

    public record Entry(long entryId, ItemStack template, long count) {
        public Entry(ItemStack template, long count) { this(0, template, count); }
    }

    public record FluidEntry(long entryId, FluidStack template, long amount) {
        public FluidEntry(FluidStack template, long amount) { this(0, template, amount); }
    }

    public record ChemicalEntry(long entryId, ResourceLocation chemicalId, long amount) {
        public ChemicalEntry(ResourceLocation chemicalId, long amount) { this(0, chemicalId, amount); }
    }

}
