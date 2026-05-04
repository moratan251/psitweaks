package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.items.curios.ItemAutoCasterCustomTick;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageAutoCasterCustomTickSync(InteractionHand hand, int interval) implements CustomPacketPayload {
    public static final Type<MessageAutoCasterCustomTickSync> TYPE =
            new Type<>(Psitweaks.location("auto_caster_custom_tick_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageAutoCasterCustomTickSync> STREAM_CODEC =
            CustomPacketPayload.codec(MessageAutoCasterCustomTickSync::write, MessageAutoCasterCustomTickSync::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeVarInt(this.interval);
    }

    private static MessageAutoCasterCustomTickSync read(RegistryFriendlyByteBuf buf) {
        return new MessageAutoCasterCustomTickSync(buf.readEnum(InteractionHand.class), buf.readVarInt());
    }

    public static void handle(MessageAutoCasterCustomTickSync message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getItemInHand(message.hand);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemAutoCasterCustomTick) {
                ItemAutoCasterCustomTick.setCastInterval(stack, message.interval);
            }
        });
    }
}
