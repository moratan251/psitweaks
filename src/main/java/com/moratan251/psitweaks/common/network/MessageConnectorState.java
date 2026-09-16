package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

/** Only nine settings and the visible page, sent only to the owner while the menu is open. */
public record MessageConnectorState(int containerId, UUID session, long revision, CompoundTag data) {
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId); buf.writeUUID(session); buf.writeLong(revision); buf.writeNbt(data);
    }
    public static MessageConnectorState read(FriendlyByteBuf buf) {
        int id = buf.readVarInt(); UUID session = buf.readUUID(); long revision = buf.readLong();
        // Transport fragments were reassembled before bounded NBT decoding.
        var tag = buf.readNbt(new net.minecraft.nbt.NbtAccounter(32L * 1024 * 1024));
        if (tag == null) throw new io.netty.handler.codec.DecoderException("Invalid connector state");
        return new MessageConnectorState(id, session, revision, tag);
    }
    public static void handle(MessageConnectorState message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> com.moratan251.psitweaks.client.gui.ConnectorClientHandler.accept(message));
    }
}
