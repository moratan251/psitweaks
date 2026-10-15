package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.storage.connector.ConnectorSettingsClipboard;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** Outcome of a paste, shown by the open connector screen. Settings themselves arrive through the state sync. */
public record MessageConnectorPasteResult(int containerId, ConnectorSettingsClipboard.Code code, int skipped, String detail) {
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(code.ordinal());
        buf.writeVarInt(skipped);
        buf.writeUtf(detail, ConnectorSettingsClipboard.MAX_DETAIL_LENGTH);
    }

    public static MessageConnectorPasteResult read(FriendlyByteBuf buf) {
        return new MessageConnectorPasteResult(buf.readVarInt(), ConnectorSettingsClipboard.Code.byId(buf.readVarInt()),
                buf.readVarInt(), buf.readUtf(ConnectorSettingsClipboard.MAX_DETAIL_LENGTH));
    }

    public static void handle(MessageConnectorPasteResult message, Supplier<NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> com.moratan251.psitweaks.client.gui.ConnectorClientHandler.accept(message));
    }
}
