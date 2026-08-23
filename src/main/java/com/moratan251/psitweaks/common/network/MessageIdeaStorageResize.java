package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * イデアストレージ GUI の表示行数変更要求(クライアント→サーバー)。
 * サーバーは範囲検証のうえ保存し、Menu を閉じてから新しい行数で開き直す(QIO の recreateViewer と同じ発想)。
 */
public record MessageIdeaStorageResize(int rows) implements CustomPacketPayload {
    public static final Type<MessageIdeaStorageResize> TYPE =
            new Type<>(Psitweaks.location("idea_storage_resize"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageResize> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageResize::write, MessageIdeaStorageResize::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(rows);
    }

    private static MessageIdeaStorageResize read(RegistryFriendlyByteBuf buf) {
        return new MessageIdeaStorageResize(buf.readVarInt());
    }

    public static void handle(MessageIdeaStorageResize message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleResize(serverPlayer, message.rows());
            }
        });
    }
}
