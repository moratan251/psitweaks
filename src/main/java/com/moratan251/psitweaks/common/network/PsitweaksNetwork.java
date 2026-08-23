package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PsitweaksNetwork {
    private PsitweaksNetwork() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Psitweaks.MOD_ID).versioned("5");
        registrar.playToClient(
                MessageFlightPsiCastEffect.TYPE,
                MessageFlightPsiCastEffect.STREAM_CODEC,
                MessageFlightPsiCastEffect::handle
        );
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
        registrar.playToClient(
                MessageIdeaStorageSync.TYPE,
                MessageIdeaStorageSync.STREAM_CODEC,
                MessageIdeaStorageSync::handle
        );
        registrar.playToServer(
                MessageIdeaStorageExtract.TYPE,
                MessageIdeaStorageExtract.STREAM_CODEC,
                MessageIdeaStorageExtract::handle
        );
        registrar.playToServer(
                MessageIdeaStorageDeposit.TYPE,
                MessageIdeaStorageDeposit.STREAM_CODEC,
                MessageIdeaStorageDeposit::handle
        );
        registrar.playToServer(
                MessageIdeaStorageResize.TYPE,
                MessageIdeaStorageResize.STREAM_CODEC,
                MessageIdeaStorageResize::handle
        );
        registrar.playToServer(
                MessageIdeaStorageCraftToggle.TYPE,
                MessageIdeaStorageCraftToggle.STREAM_CODEC,
                MessageIdeaStorageCraftToggle::handle
        );
    }
}
