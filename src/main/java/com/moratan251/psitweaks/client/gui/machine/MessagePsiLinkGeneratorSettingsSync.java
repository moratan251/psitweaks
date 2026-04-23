package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.tile.machine.TileEntityPsionicGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessagePsiLinkGeneratorSettingsSync {
    private final BlockPos pos;
    private final int consumePsiPerTick;
    private final boolean linkActive;

    public MessagePsiLinkGeneratorSettingsSync(BlockPos pos, int consumePsiPerTick, boolean linkActive) {
        this.pos = pos;
        this.consumePsiPerTick = consumePsiPerTick;
        this.linkActive = linkActive;
    }

    public static void encode(MessagePsiLinkGeneratorSettingsSync msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.consumePsiPerTick);
        buf.writeBoolean(msg.linkActive);
    }

    public static MessagePsiLinkGeneratorSettingsSync decode(FriendlyByteBuf buf) {
        return new MessagePsiLinkGeneratorSettingsSync(buf.readBlockPos(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(MessagePsiLinkGeneratorSettingsSync msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(msg.pos);
            if (blockEntity instanceof TileEntityPsionicGenerator tile && tile.isOwnedBy(player)) {
                tile.applySettings(msg.consumePsiPerTick, msg.linkActive);
            }
        });
        context.setPacketHandled(true);
    }
}
