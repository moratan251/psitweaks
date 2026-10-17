package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PsitweaksNetwork {
    private PsitweaksNetwork() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Psitweaks.MOD_ID).versioned("23");
        registrar.playToClient(MessageSpellUnlockSync.TYPE, MessageSpellUnlockSync.STREAM_CODEC, MessageSpellUnlockSync::handle);
        registrar.playToServer(MessagePortableSpellProgrammerEdit.TYPE, MessagePortableSpellProgrammerEdit.STREAM_CODEC,
                MessagePortableSpellProgrammerEdit::handle);
        registrar.playToClient(MessageConnectorState.TYPE, MessageConnectorState.STREAM_CODEC, MessageConnectorState::handle);
        registrar.playToServer(MessageConnectorAction.TYPE, MessageConnectorAction.STREAM_CODEC, MessageConnectorAction::handle);
        registrar.playToServer(MessageConnectorTemplate.TYPE, MessageConnectorTemplate.STREAM_CODEC, MessageConnectorTemplate::handle);
        registrar.playToServer(MessageConnectorExportSettings.TYPE, MessageConnectorExportSettings.STREAM_CODEC, MessageConnectorExportSettings::handle);
        registrar.playToServer(MessageConnectorPaste.TYPE, MessageConnectorPaste.STREAM_CODEC, MessageConnectorPaste::handle);
        registrar.playToClient(MessageConnectorPasteResult.TYPE, MessageConnectorPasteResult.STREAM_CODEC, MessageConnectorPasteResult::handle);
        registrar.playToClient(
                MessageFlightPsiCastEffect.TYPE,
                MessageFlightPsiCastEffect.STREAM_CODEC,
                MessageFlightPsiCastEffect::handle
        );
        registrar.playToClient(MessageSphereFlash.TYPE, MessageSphereFlash.STREAM_CODEC, MessageSphereFlash::handle);
        registrar.playToClient(MessageLightningArcs.TYPE, MessageLightningArcs.STREAM_CODEC, MessageLightningArcs::handle);
        registrar.playToClient(MessageSpellWarningPosition.TYPE, MessageSpellWarningPosition.STREAM_CODEC,
                MessageSpellWarningPosition::handle);
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
        registrar.playToServer(
                MessageIdeaStorageFillCrafting.TYPE,
                MessageIdeaStorageFillCrafting.STREAM_CODEC,
                MessageIdeaStorageFillCrafting::handle
        );
        registrar.playToServer(
                MessageIdeaStorageClearCrafting.TYPE,
                MessageIdeaStorageClearCrafting.STREAM_CODEC,
                MessageIdeaStorageClearCrafting::handle
        );
        registrar.playToServer(
                MessageIdeaStorageTransferContents.TYPE,
                MessageIdeaStorageTransferContents.STREAM_CODEC,
                MessageIdeaStorageTransferContents::handle
        );
        registrar.playToServer(
                MessageIdeaStorageFillBucket.TYPE,
                MessageIdeaStorageFillBucket.STREAM_CODEC,
                MessageIdeaStorageFillBucket::handle
        );
    }
}
