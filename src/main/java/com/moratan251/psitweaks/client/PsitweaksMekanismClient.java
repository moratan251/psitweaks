package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.client.gui.machine.GuiMaterialMutator;
import com.moratan251.psitweaks.client.gui.machine.GuiPsionicGenerator;
import com.moratan251.psitweaks.client.gui.machine.GuiSculkEroder;
import com.moratan251.psitweaks.client.gui.machine.GuiTranscendentEnergyCube;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.render.item.TransmitterTypeDecorator;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class PsitweaksMekanismClient {

    private PsitweaksMekanismClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PsitweaksMekanismClient::onClientSetup);
        modEventBus.addListener(PsitweaksMekanismClient::registerItemDecorations);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistrationUtil.registerScreen(PsitweaksMekanismContainerTypes.SCULK_ERODER, GuiSculkEroder::new);
            ClientRegistrationUtil.registerScreen(PsitweaksMekanismContainerTypes.MATERIAL_MUTATOR, GuiMaterialMutator::new);
            ClientRegistrationUtil.registerScreen(PsitweaksMekanismContainerTypes.PSIONIC_GENERATOR, GuiPsionicGenerator::new);
            ClientRegistrationUtil.registerScreen(PsitweaksMekanismContainerTypes.TRANSCENDENT_ENERGY_CUBE, GuiTranscendentEnergyCube::new);
        });
    }

    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        TransmitterTypeDecorator.registerDecorators(event, PsitweaksMekanismBlocks.TRANSCENDENT_CABLE);
    }
}
