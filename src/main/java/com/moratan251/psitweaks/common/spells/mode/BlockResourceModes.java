package com.moratan251.psitweaks.common.spells.mode;

import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import com.moratan251.psitweaksqol.api.PsitweaksValueKind;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/** Resource kinds read by Operator: Block Resource Amount and Operator: Block Free Capacity. Not registered globally. */
public final class BlockResourceModes {
    public static final PsitweaksModeOption ITEM = option("resource_item", "I");
    public static final PsitweaksModeOption FLUID = option("resource_fluid", "F");
    public static final PsitweaksModeOption CHEMICAL = option("resource_chemical", "C");
    public static final PsitweaksModeOption ENERGY = option("resource_energy", "FE");
    public static final List<PsitweaksModeOption> ALL = List.of(ITEM, FLUID, CHEMICAL, ENERGY);

    private BlockResourceModes() {
    }

    /** The saved option, or Item when the tag holds none of these. */
    public static PsitweaksModeOption bySerializedId(String serializedId) {
        for (PsitweaksModeOption option : ALL) {
            if (option.serializedId().equals(serializedId)) {
                return option;
            }
        }
        return ITEM;
    }

    private static PsitweaksModeOption option(String id, String button) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, id);
        return new PsitweaksModeOption(location, id, button, "psitweaks.mode." + id, "psi.datatype.number", 0,
                ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, "mode/" + id),
                PsitweaksValueKind.PLAIN);
    }
}
