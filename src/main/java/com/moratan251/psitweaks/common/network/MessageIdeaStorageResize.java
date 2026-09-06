package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * イデアストレージ GUI の表示行数変更要求(クライアント→サーバー)。
 * サーバーは範囲検証のうえ保存し、Menu を閉じてから新しい行数で開き直す(QIO の recreateViewer と同じ発想)。
 */
public record MessageIdeaStorageResize(int rows) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(rows);
    }

    public static MessageIdeaStorageResize read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageResize(buf.readVarInt());
    }

    public static void handle(MessageIdeaStorageResize message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleResize(serverPlayer, message.rows());
            }
        });
    }
}
