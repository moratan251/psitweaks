package com.moratan251.psitweaks.common.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"psitweaks");

    public static final RegistryObject<CreativeModeTab> PSITWEAKS_TAB = TABS.register("psitweaks",
            () -> CreativeModeTab.builder().
                    title(Component.translatable("creativetabs.psitweaks")).
                    icon(PsitweaksItems.ALLOY_PSION.get()::getDefaultInstance).
                    displayItems((((pParameters, pOutput) -> {
                        pOutput.accept(PsitweaksItems.MOVAL_SUIT_HELMET.get());
                        pOutput.accept(PsitweaksItems.MOVAL_SUIT_CHESTPLATE.get());
                        pOutput.accept(PsitweaksItems.MOVAL_SUIT_LEGGINGS.get());
                        pOutput.accept(PsitweaksItems.MOVAL_SUIT_BOOTS.get());
                        pOutput.accept(PsitweaksItems.MOVAL_SUIT_LEGGINGS_IVORY.get());
                        pOutput.accept(PsitweaksItems.PSIMETAL_BOW.get());
                        pOutput.accept(PsitweaksItems.CURIOS_CONTROLLER.get());
                        pOutput.accept(PsitweaksItems.AUTO_CASTER_SECOND.get());
                        pOutput.accept(PsitweaksItems.AUTO_CASTER_TICK.get());
                        pOutput.accept(PsitweaksItems.ENRICHED_PSIGEM.get());
                        pOutput.accept(PsitweaksItems.ENRICHED_EBONY.get());
                        pOutput.accept(PsitweaksItems.ENRICHED_IVORY.get());
                        pOutput.accept(PsitweaksItems.ALLOY_PSION.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get());
                        pOutput.accept(PsitweaksItems.FLIGHT_CHIP.get());
                        pOutput.accept(PsitweaksItems.FLASH_RING.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_FACTOR.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_FACTOR_IVORY.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_FACTOR_EBONY.get());
                        pOutput.accept(PsitweaksItems.CHAOTIC_FACTOR.get());
                        pOutput.accept(PsitweaksItems.CHAOTIC_PSIMETAL.get());
                        pOutput.accept(PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get());
                        pOutput.accept(PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL.get());
                        pOutput.accept(PsitweaksItems.AIM_ASSISTING_DEVICE.get());
                     //   pOutput.accept(PsitweaksItems.MODULE_PSYON_SUPPLYING.get());
                       // pOutput.accept(ModItems.PSYON_SUPPLY_RING.get());


                    })))
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

}
