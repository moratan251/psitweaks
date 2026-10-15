package com.moratan251.psitweaks.common.spells.mode;

import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.moratan251.psitweaksqol.api.PsitweaksValueKind;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * Modes of the merged ideaspace deposit and withdraw tricks: the resource kind, and for items, fluids and chemicals
 * also exact transfer. The plain kinds are the block resource options, so selectors and tricks share their badges.
 * Not registered globally.
 */
public final class IdeaStorageResourceModes {
    public static final PsitweaksModeOption ITEM_EXACT = exact(BlockResourceModes.ITEM);
    public static final PsitweaksModeOption FLUID_EXACT = exact(BlockResourceModes.FLUID);
    public static final PsitweaksModeOption CHEMICAL_EXACT = exact(BlockResourceModes.CHEMICAL);
    /** FE has no exact transfer. */
    public static final List<PsitweaksModeOption> TRANSFER = List.of(
            BlockResourceModes.ITEM, ITEM_EXACT, BlockResourceModes.FLUID, FLUID_EXACT,
            BlockResourceModes.CHEMICAL, CHEMICAL_EXACT, BlockResourceModes.ENERGY);
    /** Type counts exist for items, fluids and chemicals only. */
    public static final List<PsitweaksModeOption> TYPE_COUNT = List.of(
            BlockResourceModes.ITEM, BlockResourceModes.FLUID, BlockResourceModes.CHEMICAL);

    private IdeaStorageResourceModes() {
    }

    public static boolean isExact(PsitweaksModeOption option) {
        return option == ITEM_EXACT || option == FLUID_EXACT || option == CHEMICAL_EXACT;
    }

    /** The resource kind of a transfer option: an exact variant maps to its plain kind. */
    public static PsitweaksModeOption kindOf(PsitweaksModeOption option) {
        if (option == ITEM_EXACT) return BlockResourceModes.ITEM;
        if (option == FLUID_EXACT) return BlockResourceModes.FLUID;
        if (option == CHEMICAL_EXACT) return BlockResourceModes.CHEMICAL;
        return option;
    }

    /** The option saved under {@code serializedId} among {@code options}, or the first one. */
    public static PsitweaksModeOption bySerializedId(List<PsitweaksModeOption> options, String serializedId) {
        for (PsitweaksModeOption option : options) {
            if (option.serializedId().equals(serializedId)) {
                return option;
            }
        }
        return options.get(0);
    }

    private static PsitweaksModeOption exact(PsitweaksModeOption kind) {
        String id = kind.serializedId() + "_exact";
        return new PsitweaksModeOption(location(id), id, kind.buttonLabel() + "=", "psitweaks.mode." + id,
                kind.listTranslationKey(), kind.sortOrder() + 1, location("mode/" + id), PsitweaksValueKind.PLAIN);
    }

    private static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, path);
    }
}
