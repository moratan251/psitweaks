package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** クラフトグリッド全量をイデアストレージへ戻す要求。 */
public record MessageIdeaStorageClearCrafting() implements CustomPacketPayload {
    public static final Type<MessageIdeaStorageClearCrafting> TYPE =
            new Type<>(Psitweaks.location("idea_storage_clear_crafting"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageClearCrafting> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageClearCrafting::write, MessageIdeaStorageClearCrafting::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
    }

    private static MessageIdeaStorageClearCrafting read(RegistryFriendlyByteBuf buf) {
        return new MessageIdeaStorageClearCrafting();
    }

    public static void handle(MessageIdeaStorageClearCrafting message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IdeaStorageMenu menu
                    && menu.stillValid(serverPlayer)) {
                menu.handleClearCrafting();
            }
        });
    }
}
