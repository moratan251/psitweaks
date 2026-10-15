package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsClipboard;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Raw clipboard text. The server parses and validates it; the client never decides what is applied. The text can
 * exceed Forge's C2S payload limit, so it travels in {@link MessageMenuFragment} parts.
 */
public record MessageConnectorPaste(int containerId, UUID session, String text) {
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
        buf.writeUtf(text, ConnectorSettingsClipboard.MAX_LENGTH);
    }

    public static MessageConnectorPaste read(FriendlyByteBuf buf) {
        return new MessageConnectorPaste(buf.readVarInt(), buf.readUUID(), buf.readUtf(ConnectorSettingsClipboard.MAX_LENGTH));
    }
}
