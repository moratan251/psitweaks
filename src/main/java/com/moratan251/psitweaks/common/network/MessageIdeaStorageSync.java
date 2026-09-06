package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.client.gui.IdeaStorageClientHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * イデアストレージの全量スナップショット同期(サーバー→本人のみ)。
 * クライアント側の処理は client パッケージのハンドラへ委譲し、dedicated server ではロードされない。
 */
public record MessageIdeaStorageSync(List<Entry> entries, List<FluidEntry> fluidEntries,
                                     List<ChemicalEntry> chemicalEntries,
                                     int maxItemTypes, int maxFluidTypes, int maxChemicalTypes,
                                     boolean loadFailed) {

    public record Entry(ItemStack template, long count) {
    }

    public record FluidEntry(FluidStack template, long amount) {
    }

    public record ChemicalEntry(ResourceLocation chemicalId, long amount) {
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (Entry entry : entries) {
            IdeaStorageNetwork.writeItem(buf, entry.template());
            buf.writeLong(entry.count());
        }
        buf.writeVarInt(fluidEntries.size());
        for (FluidEntry entry : fluidEntries) {
            entry.template().writeToPacket(buf);
            buf.writeLong(entry.amount());
        }
        buf.writeVarInt(chemicalEntries.size());
        for (ChemicalEntry entry : chemicalEntries) {
            buf.writeResourceLocation(entry.chemicalId());
            buf.writeLong(entry.amount());
        }
        buf.writeVarInt(maxItemTypes);
        buf.writeVarInt(maxFluidTypes);
        buf.writeVarInt(maxChemicalTypes);
        buf.writeBoolean(loadFailed);
    }

    public static MessageIdeaStorageSync read(FriendlyByteBuf buf) {
        int size = readEntryCount(buf);
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack template = IdeaStorageNetwork.readItem(buf);
            long count = buf.readLong();
            entries.add(new Entry(template, count));
        }
        int fluidSize = readEntryCount(buf);
        List<FluidEntry> fluidEntries = new ArrayList<>(fluidSize);
        for (int i = 0; i < fluidSize; i++) {
            FluidStack template = FluidStack.readFromPacket(buf);
            long amount = buf.readLong();
            fluidEntries.add(new FluidEntry(template, amount));
        }
        int chemicalSize = readEntryCount(buf);
        List<ChemicalEntry> chemicalEntries = new ArrayList<>(chemicalSize);
        for (int i = 0; i < chemicalSize; i++) {
            ResourceLocation chemicalId = buf.readResourceLocation();
            long amount = buf.readLong();
            chemicalEntries.add(new ChemicalEntry(chemicalId, amount));
        }
        int maxItemTypes = buf.readVarInt();
        int maxFluidTypes = buf.readVarInt();
        int maxChemicalTypes = buf.readVarInt();
        return new MessageIdeaStorageSync(List.copyOf(entries), List.copyOf(fluidEntries),
                List.copyOf(chemicalEntries), maxItemTypes, maxFluidTypes, maxChemicalTypes,
                buf.readBoolean());
    }

    public static void handle(MessageIdeaStorageSync message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> IdeaStorageClientHandler.handleSync(message));
    }

    private static int readEntryCount(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > 100_000 || count > buf.readableBytes() / Long.BYTES) {
            throw new IllegalArgumentException("Invalid idea storage snapshot size: " + count);
        }
        return count;
    }
}
