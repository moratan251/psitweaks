package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsClipboard;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Raw clipboard text. The server parses and validates it; the client never decides what is applied. */
public record MessageConnectorPaste(int containerId, UUID session, String text) implements CustomPacketPayload {
    public static final Type<MessageConnectorPaste> TYPE = new Type<>(Psitweaks.location("connector_paste"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageConnectorPaste> STREAM_CODEC =
            CustomPacketPayload.codec(MessageConnectorPaste::write, MessageConnectorPaste::read);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
        buf.writeUtf(text, ConnectorSettingsClipboard.MAX_LENGTH);
    }

    private static MessageConnectorPaste read(RegistryFriendlyByteBuf buf) {
        return new MessageConnectorPaste(buf.readVarInt(), buf.readUUID(), buf.readUtf(ConnectorSettingsClipboard.MAX_LENGTH));
    }

    public static void handle(MessageConnectorPaste message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof IdeaspaceConnectorMenu menu && menu.containerId == message.containerId())
                menu.handlePaste(context.player(), message);
        });
    }
}
