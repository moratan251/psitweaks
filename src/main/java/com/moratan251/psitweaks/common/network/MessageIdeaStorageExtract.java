package com.moratan251.psitweaks.common.network;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.storage.idea.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
/** Session-scoped request; resource identity is resolved on the server. */
public record MessageIdeaStorageExtract(IdeaStorageMenuToken token, long entryId, int mode) {
    public static final int MODE_CURSOR_STACK = 1;
    public static final int MODE_CURSOR_HALF_STACK = 2;
    public static final int MODE_INVENTORY_STACK = 3;
    public static final int MODE_INVENTORY_HALF_STACK = 4;
    public void write(FriendlyByteBuf buf) {
        token.write(buf);
        buf.writeLong(entryId); buf.writeVarInt(mode);
    }
    public static MessageIdeaStorageExtract read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageExtract(IdeaStorageMenuToken.read(buf), buf.readLong(), buf.readVarInt());
    }
    public static void handle(MessageIdeaStorageExtract message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            IdeaStorageMenu menu = message.token().resolve(player);
            if (menu == null) return;
            if (menu.resolveEntry(message.entryId()) instanceof ItemResourceKey key) {
                menu.handleExtract(player, key.template(), message.mode());
            }
        });
    }
}
