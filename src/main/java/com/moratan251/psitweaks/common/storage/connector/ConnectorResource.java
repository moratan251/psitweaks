package com.moratan251.psitweaks.common.storage.connector;

import com.moratan251.psitweaks.common.storage.idea.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** Immutable identity of a published resource; never contains actual inventory. */
public record ConnectorResource(Kind kind, ItemResourceKey item, FluidResourceKey fluid, ResourceLocation chemical) {
    public enum Kind { EMPTY, ITEM, FLUID, CHEMICAL, ENERGY }
    public static final ConnectorResource EMPTY = new ConnectorResource(Kind.EMPTY, null, null, null);
    public static final ConnectorResource ENERGY = new ConnectorResource(Kind.ENERGY, null, null, null);

    public static ConnectorResource item(ItemResourceKey key) {
        return new ConnectorResource(Kind.ITEM, key, null, null);
    }

    public static ConnectorResource fluid(FluidResourceKey key) {
        return new ConnectorResource(Kind.FLUID, null, key, null);
    }

    public static ConnectorResource chemical(ResourceLocation id) {
        return new ConnectorResource(Kind.CHEMICAL, null, null, id);
    }

    public long amount(PlayerIdeaStorage storage) {
        if (storage == null) return 0;
        return switch (kind) {
            case ITEM -> storage.simulateExtract(item, Long.MAX_VALUE);
            case FLUID -> storage.simulateExtractFluid(fluid, Long.MAX_VALUE);
            case CHEMICAL -> storage.simulateExtractChemical(chemical, Long.MAX_VALUE);
            case ENERGY -> storage.extractEnergy(Long.MAX_VALUE, true);
            case EMPTY -> 0;
        };
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Kind", kind.name());
        switch (kind) {
            case ITEM -> tag.put("Item", item.save(registries));
            case FLUID -> tag.put("Fluid", fluid.save(registries));
            case CHEMICAL -> tag.putString("Chemical", chemical.toString());
            default -> { }
        }
        return tag;
    }

    public static ConnectorResource load(CompoundTag tag, HolderLookup.Provider registries) {
        return switch (tag.getString("Kind")) {
            case "ITEM" -> ItemResourceKey.parse(registries, tag.getCompound("Item"))
                    .map(ConnectorResource::item).orElse(EMPTY);
            case "FLUID" -> FluidResourceKey.parse(registries, tag.getCompound("Fluid"))
                    .map(ConnectorResource::fluid).orElse(EMPTY);
            case "CHEMICAL" -> {
                ResourceLocation id = ResourceLocation.tryParse(tag.getString("Chemical"));
                yield id == null ? EMPTY : chemical(id);
            }
            case "ENERGY" -> ENERGY;
            default -> EMPTY;
        };
    }

    public ItemStack itemStack(int amount) {
        return item == null || amount <= 0 ? ItemStack.EMPTY : item.template().copyWithCount(amount);
    }

    public FluidStack fluidStack(int amount) {
        return fluid == null || amount <= 0 ? FluidStack.EMPTY : fluid.template().copyWithAmount(amount);
    }
}
