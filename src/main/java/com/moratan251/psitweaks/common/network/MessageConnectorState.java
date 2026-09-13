package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Only nine settings and the visible page, sent only to the owner while the menu is open. */
public record MessageConnectorState(int containerId, UUID session, long revision, CompoundTag data) implements CustomPacketPayload {
    public static final Type<MessageConnectorState> TYPE = new Type<>(Psitweaks.location("connector_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageConnectorState> STREAM_CODEC =
            CustomPacketPayload.codec(MessageConnectorState::write, MessageConnectorState::read);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId); buf.writeUUID(session); buf.writeLong(revision); buf.writeNbt(data);
    }
    private static MessageConnectorState read(RegistryFriendlyByteBuf buf) {
        int id = buf.readVarInt(); UUID session = buf.readUUID(); long revision = buf.readLong();
        // A page can contain component-heavy books. NeoForge splits oversized play packets.
        var tag = buf.readNbt(NbtAccounter.create(32L * 1024 * 1024));
        if (!(tag instanceof CompoundTag data)) throw new io.netty.handler.codec.DecoderException("Invalid connector state");
        return new MessageConnectorState(id, session, revision, data);
    }
    public static void handle(MessageConnectorState message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof IdeaspaceConnectorMenu menu && menu.containerId == message.containerId())
                menu.applyState(message);
        });
    }
}
