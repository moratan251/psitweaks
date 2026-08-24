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
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    public static Component ideaStorageChemicalName(ResourceLocation chemicalId) {
        return MekanismAPI.CHEMICAL_REGISTRY.getHolder(chemicalId)
                .filter(holder -> !holder.is(MekanismAPI.EMPTY_CHEMICAL_KEY))
                .<Component>map(holder -> holder.value().getTextComponent())
                .orElseGet(() -> Component.literal(chemicalId.toString()));
    }

    public static boolean renderIdeaStorageChemical(GuiGraphics guiGraphics, ResourceLocation chemicalId,
                                                     int x, int y) {
        var chemical = MekanismAPI.CHEMICAL_REGISTRY.getHolder(chemicalId)
                .filter(holder -> !holder.is(MekanismAPI.EMPTY_CHEMICAL_KEY));
        if (chemical.isEmpty()) {
            return false;
        }
        ChemicalStack stack = new ChemicalStack(chemical.get(), 1);
        TextureAtlasSprite sprite = MekanismRenderer.getChemicalTexture(stack);
        int color = MekanismRenderer.getColorARGB(stack, 1.0F);
        guiGraphics.setColor(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                ((color >>> 24) & 0xFF) / 255.0F
        );
        guiGraphics.blit(x, y, 300, 16, 16, sprite);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }
}
