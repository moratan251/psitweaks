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
 * クラフトウィンドウ開閉トグル(クライアント→サーバー)。
 * クライアントは楽観的に自身の Menu を更新し、サーバー側 Menu もこの payload で同期する。
 * サーバー側は「開いている Menu が IdeaStorageMenu か」を検証してから反映する。
 */
public record MessageIdeaStorageCraftToggle(boolean open) implements CustomPacketPayload {
    public static final Type<MessageIdeaStorageCraftToggle> TYPE =
            new Type<>(Psitweaks.location("idea_storage_craft_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageCraftToggle> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageCraftToggle::write, MessageIdeaStorageCraftToggle::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(open);
    }

    private static MessageIdeaStorageCraftToggle read(RegistryFriendlyByteBuf buf) {
        return new MessageIdeaStorageCraftToggle(buf.readBoolean());
    }

    public static void handle(MessageIdeaStorageCraftToggle message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.setCraftOpen(message.open());
            }
        });
    }
}
