package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.handler.SpellUnlockHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 接続先サーバーから同期されたスペル解禁定義を、切断時に破棄する。 */
@Mod.EventBusSubscriber(modid = Psitweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpellUnlockClientEvents {
    private SpellUnlockClientEvents() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SpellUnlockHandler.clearClientDefinitions();
    }
}
