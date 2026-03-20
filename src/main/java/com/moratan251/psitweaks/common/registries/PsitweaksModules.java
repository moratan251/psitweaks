package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.modules.ModulePhenomenonInterferenceEnhancementUnit;
import com.moratan251.psitweaks.common.items.modules.ModulePsyonCapacityUnit;
import com.moratan251.psitweaks.common.items.modules.ModulePsyonSupplyingUnit;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import net.minecraft.world.item.Rarity;

public class PsitweaksModules {

    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister("psitweaks");

    public static final ModuleRegistryObject<ModulePsyonSupplyingUnit> PSYON_SUPPLYING_UNIT =
            MODULES.register("psyon_supplying_unit", ModulePsyonSupplyingUnit::new,
                    () -> PsitweaksItems.MODULE_PSYON_SUPPLYING.asItem(),
                    builder -> builder.maxStackSize(8).rarity(Rarity.RARE));

    public static final ModuleRegistryObject<ModulePsyonCapacityUnit> PSYON_CAPACITY_UNIT =
            MODULES.register("psyon_capacity_unit", ModulePsyonCapacityUnit::new,
                    () -> PsitweaksItems.MODULE_PSYON_CAPACITY.asItem(),
                    builder -> builder.maxStackSize(8).rarity(Rarity.RARE));

    public static final ModuleRegistryObject<ModulePhenomenonInterferenceEnhancementUnit> PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT =
            MODULES.register("phenomenon_interference_enhancement_unit", ModulePhenomenonInterferenceEnhancementUnit::new,
                    () -> PsitweaksItems.MODULE_PHENOMENON_INTERFERENCE_ENHANCEMENT.asItem(),
                    builder -> builder.maxStackSize(8).rarity(Rarity.RARE));

    private PsitweaksModules() {
    }
}


