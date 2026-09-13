package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.storage.connector.ConnectorExportSettings;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Four fixed resource rows, applied atomically after menu authorization and range validation. */
public record MessageConnectorExportSettings(int containerId, UUID session, long revision, int slot,
                                              List<ConnectorExportSettings> settings) implements CustomPacketPayload {
    public MessageConnectorExportSettings {
        settings = List.copyOf(settings);
        if (settings.size() != ConnectorExportSettings.TYPES) throw new IllegalArgumentException("Expected four export settings");
    }
    public static final Type<MessageConnectorExportSettings> TYPE = new Type<>(Psitweaks.location("connector_export_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageConnectorExportSettings> STREAM_CODEC =
            CustomPacketPayload.codec(MessageConnectorExportSettings::write, MessageConnectorExportSettings::read);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId); buf.writeUUID(session); buf.writeLong(revision); buf.writeVarInt(slot);
        for (var value : settings) { buf.writeVarInt(value.amount()); buf.writeVarInt(value.interval()); }
    }
    private static MessageConnectorExportSettings read(RegistryFriendlyByteBuf buf) {
        int id = buf.readVarInt();
        UUID session = buf.readUUID();
        long revision = buf.readLong();
        int slot = buf.readVarInt();
        var settings = new ConnectorExportSettings[ConnectorExportSettings.TYPES];
        for (int type = 0; type < settings.length; type++) settings[type] = new ConnectorExportSettings(buf.readVarInt(), buf.readVarInt());
        return new MessageConnectorExportSettings(id, session, revision, slot, List.of(settings));
    }
    public static void handle(MessageConnectorExportSettings message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof IdeaspaceConnectorMenu menu)
                menu.handleExportSettings(context.player(), message);
        });
    }
}
