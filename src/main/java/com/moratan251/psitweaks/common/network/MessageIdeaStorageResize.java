package com.moratan251.psitweaks.common.network;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.storage.idea.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
/** Session-scoped request; resource identity is resolved on the server. */
public record MessageIdeaStorageResize(IdeaStorageMenuToken token, int rows) {

    public void write(FriendlyByteBuf buf) {
        token.write(buf);
        buf.writeVarInt(rows);
    }
    public static MessageIdeaStorageResize read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageResize(IdeaStorageMenuToken.read(buf), buf.readVarInt());
    }
    public static void handle(MessageIdeaStorageResize message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            IdeaStorageMenu menu = message.token().resolve(player);
            if (menu == null) return;
            menu.handleResize(player, message.rows());
        });
    }
}
