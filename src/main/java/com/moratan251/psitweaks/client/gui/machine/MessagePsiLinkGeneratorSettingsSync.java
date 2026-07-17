package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.tile.machine.TileEntityPsionicGenerator;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessagePsiLinkGeneratorSettingsSync {
    private final int consumePsiPerTick;
    private final boolean linkActive;

    public MessagePsiLinkGeneratorSettingsSync(int consumePsiPerTick, boolean linkActive) {
        this.consumePsiPerTick = consumePsiPerTick;
        this.linkActive = linkActive;
    }

    public static void encode(MessagePsiLinkGeneratorSettingsSync msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.consumePsiPerTick);
        buf.writeBoolean(msg.linkActive);
    }

    public static MessagePsiLinkGeneratorSettingsSync decode(FriendlyByteBuf buf) {
        return new MessagePsiLinkGeneratorSettingsSync(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(MessagePsiLinkGeneratorSettingsSync msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) {
                return;
            }

            if (!(player.containerMenu instanceof MekanismTileContainer<?> menu)
                    || !(menu.getTileEntity() instanceof TileEntityPsionicGenerator tile)
                    || !menu.stillValid(player)
                    || !tile.isOwnedBy(player)) {
                return;
            }
            tile.applySettings(msg.consumePsiPerTick, msg.linkActive);
        });
        context.setPacketHandled(true);
    }
}
