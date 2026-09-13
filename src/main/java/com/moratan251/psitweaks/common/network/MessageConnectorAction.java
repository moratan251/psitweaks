package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageConnectorAction(int containerId, UUID session, long revision, int action, int slot, int argument)
        implements CustomPacketPayload {
    public static final Type<MessageConnectorAction> TYPE = new Type<>(Psitweaks.location("connector_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageConnectorAction> STREAM_CODEC =
            CustomPacketPayload.codec(MessageConnectorAction::write, MessageConnectorAction::read);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId); buf.writeUUID(session); buf.writeLong(revision);
        buf.writeVarInt(action); buf.writeVarInt(slot); buf.writeVarInt(argument);
    }
    private static MessageConnectorAction read(RegistryFriendlyByteBuf buf) {
        return new MessageConnectorAction(buf.readVarInt(), buf.readUUID(), buf.readLong(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }
    public static void handle(MessageConnectorAction message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof IdeaspaceConnectorMenu menu && menu.containerId == message.containerId())
                menu.handleAction(context.player(), message);
        });
    }
}
