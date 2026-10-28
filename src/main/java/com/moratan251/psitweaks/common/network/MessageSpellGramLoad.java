package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramPotencyLoad;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 「CAD の規模で数える版」の試作: 自分の領域型の魔法式が使っている枠（規模の合計）。Psi ゲージの上に表示する。 */
public record MessageSpellGramLoad(int load) implements CustomPacketPayload {
    public static final Type<MessageSpellGramLoad> TYPE = new Type<>(Psitweaks.location("spellgram_load"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSpellGramLoad> STREAM_CODEC =
            CustomPacketPayload.codec(MessageSpellGramLoad::write, MessageSpellGramLoad::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(load);
    }

    private static MessageSpellGramLoad read(RegistryFriendlyByteBuf buf) {
        return new MessageSpellGramLoad(buf.readVarInt());
    }

    public static void handle(MessageSpellGramLoad message, IPayloadContext context) {
        context.enqueueWork(() -> SpellGramPotencyLoad.setClientLoad(message.load()));
    }
}
