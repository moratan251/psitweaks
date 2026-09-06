package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** クラフトグリッド全量をイデアストレージへ戻す要求。 */
public record MessageIdeaStorageClearCrafting() {

    public void write(FriendlyByteBuf buf) {
    }

    public static MessageIdeaStorageClearCrafting read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageClearCrafting();
    }

    public static void handle(MessageIdeaStorageClearCrafting message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleClearCrafting();
            }
        });
    }
}
