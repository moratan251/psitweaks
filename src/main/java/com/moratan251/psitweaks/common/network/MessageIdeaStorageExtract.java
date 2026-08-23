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
 * イデアストレージからの払出要求(クライアント→サーバー)。
 * サーバー側で「開いている Menu が IdeaStorageMenu か」「本人のストレージか」を検証してから実行する。
 */
public record MessageIdeaStorageExtract(ItemStack template, int mode) implements CustomPacketPayload {
    public static final int MODE_CURSOR_STACK = 1;
    public static final int MODE_CURSOR_HALF_STACK = 2;
    public static final int MODE_INVENTORY_STACK = 3;
    public static final int MODE_INVENTORY_HALF_STACK = 4;

    public static final Type<MessageIdeaStorageExtract> TYPE =
            new Type<>(Psitweaks.location("idea_storage_extract"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageExtract> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageExtract::write, MessageIdeaStorageExtract::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, template);
        buf.writeVarInt(mode);
    }

    private static MessageIdeaStorageExtract read(RegistryFriendlyByteBuf buf) {
        return new MessageIdeaStorageExtract(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readVarInt());
    }

    public static void handle(MessageIdeaStorageExtract message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleExtract(serverPlayer, message.template(), message.mode());
            }
        });
    }
}
