package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import io.netty.handler.codec.DecoderException;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

/** A ghost filter only: this payload never inserts or extracts resources. */
public record MessageConnectorTemplate(int containerId, UUID session, int slot, CompoundTag resource)
        {
    public static final int MAX_TEMPLATE_SIZE = 64 * 1024;


    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
        buf.writeVarInt(slot);
        buf.writeNbt(resource);
    }

    public static MessageConnectorTemplate read(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        UUID session = buf.readUUID();
        int slot = buf.readVarInt();
        CompoundTag resource = buf.readNbt(new net.minecraft.nbt.NbtAccounter(MAX_TEMPLATE_SIZE));
        if (resource == null)
            throw new DecoderException("Missing connector template");
        return new MessageConnectorTemplate(containerId, session, slot, resource);
    }

    public static void handle(MessageConnectorTemplate message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            if (context.getSender() != null && context.getSender().containerMenu instanceof IdeaspaceConnectorMenu menu && menu.containerId == message.containerId())
                menu.handleTemplate(context.getSender(), message);
        });
    }
}
