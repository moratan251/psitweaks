package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.client.compat.IdeaStorageChemicalClientCompat;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/** Item / Fluid / Chemicalを同じ仮想グリッドへ表示するためのクライアント専用表現。 */
public record IdeaStorageDisplayEntry(Kind kind, ItemStack itemTemplate, FluidStack fluidTemplate,
                                      @Nullable ResourceLocation chemicalId, long amount) {
    public enum Kind {
        ITEM,
        FLUID,
        CHEMICAL
    }

    public static IdeaStorageDisplayEntry item(MessageIdeaStorageSync.Entry entry) {
        return new IdeaStorageDisplayEntry(Kind.ITEM, entry.template(), FluidStack.EMPTY, null, entry.count());
    }

    public static IdeaStorageDisplayEntry fluid(MessageIdeaStorageSync.FluidEntry entry) {
        return new IdeaStorageDisplayEntry(Kind.FLUID, ItemStack.EMPTY, entry.template(), null, entry.amount());
    }

    public static IdeaStorageDisplayEntry chemical(MessageIdeaStorageSync.ChemicalEntry entry) {
        return new IdeaStorageDisplayEntry(Kind.CHEMICAL, ItemStack.EMPTY, FluidStack.EMPTY,
                entry.chemicalId(), entry.amount());
    }

    public Component displayName() {
        return switch (kind) {
            case ITEM -> itemTemplate.getHoverName();
            case FLUID -> fluidTemplate.getHoverName();
            case CHEMICAL -> IdeaStorageChemicalClientCompat.displayName(chemicalId);
        };
    }

    public ResourceLocation resourceId() {
        return switch (kind) {
            case ITEM -> BuiltInRegistries.ITEM.getKey(itemTemplate.getItem());
            case FLUID -> BuiltInRegistries.FLUID.getKey(fluidTemplate.getFluid());
            case CHEMICAL -> chemicalId;
        };
    }
}
