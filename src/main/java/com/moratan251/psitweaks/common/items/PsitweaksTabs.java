package com.moratan251.psitweaks.common.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"psitweaks");

    public static final RegistryObject<CreativeModeTab> PSITWEAKS_TAB = TABS.register("psitweaks",
            () -> CreativeModeTab.builder().
                    title(Component.translatable("creativetabs.psitweaks")).
                    icon(ModItems.ALLOY_PSION.get()::getDefaultInstance).
                    displayItems((((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.MOVAL_SUIT_HELMET.get());
                        pOutput.accept(ModItems.MOVAL_SUIT_CHESTPLATE.get());
                        pOutput.accept(ModItems.MOVAL_SUIT_LEGGINGS.get());
                        pOutput.accept(ModItems.MOVAL_SUIT_BOOTS.get());
                        pOutput.accept(ModItems.MOVAL_SUIT_LEGGINGS_IVORY.get());
                        pOutput.accept(ModItems.ENRICHED_PSIGEM.get());
                        pOutput.accept(ModItems.ENRICHED_EBONY.get());
                        pOutput.accept(ModItems.ENRICHED_IVORY.get());
                        pOutput.accept(ModItems.ALLOY_PSION.get());
                        pOutput.accept(ModItems.PSIONIC_CONTROL_CIRCUIT.get());
                        pOutput.accept(ModItems.FLIGHT_CHIP.get());
                        pOutput.accept(ModItems.FLASH_RING.get());
                       // pOutput.accept(ModItems.PSYON_SUPPLY_RING.get());


                    })))
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

}
