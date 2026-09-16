package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record MessageConnectorAction(int containerId, UUID session, long revision, int action, int slot, int argument)
        {
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId); buf.writeUUID(session); buf.writeLong(revision);
        buf.writeVarInt(action); buf.writeVarInt(slot); buf.writeVarInt(argument);
    }
    public static MessageConnectorAction read(FriendlyByteBuf buf) {
        return new MessageConnectorAction(buf.readVarInt(), buf.readUUID(), buf.readLong(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }
    public static void handle(MessageConnectorAction message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            if (context.getSender() != null && context.getSender().containerMenu instanceof IdeaspaceConnectorMenu menu && menu.containerId == message.containerId())
                menu.handleAction(context.getSender(), message);
        });
    }
}
