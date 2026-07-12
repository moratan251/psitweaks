package com.moratan251.psitweaks.client.compat;

import com.moratan251.psitweaks.client.gui.machine.GuiMaterialMutator;
import com.moratan251.psitweaks.client.gui.machine.GuiProgramResearcher;
import com.moratan251.psitweaks.client.gui.machine.GuiPsionicGenerator;
import com.moratan251.psitweaks.client.gui.machine.GuiSculkEroder;
import com.moratan251.psitweaks.client.gui.machine.GuiTranscendentEnergyCube;
import com.moratan251.psitweaks.client.render.item.block.RenderTranscendentEnergyCubeItem;
import com.moratan251.psitweaks.common.handler.MekanismMaterialMutationRecipeHandler;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import mekanism.client.render.item.TransmitterTypeDecorator;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class MekanismClientIntegration {
    private MekanismClientIntegration() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MekanismClientIntegration::onRegisterMenuScreens);
        modEventBus.addListener(MekanismClientIntegration::onRegisterClientReloadListeners);
        modEventBus.addListener(MekanismClientIntegration::onRegisterClientExtensions);
        modEventBus.addListener(MekanismClientIntegration::onRegisterItemDecorations);
        MekanismMaterialMutationRecipeHandler.registerClientReloadListeners(modEventBus);
    }

    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(PsitweaksMekanismContainerTypes.SCULK_ERODER.get(), GuiSculkEroder::new);
        event.register(PsitweaksMekanismContainerTypes.PROGRAM_RESEARCHER.get(), GuiProgramResearcher::new);
        event.register(PsitweaksMekanismContainerTypes.MATERIAL_MUTATOR.get(), GuiMaterialMutator::new);
        event.register(PsitweaksMekanismContainerTypes.PSIONIC_GENERATOR.get(), GuiPsionicGenerator::new);
        event.register(
                PsitweaksMekanismContainerTypes.TRANSCENDENT_ENERGY_CUBE.get(),
                GuiTranscendentEnergyCube::new
        );
    }

    private static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RenderTranscendentEnergyCubeItem.RENDERER);
    }

    private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return RenderTranscendentEnergyCubeItem.RENDERER;
            }
        }, PsitweaksMekanismBlocks.TRANSCENDENT_ENERGY_CUBE.asItem());
    }

    private static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        TransmitterTypeDecorator.registerDecorators(event, PsitweaksMekanismBlocks.TRANSCENDENT_CABLE);
    }
}
