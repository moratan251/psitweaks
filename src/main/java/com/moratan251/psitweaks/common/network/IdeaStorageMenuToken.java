package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.menu.IdeaStorageMenu;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/** A window id can wrap; the random session also distinguishes earlier windows and connections. */
public record IdeaStorageMenuToken(int containerId, UUID session) {
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
    }

    public static IdeaStorageMenuToken read(FriendlyByteBuf buf) {
        return new IdeaStorageMenuToken(buf.readVarInt(), buf.readUUID());
    }

    @Nullable
    public IdeaStorageMenu resolve(@Nullable ServerPlayer player) {
        return player != null && player.containerMenu instanceof IdeaStorageMenu menu
                && equals(menu.token()) && menu.stillValid(player) ? menu : null;
    }
}
