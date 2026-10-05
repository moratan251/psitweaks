package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.handler.SpellUnlockHandler;
import com.moratan251.psitweaks.common.handler.SpellUnlockHandler.SpellUnlockDefinition;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** サーバーの有効なスペル解禁定義をクライアントへ送る(GUI 側の解禁判定に使う)。 */
public record MessageSpellUnlockSync(List<SpellUnlockDefinition> definitions) {
    public MessageSpellUnlockSync {
        definitions = List.copyOf(definitions);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(definitions.size());
        for (SpellUnlockDefinition definition : definitions) {
            buf.writeUtf(definition.commandId());
            buf.writeResourceLocation(definition.pieceId());
            buf.writeResourceLocation(definition.unlockItemId());
            buf.writeUtf(definition.unlockTag());
            buf.writeNullable(definition.groupId(), FriendlyByteBuf::writeResourceLocation);
        }
    }

    /**
     * {@link #write} と同じバイト数を、パケット全体を確保せずに数える。
     * limit を超えた時点で打ち切り、limit + 1 を返す。
     */
    public static int encodedSize(List<SpellUnlockDefinition> definitions, int limit) {
        long size = FriendlyByteBuf.getVarIntSize(definitions.size());
        for (SpellUnlockDefinition definition : definitions) {
            size += utfSize(definition.commandId());
            size += utfSize(definition.pieceId().toString());
            size += utfSize(definition.unlockItemId().toString());
            size += utfSize(definition.unlockTag());
            size += 1;
            if (definition.groupId() != null) {
                size += utfSize(definition.groupId().toString());
            }
            if (size > limit) {
                return limit + 1;
            }
        }
        return (int) size;
    }

    private static int utfSize(String value) {
        int bytes = value.getBytes(StandardCharsets.UTF_8).length;
        return FriendlyByteBuf.getVarIntSize(bytes) + bytes;
    }

    public static MessageSpellUnlockSync read(FriendlyByteBuf buf) {
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
            ResourceLocation groupId = buf.readNullable(FriendlyByteBuf::readResourceLocation);
            definitions.add(new SpellUnlockDefinition(commandId, pieceId, unlockItemId, unlockTag, groupId));
        }
        return new MessageSpellUnlockSync(definitions);
    }

    public static void handle(MessageSpellUnlockSync message, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> SpellUnlockHandler.applyClientDefinitions(message.definitions()));
    }
}
