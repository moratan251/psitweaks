package com.moratan251.psitweaks.client;

import com.moratan251.psitweaks.common.handler.SpellUnlockHandler;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

/** 接続先サーバーから同期されたスペル解禁定義を、切断時に破棄する。 */
public final class SpellUnlockClientEvents {
    private SpellUnlockClientEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SpellUnlockClientEvents::onLoggingOut);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SpellUnlockHandler.clearClientDefinitions();
    }
}
