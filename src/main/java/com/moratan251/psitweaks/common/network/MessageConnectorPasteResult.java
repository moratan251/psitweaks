package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsClipboard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Outcome of a paste, shown by the open connector screen. Settings themselves arrive through the state sync. */
public record MessageConnectorPasteResult(int containerId, ConnectorSettingsClipboard.Code code, int skipped, String detail)
        implements CustomPacketPayload {
    public static final Type<MessageConnectorPasteResult> TYPE = new Type<>(Psitweaks.location("connector_paste_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageConnectorPasteResult> STREAM_CODEC =
            CustomPacketPayload.codec(MessageConnectorPasteResult::write, MessageConnectorPasteResult::read);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(code.ordinal());
        buf.writeVarInt(skipped);
        buf.writeUtf(detail, ConnectorSettingsClipboard.MAX_DETAIL_LENGTH);
    }

    private static MessageConnectorPasteResult read(RegistryFriendlyByteBuf buf) {
        return new MessageConnectorPasteResult(buf.readVarInt(), ConnectorSettingsClipboard.Code.byId(buf.readVarInt()),
                buf.readVarInt(), buf.readUtf(ConnectorSettingsClipboard.MAX_DETAIL_LENGTH));
    }

    public static void handle(MessageConnectorPasteResult message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof IdeaspaceConnectorMenu menu && menu.containerId == message.containerId())
                menu.receivePasteResult(message);
        });
    }
}
