package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * クラフトウィンドウ開閉トグル(クライアント→サーバー)。
 * クライアントは楽観的に自身の Menu を更新し、サーバー側 Menu もこの payload で同期する。
 * サーバー側は「開いている Menu が IdeaStorageMenu か」を検証してから反映する。
 */
public record MessageIdeaStorageCraftToggle(boolean open) {

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(open);
    }

    public static MessageIdeaStorageCraftToggle read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageCraftToggle(buf.readBoolean());
    }

    public static void handle(MessageIdeaStorageCraftToggle message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.setCraftOpen(message.open());
            }
        });
    }
}
