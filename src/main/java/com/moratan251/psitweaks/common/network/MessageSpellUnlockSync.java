package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.handler.SpellUnlockHandler;
import com.moratan251.psitweaks.common.handler.SpellUnlockHandler.SpellUnlockDefinition;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** サーバーの有効なスペル解禁定義をクライアントへ送る(GUI 側の解禁判定に使う)。 */
public record MessageSpellUnlockSync(List<SpellUnlockDefinition> definitions) implements CustomPacketPayload {
    public static final Type<MessageSpellUnlockSync> TYPE = new Type<>(Psitweaks.location("spell_unlock_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSpellUnlockSync> STREAM_CODEC =
            CustomPacketPayload.codec(MessageSpellUnlockSync::write, MessageSpellUnlockSync::read);

    public MessageSpellUnlockSync {
        definitions = List.copyOf(definitions);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(definitions.size());
        for (SpellUnlockDefinition definition : definitions) {
            buf.writeUtf(definition.commandId());
            buf.writeResourceLocation(definition.pieceId());
            buf.writeResourceLocation(definition.unlockItemId());
            buf.writeUtf(definition.unlockTag());
            buf.writeNullable(definition.groupId(), (out, id) -> out.writeResourceLocation(id));
        }
    }

    private static MessageSpellUnlockSync read(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > SpellUnlockHandler.MAX_SYNC_DEFINITIONS) {
            throw new DecoderException("Too many spell unlock definitions: " + size);
        }
        List<SpellUnlockDefinition> definitions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String commandId = buf.readUtf();
            ResourceLocation pieceId = buf.readResourceLocation();
            ResourceLocation unlockItemId = buf.readResourceLocation();
            String unlockTag = buf.readUtf();
            ResourceLocation groupId = buf.readNullable(in -> in.readResourceLocation());
            definitions.add(new SpellUnlockDefinition(commandId, pieceId, unlockItemId, unlockTag, groupId));
        }
        return new MessageSpellUnlockSync(definitions);
    }

    public static void handle(MessageSpellUnlockSync message, IPayloadContext context) {
        context.enqueueWork(() -> SpellUnlockHandler.applyClientDefinitions(message.definitions()));
    }
}
