package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.spells.SpellWarningClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * The grid position of the piece that raised a spell warning, the counterpart of Psi's {@code MessageSpellError}.
 * The client formats the column itself so it follows Psi's letter-number grid coordinate setting.
 */
public record MessageSpellWarningPosition(int x, int y) implements CustomPacketPayload {
    public static final Type<MessageSpellWarningPosition> TYPE = new Type<>(Psitweaks.location("spell_warning_position"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSpellWarningPosition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageSpellWarningPosition::x,
            ByteBufCodecs.VAR_INT, MessageSpellWarningPosition::y,
            MessageSpellWarningPosition::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MessageSpellWarningPosition message, IPayloadContext context) {
        context.enqueueWork(() -> SpellWarningClient.showPosition(message.x(), message.y()));
    }
}
