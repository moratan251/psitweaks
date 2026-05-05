package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PsitweaksNetwork {
    private PsitweaksNetwork() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Psitweaks.MOD_ID).versioned("1");
        registrar.playToServer(
                MessageAutoCasterCustomTickSync.TYPE,
                MessageAutoCasterCustomTickSync.STREAM_CODEC,
                MessageAutoCasterCustomTickSync::handle
        );
        registrar.playToServer(
                MessagePsiLinkGeneratorSettingsSync.TYPE,
                MessagePsiLinkGeneratorSettingsSync.STREAM_CODEC,
                MessagePsiLinkGeneratorSettingsSync::handle
        );
    }
}
