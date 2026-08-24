package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.IdeaStorageClientHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * イデアストレージの全量スナップショット同期(サーバー→本人のみ)。
 * クライアント側の処理は client パッケージのハンドラへ委譲し、dedicated server ではロードされない。
 */
public record MessageIdeaStorageSync(List<Entry> entries, List<FluidEntry> fluidEntries,
                                     List<ChemicalEntry> chemicalEntries,
                                     boolean loadFailed) implements CustomPacketPayload {
    public static final Type<MessageIdeaStorageSync> TYPE =
            new Type<>(Psitweaks.location("idea_storage_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageIdeaStorageSync> STREAM_CODEC =
            CustomPacketPayload.codec(MessageIdeaStorageSync::write, MessageIdeaStorageSync::read);

    public record Entry(ItemStack template, long count) {
    }

    public record FluidEntry(FluidStack template, long amount) {
    }

    public record ChemicalEntry(ResourceLocation chemicalId, long amount) {
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
        buf.writeVarInt(fluidEntries.size());
        for (FluidEntry entry : fluidEntries) {
            FluidStack.STREAM_CODEC.encode(buf, entry.template());
            buf.writeLong(entry.amount());
        }
        buf.writeVarInt(chemicalEntries.size());
        for (ChemicalEntry entry : chemicalEntries) {
            buf.writeResourceLocation(entry.chemicalId());
            buf.writeLong(entry.amount());
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
        int fluidSize = buf.readVarInt();
        List<FluidEntry> fluidEntries = new ArrayList<>(fluidSize);
        for (int i = 0; i < fluidSize; i++) {
            FluidStack template = FluidStack.STREAM_CODEC.decode(buf);
            long amount = buf.readLong();
            fluidEntries.add(new FluidEntry(template, amount));
        }
        int chemicalSize = buf.readVarInt();
        List<ChemicalEntry> chemicalEntries = new ArrayList<>(chemicalSize);
        for (int i = 0; i < chemicalSize; i++) {
            ResourceLocation chemicalId = buf.readResourceLocation();
            long amount = buf.readLong();
            chemicalEntries.add(new ChemicalEntry(chemicalId, amount));
        }
        return new MessageIdeaStorageSync(List.copyOf(entries), List.copyOf(fluidEntries),
                List.copyOf(chemicalEntries), buf.readBoolean());
    }

    public static void handle(MessageIdeaStorageSync message, IPayloadContext context) {
        context.enqueueWork(() -> IdeaStorageClientHandler.handleSync(message));
    }
}
