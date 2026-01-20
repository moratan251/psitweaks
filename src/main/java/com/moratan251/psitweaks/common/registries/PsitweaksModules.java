package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.items.modules.ModulePsyonSupplyingUnit;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.item.Rarity;

public class PsitweaksModules {



    //public static final ModuleRegistryObject<ModulePsyonSupplyingUnit> PSYON_SUPPLYING_UNIT = MODULES.register("psyon_supplying_unit", ModulePsyonSupplyingUnit::new, PsitweaksItems.PSYON_SUPPLYING_UNIT, builder -> builder.maxStackSize(4).rarity(Rarity.RARE));

    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister("psitweaks");

    public static final ModuleRegistryObject<ModulePsyonSupplyingUnit> PSYON_SUPPLYING_UNIT = MODULES.register("psyon_supplying_unit", ModulePsyonSupplyingUnit::new, () -> PsitweaksItems.MODULE_PSYON_SUPPLYING.asItem(), builder -> builder.maxStackSize(1).rarity(Rarity.RARE));

    private PsitweaksModules()
    {

    }

}


