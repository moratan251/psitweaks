package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.IdeaStorageClientHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * イデアストレージの全量スナップショット同期(サーバー→本人のみ)。
 * クライアント側の処理は client パッケージのハンドラへ委譲し、dedicated server ではロードされない。
 */
public record MessageIdeaStorageSync(List<Entry> entries, boolean loadFailed) implements CustomPacketPayload {
    public static final Type<MessageIdeaStorageSync> TYPE =
            new Type<>(Psitweaks.location("idea_storage_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageSync> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageSync::write, MessageIdeaStorageSync::read);

    public record Entry(ItemStack template, long count) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (Entry entry : entries) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, entry.template());
            buf.writeLong(entry.count());
        }
        buf.writeBoolean(loadFailed);
    }

    private static MessageIdeaStorageSync read(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack template = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            long count = buf.readLong();
            entries.add(new Entry(template, count));
        }
        return new MessageIdeaStorageSync(List.copyOf(entries), buf.readBoolean());
    }

    public static void handle(MessageIdeaStorageSync message, IPayloadContext context) {
        context.enqueueWork(() -> IdeaStorageClientHandler.handleSync(message));
    }
}
