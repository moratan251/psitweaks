package com.moratan251.psitweaks.common.items;

import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
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
                        pOutput.accept(PsitweaksItems.FLASH_CHARM.get());
                        pOutput.accept(PsitweaksItems.ENRICHED_PSIGEM.get());
                        pOutput.accept(PsitweaksItems.ENRICHED_EBONY.get());
                        pOutput.accept(PsitweaksItems.ENRICHED_IVORY.get());
                        pOutput.accept(PsitweaksItems.ENRICHED_ECHO.get());
                        pOutput.accept(PsitweaksItems.ALLOY_PSION.get());
                        pOutput.accept(PsitweaksItems.ALLOY_PSIONIC_ECHO.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_ECHO.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_BLANK.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_COCYTUS.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_TIME_ACCELERATE.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_FLIGHT.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_PHONON_MASER.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_SUPREME_INFUSION.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_MOLECULAR_DIVIDER.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_RADIATION_INJECTION.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_GUILLOTINE.get());
                        pOutput.accept(PsitweaksItems.PROGRAM_ACTIVE_AIR_MINE.get());
                        pOutput.accept(PsitweaksItems.HEAVY_PSIMETAL_SCRAP.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_CONTROL_CIRCUIT.get());
                        pOutput.accept(PsitweaksItems.ECHO_CONTROL_CIRCUIT.get());
                        pOutput.accept(PsitweaksItems.FLASH_RING.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_FACTOR.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_FACTOR_IVORY.get());
                        pOutput.accept(PsitweaksItems.PSIONIC_FACTOR_EBONY.get());
                        pOutput.accept(PsitweaksItems.CHAOTIC_FACTOR.get());
                        pOutput.accept(PsitweaksItems.CHAOTIC_PSIMETAL.get());
                        pOutput.accept(PsitweaksItems.UNREFINED_FLASHMETAL.get());
                        pOutput.accept(PsitweaksItems.FLASHMETAL.get());
                        pOutput.accept(PsitweaksItems.HEAVY_PSIMETAL.get());

                        pOutput.accept(PsitweaksItems.ECHO_PELLET.get());
                        pOutput.accept(PsitweaksItems.ECHO_SHEET.get());

                        pOutput.accept(PsitweaksItems.CAD_ASSEMBLY_ALLOY_PSION.get());
                        pOutput.accept(PsitweaksItems.CAD_ASSEMBLY_CHAOTIC_PSIMETAL.get());
                        pOutput.accept(PsitweaksItems.CAD_ASSEMBLY_FLASHMETAL.get());
                        pOutput.accept(PsitweaksItems.INCOMPLETE_HEAVY_PSIMETAL_ASSEMBLY.get());
                        pOutput.accept(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_ALPHA.get());
                        pOutput.accept(PsitweaksItems.CAD_ASSEMBLY_HEAVY_PSIMETAL_BETA.get());
                        pOutput.accept(PsitweaksItems.PORTABLE_CAD_ASSEMBLER.get());


                        pOutput.accept(PsitweaksItems.THIRD_EYE_DEVICE.get());
                        pOutput.accept(PsitweaksItems.ADVANCED_SPELL_BULLET.get());
                        pOutput.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_LOOP.get());
                        pOutput.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_MINE.get());
                        pOutput.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_CHARGE.get());
                        pOutput.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_GRENADE.get());
                        pOutput.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_PROJECTILE.get());
                        pOutput.accept(PsitweaksItems.ADVANCED_SPELL_BULLET_CIRCLE.get());

                        pOutput.accept(PsitweaksItems.RESONANT_SPELL_BULLET.get());
                        pOutput.accept(PsitweaksItems.RESONANT_SPELL_BULLET_LOOP.get());
                        pOutput.accept(PsitweaksItems.RESONANT_SPELL_BULLET_MINE.get());
                        pOutput.accept(PsitweaksItems.RESONANT_SPELL_BULLET_CHARGE.get());
                        pOutput.accept(PsitweaksItems.RESONANT_SPELL_BULLET_GRENADE.get());
                        pOutput.accept(PsitweaksItems.RESONANT_SPELL_BULLET_PROJECTILE.get());
                        pOutput.accept(PsitweaksItems.RESONANT_SPELL_BULLET_CIRCLE.get());

                        pOutput.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET.get());
                        pOutput.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_LOOP.get());
                        pOutput.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_MINE.get());
                        pOutput.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_CHARGE.get());
                        pOutput.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_GRENADE.get());
                        pOutput.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_PROJECTILE.get());
                        pOutput.accept(PsitweaksItems.SUBLIMATED_SPELL_BULLET_CIRCLE.get());

                        pOutput.accept(PsitweaksItems.AWAKENED_SPELL_BULLET.get());
                        pOutput.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_LOOP.get());
                        pOutput.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_MINE.get());
                        pOutput.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_CHARGE.get());
                        pOutput.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_GRENADE.get());
                        pOutput.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_PROJECTILE.get());
                        pOutput.accept(PsitweaksItems.AWAKENED_SPELL_BULLET_CIRCLE.get());

                        pOutput.accept(PsitweaksBlocks.CAD_DISASSEMBLER.get());
                        pOutput.accept(PsitweaksBlocks.PROGRAM_RESEARCHER.get());
                        pOutput.accept(PsitweaksMekanismBlocks.SCULK_ERODER.getBlock());
                     //   pOutput.accept(PsitweaksItems.MODULE_PSYON_SUPPLYING.get());
                       // pOutput.accept(ModItems.PSYON_SUPPLY_RING.get());


                    })))
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

}
