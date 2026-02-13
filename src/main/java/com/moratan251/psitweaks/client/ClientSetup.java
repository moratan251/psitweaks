package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.machine.GuiPortableCADAssembler;
import com.moratan251.psitweaks.client.gui.machine.GuiProgramResearcher;
import com.moratan251.psitweaks.client.gui.machine.ModMenuTypes;
import com.moratan251.psitweaks.client.renderer.EntityTimeAcceleratorRenderer;
import com.moratan251.psitweaks.client.renderer.MolecularDividerRenderer;
import com.moratan251.psitweaks.client.renderer.PhononMaserBeamRenderer;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // エンティティのレンダラーを登録
        event.registerEntityRenderer(PsitweaksEntities.PHONON_MASER_BEAM.get(), PhononMaserBeamRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.MOLECULAR_DIVIDER.get(), MolecularDividerRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.TIME_ACCELERATOR.get(), EntityTimeAcceleratorRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.BLAZE_BALL.get(), context -> new ThrownItemRenderer<>(context, 0.75F, true));

    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.PORTABLE_CAD_ASSEMBLER.get(), GuiPortableCADAssembler::new);
            MenuScreens.register(ModMenuTypes.PROGRAM_RESEARCHER.get(), GuiProgramResearcher::new);
        });
    }
}
