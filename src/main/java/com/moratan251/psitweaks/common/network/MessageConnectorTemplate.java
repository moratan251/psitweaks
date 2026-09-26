package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import io.netty.handler.codec.DecoderException;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** A ghost filter only: this payload never inserts or extracts resources. */
public record MessageConnectorTemplate(int containerId, UUID session, int slot, CompoundTag resource, boolean inputFilter, long revision)
        implements CustomPacketPayload {
    public MessageConnectorTemplate(int containerId, UUID session, int slot, CompoundTag resource) {
        this(containerId, session, slot, resource, false, -1);
    }
    public static final int MAX_TEMPLATE_SIZE = 64 * 1024;
    public static final Type<MessageConnectorTemplate> TYPE = new Type<>(Psitweaks.location("connector_template"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageConnectorTemplate> STREAM_CODEC =
            CustomPacketPayload.codec(MessageConnectorTemplate::write, MessageConnectorTemplate::read);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
        buf.writeVarInt(slot);
        buf.writeBoolean(inputFilter);
        buf.writeLong(revision);
        buf.writeNbt(resource);
    }

    private static MessageConnectorTemplate read(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        UUID session = buf.readUUID();
        int slot = buf.readVarInt();
        boolean inputFilter = buf.readBoolean();
        long revision = buf.readLong();
        if (!(buf.readNbt(NbtAccounter.create(MAX_TEMPLATE_SIZE)) instanceof CompoundTag resource))
            throw new DecoderException("Missing connector template");
        return new MessageConnectorTemplate(containerId, session, slot, resource, inputFilter, revision);
    }

    public static void handle(MessageConnectorTemplate message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof IdeaspaceConnectorMenu menu && menu.containerId == message.containerId())
                menu.handleTemplate(context.player(), message);
        });
    }
}
