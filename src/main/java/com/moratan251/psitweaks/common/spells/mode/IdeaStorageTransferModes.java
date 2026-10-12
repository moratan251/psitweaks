package com.moratan251.psitweaks.common.spells.mode;

import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.moratan251.psitweaksqol.api.PsitweaksValueKind;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Transfer modes of the ideaspace logistics tricks. The Psitweaks mode menu has a single axis, so item tricks list
 * every filter type once as a partial transfer and once as an exact one. The filter type keeps its old NBT key and
 * the exact flag is stored separately, so spells saved before exact transfer existed load as partial transfers.
 * These options are deliberately not registered globally: only these tricks may offer them.
 */
public final class IdeaStorageTransferModes {
    public static final String TAG = "psitweaksTransferMode";
    private static final String EXACT_VALUE = "exact";

    public static final PsitweaksModeOption PARTIAL = option("idea_transfer_partial", "P", "partial", "partial",
            PsitweaksModeOptions.STRING.listTranslationKey(), PsitweaksValueKind.PLAIN);
    public static final PsitweaksModeOption EXACT = option("idea_transfer_exact", "=", "exact", "exact",
            PsitweaksModeOptions.STRING.listTranslationKey(), PsitweaksValueKind.PLAIN);
    public static final PsitweaksModeOption STRING_EXACT = exactVariant(PsitweaksModeOptions.STRING, "string_exact", "S=",
            "string_exact");
    public static final PsitweaksModeOption ITEM_EXACT = exactVariant(PsitweaksModeOptions.ITEM, "item_exact", "I=",
            "item_exact");
    public static final PsitweaksModeOption ITEM_STRICT_EXACT = exactVariant(PsitweaksModeOptions.ITEM_STRICT,
            "item_strict_exact", "I*=", "item_s_exact");

    /** Fluid and chemical tricks have a fixed String filter and only choose the transfer mode. */
    public static final List<PsitweaksModeOption> TRANSFER_ONLY = List.of(PARTIAL, EXACT);
    /** Item tricks: filter type, then the same filter types with exact transfer. Fits the menu without scrolling. */
    public static final List<PsitweaksModeOption> ITEM_FILTERS = List.of(
            PsitweaksModeOptions.STRING, PsitweaksModeOptions.ITEM, PsitweaksModeOptions.ITEM_STRICT);
    public static final List<PsitweaksModeOption> ITEM_FILTERS_WITH_EXACT = List.of(
            PsitweaksModeOptions.STRING, PsitweaksModeOptions.ITEM, PsitweaksModeOptions.ITEM_STRICT,
            STRING_EXACT, ITEM_EXACT, ITEM_STRICT_EXACT);

    private IdeaStorageTransferModes() {
    }

    public static boolean isExact(PsitweaksModeOption option) {
        return option != null && (same(option, EXACT) || same(option, STRING_EXACT) || same(option, ITEM_EXACT)
                || same(option, ITEM_STRICT_EXACT));
    }

    /** The filter type of an item-trick option; exact variants map to their partial counterpart. */
    public static PsitweaksModeOption filterOf(PsitweaksModeOption option) {
        if (same(option, STRING_EXACT)) return PsitweaksModeOptions.STRING;
        if (same(option, ITEM_EXACT)) return PsitweaksModeOptions.ITEM;
        if (same(option, ITEM_STRICT_EXACT)) return PsitweaksModeOptions.ITEM_STRICT;
        for (PsitweaksModeOption filter : ITEM_FILTERS) {
            if (same(option, filter)) return filter;
        }
        return PsitweaksModeOptions.STRING;
    }

    public static PsitweaksModeOption withExact(PsitweaksModeOption filter, boolean exact) {
        PsitweaksModeOption normalized = filterOf(filter);
        if (!exact) return normalized;
        if (same(normalized, PsitweaksModeOptions.ITEM)) return ITEM_EXACT;
        if (same(normalized, PsitweaksModeOptions.ITEM_STRICT)) return ITEM_STRICT_EXACT;
        return STRING_EXACT;
    }

    public static boolean readExact(CompoundTag tag) {
        return EXACT_VALUE.equals(tag.getString(TAG));
    }

    /** Partial transfer writes nothing, which is exactly what older versions saved. */
    public static void writeExact(CompoundTag tag, boolean exact) {
        if (exact) {
            tag.putString(TAG, EXACT_VALUE);
        } else {
            tag.remove(TAG);
        }
    }

    private static boolean same(PsitweaksModeOption left, PsitweaksModeOption right) {
        return left != null && left.id().equals(right.id());
    }

    private static PsitweaksModeOption option(String id, String button, String langSuffix, String overlay,
                                              String listKey, PsitweaksValueKind kind) {
        return new PsitweaksModeOption(location(id), id, button, "psitweaks.mode.idea_transfer." + langSuffix, listKey,
                0, location("mode/" + overlay), kind);
    }

    private static PsitweaksModeOption exactVariant(PsitweaksModeOption filter, String id, String button, String overlay) {
        return new PsitweaksModeOption(location(id), id, button, "psitweaks.mode.idea_transfer." + id,
                filter.listTranslationKey(), filter.sortOrder() + 1, location("mode/" + overlay), filter.valueKind());
    }

    private static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, path);
    }
}
