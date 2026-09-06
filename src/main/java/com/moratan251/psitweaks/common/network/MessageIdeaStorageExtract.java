package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * イデアストレージからの払出要求(クライアント→サーバー)。
 * サーバー側で「開いている Menu が IdeaStorageMenu か」「本人のストレージか」を検証してから実行する。
 */
public record MessageIdeaStorageExtract(ItemStack template, int mode) {
    public static final int MODE_CURSOR_STACK = 1;
    public static final int MODE_CURSOR_HALF_STACK = 2;
    public static final int MODE_INVENTORY_STACK = 3;
    public static final int MODE_INVENTORY_HALF_STACK = 4;

    public void write(FriendlyByteBuf buf) {
        IdeaStorageNetwork.writeItem(buf, template);
        buf.writeVarInt(mode);
    }

    public static MessageIdeaStorageExtract read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageExtract(IdeaStorageNetwork.readItem(buf), buf.readVarInt());
    }

    public static void handle(MessageIdeaStorageExtract message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleExtract(serverPlayer, message.template(), message.mode());
            }
        });
    }
}
