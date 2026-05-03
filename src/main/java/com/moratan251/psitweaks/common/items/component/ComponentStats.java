package com.moratan251.psitweaks.common.items.component;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.common.item.component.ItemCADComponent;

public final class ComponentStats {
    private ComponentStats() {
    }

    public static void registerAssemblyStats() {
        addAssemblyStats(PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get(), 250, 60);
        addAssemblyStats(PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL.get(), 110, 640);
        addAssemblyStats(PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL.get(), 125, 1050);
        addAssemblyStats(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA.get(), 150, 1600);
        addAssemblyStats(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA.get(), 175, 1400);
        addAssemblyStats(PsitweaksItems.CAD_ASSEMBLY_PSYCHEONIC_METAL.get(), 200, 2000);
    }

    private static void addAssemblyStats(ItemCADComponent assembly, int efficiency, int potency) {
        ItemCADComponent.addStatToStack(assembly, EnumCADStat.EFFICIENCY, efficiency);
        ItemCADComponent.addStatToStack(assembly, EnumCADStat.POTENCY, potency);
    }
}
