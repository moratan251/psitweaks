package com.moratan251.psitweaks.client.head;

import com.moratan251.psitweaks.common.blocks.PsitweaksSkullTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * 魔法師の頭をバニラの頭と同じ仕組み (SkullBlockRenderer) で描くための登録。置いた頭・かぶった頭・手に持った頭のどれもこれで描かれる。
 */
public final class MagiciansHeadClient {
    private MagiciansHeadClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MagiciansHeadClient::onRegisterLayers);
        modEventBus.addListener(MagiciansHeadClient::onCreateSkullModels);
        modEventBus.addListener(MagiciansHeadClient::onRegisterReloadListeners);
        modEventBus.addListener(MagiciansHeadClient::onClientSetup);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MagiciansHeadModel.LAYER, MagiciansHeadModel::createLayer);
    }

    private static void onCreateSkullModels(EntityRenderersEvent.CreateSkullModels event) {
        event.registerSkullModel(PsitweaksSkullTypes.MAGICIAN,
                new MagiciansHeadModel(event.getEntityModelSet().bakeLayer(MagiciansHeadModel.LAYER)));
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        // 資源を読み込み直すたびに作り直し、そのときの村人と魔法師の見た目に合わせる。
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> Minecraft.getInstance()
                .getTextureManager()
                .register(MagiciansHeadTexture.LOCATION, new MagiciansHeadTexture()));
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> SkullBlockRenderer.SKIN_BY_TYPE.put(PsitweaksSkullTypes.MAGICIAN, MagiciansHeadTexture.LOCATION));
    }
}
