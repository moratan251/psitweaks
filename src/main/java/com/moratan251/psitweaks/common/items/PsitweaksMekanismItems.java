package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public final class PsitweaksMekanismItems {
    private static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(Psitweaks.MOD_ID);

    public static final ItemRegistryObject<ItemModule> MODULE_PSYON_SUPPLYING =
            ITEMS.registerModule(PsitweaksModules.PSYON_SUPPLYING_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_PSYON_CAPACITY =
            ITEMS.registerModule(PsitweaksModules.PSYON_CAPACITY_UNIT);
    public static final ItemRegistryObject<ItemModule> MODULE_PHENOMENON_INTERFERENCE_ENHANCEMENT =
            ITEMS.registerModule(PsitweaksModules.PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT);

    private PsitweaksMekanismItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
