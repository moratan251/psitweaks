package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.machine.GuiMaterialMutator;
import com.moratan251.psitweaks.client.gui.machine.GuiSculkEroder;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismContainerTypes;
import com.moratan251.psitweaks.common.registries.PsitweaksMekanismBlocks;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.render.item.TransmitterTypeDecorator;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PsitweaksMekanismClient {

    private PsitweaksMekanismClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistrationUtil.registerScreen(PsitweaksMekanismContainerTypes.SCULK_ERODER, GuiSculkEroder::new);
            ClientRegistrationUtil.registerScreen(PsitweaksMekanismContainerTypes.MATERIAL_MUTATOR, GuiMaterialMutator::new);
        });
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        TransmitterTypeDecorator.registerDecorators(event, PsitweaksMekanismBlocks.TRANSCENDENT_CABLE);
    }
}
