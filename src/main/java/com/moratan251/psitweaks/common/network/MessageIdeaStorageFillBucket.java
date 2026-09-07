package com.moratan251.psitweaks.common.network;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import com.moratan251.psitweaks.common.storage.idea.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
/** Session-scoped request; resource identity is resolved on the server. */
public record MessageIdeaStorageFillBucket(IdeaStorageMenuToken token, long entryId) {

    public void write(FriendlyByteBuf buf) {
        token.write(buf);
        buf.writeLong(entryId);
    }
    public static MessageIdeaStorageFillBucket read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageFillBucket(IdeaStorageMenuToken.read(buf), buf.readLong());
    }
    public static void handle(MessageIdeaStorageFillBucket message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            IdeaStorageMenu menu = message.token().resolve(player);
            if (menu == null) return;
            if (menu.resolveEntry(message.entryId()) instanceof FluidResourceKey key) {
                menu.handleFillStoredBucket(key.template());
            }
        });
    }
}
