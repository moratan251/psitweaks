package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterCustomTick;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageAutoCasterCustomTickSync {
    private final InteractionHand hand;
    private final int interval;

    public MessageAutoCasterCustomTickSync(InteractionHand hand, int interval) {
        this.hand = hand;
        this.interval = interval;
    }

    public static void encode(MessageAutoCasterCustomTickSync msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeVarInt(msg.interval);
    }

    public static MessageAutoCasterCustomTickSync decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        int interval = buf.readVarInt();
        return new MessageAutoCasterCustomTickSync(hand, interval);
    }

    public static void handle(MessageAutoCasterCustomTickSync msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) {
                return;
            }

            ItemStack stack = player.getItemInHand(msg.hand);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemAutoCasterCustomTick) {
                ItemAutoCasterCustomTick.setCastInterval(stack, msg.interval);
            }
        });
        context.setPacketHandled(true);
    }
}
