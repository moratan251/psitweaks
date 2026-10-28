package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramRegions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 回収キー: 押したプレイヤーの領域型の魔法式オブジェクトをすべて解く要求（試作）。 */
public record MessageRecallSpellGrams() implements CustomPacketPayload {
    public static final Type<MessageRecallSpellGrams> TYPE = new Type<>(Psitweaks.location("recall_spellgrams"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageRecallSpellGrams> STREAM_CODEC =
            CustomPacketPayload.codec(MessageRecallSpellGrams::write, MessageRecallSpellGrams::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
    }

    private static MessageRecallSpellGrams read(RegistryFriendlyByteBuf buf) {
        return new MessageRecallSpellGrams();
    }

    public static void handle(MessageRecallSpellGrams message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            int count = SpellGramRegions.recallAll(CasterIds.idOf(player));
            player.displayClientMessage(count > 0
                    ? Component.translatable("psitweaks.spellgram_region.recalled", count)
                    : Component.translatable("psitweaks.spellgram_region.recalled_none"), true);
        });
    }
}
