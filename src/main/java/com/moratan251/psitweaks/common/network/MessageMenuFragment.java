package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu;
import com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Both directions stay below Forge's 32767 byte C2S custom payload limit. */
public record MessageMenuFragment(int containerId, UUID session, UUID transfer, int kind,
                                  int sequence, boolean last, byte[] data) {
    public static final int CONNECTOR_STATE = 0, CONNECTOR_TEMPLATE = 1, SPELL_EDIT = 2, SPELL_INITIAL = 3;
    public static final int MAX_PART = 24 * 1024, MAX_TOTAL = 32 * 1024 * 1024;
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId); buf.writeUUID(session); buf.writeUUID(transfer); buf.writeVarInt(kind);
        buf.writeVarInt(sequence); buf.writeBoolean(last); buf.writeByteArray(data);
    }
    public static MessageMenuFragment read(FriendlyByteBuf buf) {
        return new MessageMenuFragment(buf.readVarInt(), buf.readUUID(), buf.readUUID(), buf.readVarInt(),
                buf.readVarInt(), buf.readBoolean(), buf.readByteArray(MAX_PART));
    }
    public static void send(int id, UUID session, int kind, Consumer<FriendlyByteBuf> encoder, Consumer<MessageMenuFragment> output) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            encoder.accept(buf);
            if (buf.readableBytes() > MAX_TOTAL) throw new IllegalArgumentException("Menu payload too large");
            UUID transfer = UUID.randomUUID();
            int sequence = 0;
            do {
                byte[] data = new byte[Math.min(buf.readableBytes(), MAX_PART)]; buf.readBytes(data);
                output.accept(new MessageMenuFragment(id, session, transfer, kind, sequence++, !buf.isReadable(), data));
            } while (buf.isReadable());
        } finally { buf.release(); }
    }
    public static void handle(MessageMenuFragment part, Supplier<NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                com.moratan251.psitweaks.client.gui.ConnectorClientHandler.accept(part);
                return;
            }
            ServerPlayer player = context.getSender();
            if (player == null || player.containerMenu.containerId != part.containerId()) return;
            MenuPayloadBuffer receiver;
            if (part.kind() == CONNECTOR_TEMPLATE && player.containerMenu instanceof IdeaspaceConnectorMenu menu
                    && menu.stillValid(player) && menu.session().equals(part.session())) receiver = menu.payloadBuffer();
            else if (part.kind() == SPELL_EDIT && player.containerMenu instanceof PortableSpellProgrammerMenu menu
                    && menu.stillValid(player) && menu.session().equals(part.session())) receiver = menu.payloadBuffer();
            else return;
            byte[] bytes = receiver.accept(part);
            if (bytes == null) return;
            var buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            try {
                if (part.kind() == CONNECTOR_TEMPLATE) {
                    var message = MessageConnectorTemplate.read(buf);
                    if (!buf.isReadable()) ((IdeaspaceConnectorMenu) player.containerMenu).handleTemplate(player, message);
                } else {
                    var message = MessagePortableSpellProgrammerEdit.read(buf);
                    if (!buf.isReadable()) ((PortableSpellProgrammerMenu) player.containerMenu).handleEdit(player, message);
                }
            } catch (RuntimeException malformed) {
                // A malformed client frame cannot partially edit a menu or the held item.
            } finally { buf.release(); }
        });
    }
}
