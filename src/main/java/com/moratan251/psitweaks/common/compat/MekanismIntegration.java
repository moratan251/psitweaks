package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.client.compat.MekanismClientIntegration;
import com.moratan251.psitweaks.common.chemicals.PsitweaksChemicals;
import com.moratan251.psitweaks.common.handler.PsitweaksMekanismGeneratorTweaks;
import com.moratan251.psitweaks.common.handler.MekanismMaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.items.PsitweaksMekanismItems;
import com.moratan251.psitweaks.common.items.armor.ArmorSpellDamageAttributeHandler;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismTileEntityTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksModules;
import mekanism.api.MekanismIMC;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

final class MekanismIntegration {
    private MekanismIntegration() {
    }

    static void register(IEventBus modEventBus) {
        PsitweaksMekanismItems.register(modEventBus);
        PsitweaksChemicals.register(modEventBus);
        PsitweaksMekanismBlocks.register(modEventBus);
        PsitweaksMekanismTileEntityTypes.register(modEventBus);
        PsitweaksMekanismContainerTypes.register(modEventBus);
        PsitweaksModules.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(ArmorSpellDamageAttributeHandler::onItemAttributeModifier);
        NeoForge.EVENT_BUS.addListener(MekanismMaterialMutationRecipeHandler::onAddReloadListener);
    }

    static void commonSetup(FMLCommonSetupEvent event) {
        if (MekanismCompat.isGeneratorsLoaded()) {
            event.enqueueWork(PsitweaksMekanismGeneratorTweaks::registerGeneratorTweaks);
        }
        event.enqueueWork(MekanismIntegration::enqueueIMC);
    }

    static void registerClient(IEventBus modEventBus) {
        MekanismClientIntegration.register(modEventBus);
    }

    static void addCreativeTabContents(CreativeModeTab.Output output) {
        output.accept(PsitweaksMekanismItems.MODULE_PSYON_SUPPLYING.get());
        output.accept(PsitweaksMekanismItems.MODULE_PSYON_CAPACITY.get());
        output.accept(PsitweaksMekanismItems.MODULE_PHENOMENON_INTERFERENCE_ENHANCEMENT.get());
        output.accept(PsitweaksMekanismBlocks.SCULK_ERODER.get());
        output.accept(PsitweaksMekanismBlocks.PROGRAM_RESEARCHER.get());
        output.accept(PsitweaksMekanismBlocks.MATERIAL_MUTATOR.get());
        output.accept(PsitweaksMekanismBlocks.PSIONIC_GENERATOR.get());
        output.accept(PsitweaksMekanismBlocks.TRANSCENDENT_CABLE.get());
        output.accept(PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.get());
        output.accept(StorageUtils.getFilledEnergyVariant(
                PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.getItemHolder()
        ));
    }

    private static void enqueueIMC() {
        MekanismIMC.addMekaSuitBodyarmorModules(
                PsitweaksModules.PSYON_SUPPLYING_UNIT,
                PsitweaksModules.PSYON_CAPACITY_UNIT
        );
        MekanismIMC.addMekaSuitHelmetModules(
                PsitweaksModules.PHENOMENON_INTERFERENCE_ENHANCEMENT_UNIT
        );
    }
}
