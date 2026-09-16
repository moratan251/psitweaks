package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.storage.connector.ConnectorExportSettings;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

/** Four fixed resource rows, applied atomically after menu authorization and range validation. */
public record MessageConnectorExportSettings(int containerId, UUID session, long revision, int slot,
                                              List<ConnectorExportSettings> settings) {
    public MessageConnectorExportSettings {
        settings = List.copyOf(settings);
        if (settings.size() != ConnectorExportSettings.TYPES) throw new IllegalArgumentException("Expected four export settings");
    }
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId); buf.writeUUID(session); buf.writeLong(revision); buf.writeVarInt(slot);
        for (var value : settings) { buf.writeVarInt(value.amount()); buf.writeVarInt(value.interval()); }
    }
    public static MessageConnectorExportSettings read(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        UUID session = buf.readUUID();
        long revision = buf.readLong();
        int slot = buf.readVarInt();
        var settings = new ConnectorExportSettings[ConnectorExportSettings.TYPES];
        for (int type = 0; type < settings.length; type++) settings[type] = new ConnectorExportSettings(buf.readVarInt(), buf.readVarInt());
        return new MessageConnectorExportSettings(id, session, revision, slot, List.of(settings));
    }
    public static void handle(MessageConnectorExportSettings message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            if (context.getSender() != null && context.getSender().containerMenu instanceof IdeaspaceConnectorMenu menu)
                menu.handleExportSettings(context.getSender(), message);
        });
    }
}
