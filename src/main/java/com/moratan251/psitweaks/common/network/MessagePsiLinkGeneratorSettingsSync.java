package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.tile.machine.PsionicGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessagePsiLinkGeneratorSettingsSync(BlockPos pos, int consumePsiPerTick, boolean linkActive) implements CustomPacketPayload {
    public static final Type<MessagePsiLinkGeneratorSettingsSync> TYPE =
            new Type<>(Psitweaks.location("psi_link_generator_settings_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePsiLinkGeneratorSettingsSync> STREAM_CODEC =
            CustomPacketPayload.codec(MessagePsiLinkGeneratorSettingsSync::write, MessagePsiLinkGeneratorSettingsSync::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(consumePsiPerTick);
        buf.writeBoolean(linkActive);
    }

    private static MessagePsiLinkGeneratorSettingsSync read(RegistryFriendlyByteBuf buf) {
        return new MessagePsiLinkGeneratorSettingsSync(buf.readBlockPos(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(MessagePsiLinkGeneratorSettingsSync message, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = context.player().level().getBlockEntity(message.pos);
            if (blockEntity instanceof PsionicGeneratorBlockEntity generator) {
                generator.ensureOwner(context.player());
                if (generator.isOwnedBy(context.player())) {
                    generator.applySettings(message.consumePsiPerTick, message.linkActive);
                }
            }
        });
    }
}
