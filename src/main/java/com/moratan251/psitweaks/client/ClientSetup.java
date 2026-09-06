package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.machine.GuiPortableCADAssembler;
import com.moratan251.psitweaks.client.gui.machine.ModMenuTypes;
import com.moratan251.psitweaks.client.renderer.AquaCutterProjectileRenderer;
import com.moratan251.psitweaks.client.renderer.DryIceProjectileRenderer;
import com.moratan251.psitweaks.client.renderer.EntityTimeAcceleratorRenderer;
import com.moratan251.psitweaks.client.renderer.FlareCircleRenderer;
import com.moratan251.psitweaks.client.renderer.IceCircleRenderer;
import com.moratan251.psitweaks.client.renderer.MeteorLineBeamRenderer;
import com.moratan251.psitweaks.client.renderer.MolecularDividerRenderer;
import com.moratan251.psitweaks.client.renderer.PhononMaserBeamRenderer;
import com.moratan251.psitweaks.client.renderer.TunnelerArrowRenderer;
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
        event.registerEntityRenderer(PsitweaksEntities.METEOR_LINE_BEAM.get(), MeteorLineBeamRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.MOLECULAR_DIVIDER.get(), MolecularDividerRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.TIME_ACCELERATOR.get(), EntityTimeAcceleratorRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.AQUA_CUTTER_PROJECTILE.get(), AquaCutterProjectileRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.DRY_ICE_PROJECTILE.get(), DryIceProjectileRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.BLAZE_BALL.get(), context -> new ThrownItemRenderer<>(context, 0.75F, true));
        event.registerEntityRenderer(PsitweaksEntities.FLARE_CIRCLE.get(), FlareCircleRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.ICE_CIRCLE.get(), IceCircleRenderer::new);
        event.registerEntityRenderer(PsitweaksEntities.TUNNELER_ARROW.get(), TunnelerArrowRenderer::new);

    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.PORTABLE_CAD_ASSEMBLER.get(), GuiPortableCADAssembler::new);
            MenuScreens.register(ModMenuTypes.IDEA_STORAGE.get(), com.moratan251.psitweaks.client.gui.IdeaStorageScreen::new);
        });
    }
}
