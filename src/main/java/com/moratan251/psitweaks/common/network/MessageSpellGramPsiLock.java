package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramPsiLock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 「Psi を確保する版」の試作: 領域型の魔法式に確保している自分の Psi の量。Psi ゲージの表示と回復の予測に使う。 */
public record MessageSpellGramPsiLock(int lock) implements CustomPacketPayload {
    public static final Type<MessageSpellGramPsiLock> TYPE = new Type<>(Psitweaks.location("spellgram_psi_lock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSpellGramPsiLock> STREAM_CODEC =
            CustomPacketPayload.codec(MessageSpellGramPsiLock::write, MessageSpellGramPsiLock::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(lock);
    }

    private static MessageSpellGramPsiLock read(RegistryFriendlyByteBuf buf) {
        return new MessageSpellGramPsiLock(buf.readVarInt());
    }

    public static void handle(MessageSpellGramPsiLock message, IPayloadContext context) {
        context.enqueueWork(() -> SpellGramPsiLock.setClientLock(message.lock()));
    }
}
