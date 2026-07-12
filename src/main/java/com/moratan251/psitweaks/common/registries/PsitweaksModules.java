package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.PsitweaksMekanismItems;
import com.moratan251.psitweaks.common.items.modules.ModulePhenomenonInterferenceEnhancementUnit;
import com.moratan251.psitweaks.common.items.modules.ModulePsyonCapacityUnit;
import com.moratan251.psitweaks.common.items.modules.ModulePsyonSupplyingUnit;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksModules {
    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister(Psitweaks.MOD_ID);

    public static final ModuleRegistryObject<ModulePsyonSupplyingUnit> PSYON_SUPPLYING_UNIT =
            MODULES.registerInstanced(
                    "psyon_supplying_unit",
                    ModulePsyonSupplyingUnit::new,
                    () -> PsitweaksMekanismItems.MODULE_PSYON_SUPPLYING.getDelegate(),
                    builder -> builder.maxStackSize(8)
            );

    public static final ModuleRegistryObject<ModulePsyonCapacityUnit> PSYON_CAPACITY_UNIT =
            MODULES.registerInstanced(
                    "psyon_capacity_unit",
                    ModulePsyonCapacityUnit::new,
                    () -> PsitweaksMekanismItems.MODULE_PSYON_CAPACITY.getDelegate(),
                    builder -> builder.maxStackSize(8)
            );

    public static final ModuleRegistryObject<ModulePhenomenonInterferenceEnhancementUnit> PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT =
            MODULES.registerInstanced(
                    "phenomenon_interference_enhancement_unit",
                    ModulePhenomenonInterferenceEnhancementUnit::new,
                    () -> PsitweaksMekanismItems.MODULE_PHENOMENON_INTERFERENCE_ENHANCEMENT.getDelegate(),
                    builder -> builder.maxStackSize(8)
            );

    private PsitweaksModules() {
    }

    public static void register(IEventBus eventBus) {
        MODULES.register(eventBus);
    }
}
