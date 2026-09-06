package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * カーソル保持スタックのイデアストレージへの格納要求(クライアント→サーバー)。
 * サーバー側で getCarried() と照合してから実行する。
 */
public record MessageIdeaStorageDeposit(ItemStack template) {

    public void write(FriendlyByteBuf buf) {
        IdeaStorageNetwork.writeItem(buf, template);
    }

    public static MessageIdeaStorageDeposit read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageDeposit(IdeaStorageNetwork.readItem(buf));
    }

    public static void handle(MessageIdeaStorageDeposit message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleDeposit(serverPlayer, message.template());
            }
        });
    }
}
