package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * カーソル保持スタックのイデアストレージへの格納要求(クライアント→サーバー)。
 * サーバー側で getCarried() と照合してから実行する。
 */
public record MessageIdeaStorageDeposit(ItemStack template) implements CustomPacketPayload {
    public static final Type<MessageIdeaStorageDeposit> TYPE =
            new Type<>(Psitweaks.location("idea_storage_deposit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageDeposit> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageDeposit::write, MessageIdeaStorageDeposit::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, template);
    }

    private static MessageIdeaStorageDeposit read(RegistryFriendlyByteBuf buf) {
        return new MessageIdeaStorageDeposit(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }

    public static void handle(MessageIdeaStorageDeposit message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleDeposit(serverPlayer, message.template());
            }
        });
    }
}
