package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.renderer.MolecularDividerRenderer;
import com.moratan251.psitweaks.client.renderer.PhononMaserBeamRenderer;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // エンティティのレンダラーを登録
        event.registerEntityRenderer(PsitweaksEntities.PHONON_MASER_BEAM.get(), PhononMaserBeamRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.MOLECULAR_DIVIDER.get(), MolecularDividerRenderer::new);

    }
}