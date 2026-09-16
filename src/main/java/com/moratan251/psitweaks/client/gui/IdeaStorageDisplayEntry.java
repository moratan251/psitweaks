package com.moratan251.psitweaks.client.gui;

import com.moratan251.psitweaks.client.compat.IdeaStorageChemicalClientCompat;
import com.moratan251.psitweaks.common.network.MessageIdeaStorageSync;
import java.math.BigDecimal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/** Item / Fluid / Chemicalを同じ仮想グリッドへ表示するためのクライアント専用表現。 */
public record IdeaStorageDisplayEntry(long entryId, Kind kind, ItemStack itemTemplate, FluidStack fluidTemplate,
                                      @Nullable ResourceLocation chemicalId, long amount) {
    public enum Kind {
        ITEM,
        FLUID,
        CHEMICAL,
        ENERGY
    }

    public static IdeaStorageDisplayEntry item(MessageIdeaStorageSync.Entry entry) {
        return new IdeaStorageDisplayEntry(entry.entryId(), Kind.ITEM, entry.template(), FluidStack.EMPTY, null, entry.count());
    }

    public static IdeaStorageDisplayEntry fluid(MessageIdeaStorageSync.FluidEntry entry) {
        return new IdeaStorageDisplayEntry(entry.entryId(), Kind.FLUID, ItemStack.EMPTY, entry.template(), null, entry.amount());
    }

    public static IdeaStorageDisplayEntry chemical(MessageIdeaStorageSync.ChemicalEntry entry) {
        return new IdeaStorageDisplayEntry(entry.entryId(), Kind.CHEMICAL, ItemStack.EMPTY, FluidStack.EMPTY,
                entry.chemicalId(), entry.amount());
    }

    public Component displayName() {
        return switch (kind) {
            case ITEM -> itemTemplate.getHoverName();
            case FLUID -> fluidTemplate.getDisplayName();
            case CHEMICAL -> IdeaStorageChemicalClientCompat.displayName(chemicalId);
            case ENERGY -> Component.translatable("gui.psitweaks.idea_storage.energy");
        };
    }

    public ResourceLocation resourceId() {
        return switch (kind) {
            case ITEM -> BuiltInRegistries.ITEM.getKey(itemTemplate.getItem());
            case FLUID -> BuiltInRegistries.FLUID.getKey(fluidTemplate.getFluid());
            case CHEMICAL -> chemicalId;
            case ENERGY -> ResourceLocation.fromNamespaceAndPath("psitweaks", "fe");
        };
    }

    public int displayAmountScale() {
        return kind == Kind.ITEM || kind == Kind.ENERGY
                ? IdeaStorageAmountFormatter.ITEM_SCALE
                : IdeaStorageAmountFormatter.BUCKET_SCALE;
    }

    public BigDecimal amountInDisplayUnits() {
        return IdeaStorageAmountFormatter.asDisplayAmount(amount, displayAmountScale());
    }
    public IdeaStorageDisplayEntry(Kind kind, ItemStack item, FluidStack fluid, ResourceLocation chemical, long amount) {
        this(0, kind, item, fluid, chemical, amount);
    }
    public static IdeaStorageDisplayEntry energy(long amount) {
        return new IdeaStorageDisplayEntry(0, Kind.ENERGY, ItemStack.EMPTY, FluidStack.EMPTY, null, amount);
    }
}
