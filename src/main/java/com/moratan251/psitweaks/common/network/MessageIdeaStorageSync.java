package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.IdeaStorageClientHandler;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * イデアストレージの全量スナップショット・数量/FE差分同期(サーバー→本人のみ)。
 * クライアント側の処理は client パッケージのハンドラへ委譲し、dedicated server ではロードされない。
 */
public record MessageIdeaStorageSync(int containerId, UUID session, long revision, boolean full,
                                     List<Entry> entries, List<FluidEntry> fluidEntries,
                                     List<ChemicalEntry> chemicalEntries,
                                     int maxItemTypes, int maxFluidTypes, int maxChemicalTypes,
                                     long energy, long maxEnergy, boolean loadFailed,
                                     List<QuantityUpdate> itemAmounts, List<QuantityUpdate> fluidAmounts,
                                     List<QuantityUpdate> chemicalAmounts) implements CustomPacketPayload {
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

    /** Index in the last full snapshot, never a filtered/sorted screen slot. */
    public record QuantityUpdate(int index, long amount) {
    }

    public MessageIdeaStorageSync(int containerId, UUID session, long revision, boolean full,
                                 List<Entry> entries, List<FluidEntry> fluidEntries, List<ChemicalEntry> chemicalEntries,
                                 int maxItemTypes, int maxFluidTypes, int maxChemicalTypes,
                                 long energy, long maxEnergy, boolean loadFailed) {
        this(containerId, session, revision, full, entries, fluidEntries, chemicalEntries,
                maxItemTypes, maxFluidTypes, maxChemicalTypes, energy, maxEnergy, loadFailed,
                List.of(), List.of(), List.of());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
        buf.writeVarLong(revision);
        buf.writeBoolean(full);
        buf.writeLong(energy);
        buf.writeLong(maxEnergy);
        if (!full) {
            writeAmounts(buf, itemAmounts);
            writeAmounts(buf, fluidAmounts);
            writeAmounts(buf, chemicalAmounts);
            return;
        }
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
        buf.writeVarInt(maxItemTypes);
        buf.writeVarInt(maxFluidTypes);
        buf.writeVarInt(maxChemicalTypes);
        buf.writeBoolean(loadFailed);
    }

    private static MessageIdeaStorageSync read(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        UUID session = buf.readUUID();
        long revision = buf.readVarLong();
        boolean full = buf.readBoolean();
        long energy = buf.readLong(), maxEnergy = buf.readLong();
        if (!full) return quantityUpdate(containerId, session, revision, energy, maxEnergy,
                readAmounts(buf), readAmounts(buf), readAmounts(buf));
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
        int maxItemTypes = buf.readVarInt();
        int maxFluidTypes = buf.readVarInt();
        int maxChemicalTypes = buf.readVarInt();
        return new MessageIdeaStorageSync(containerId, session, revision, true, List.copyOf(entries), List.copyOf(fluidEntries),
                List.copyOf(chemicalEntries), maxItemTypes, maxFluidTypes, maxChemicalTypes,
                energy, maxEnergy, buf.readBoolean());
    }

    public static MessageIdeaStorageSync energyUpdate(int containerId, UUID session, long revision, long energy, long maxEnergy) {
        return quantityUpdate(containerId, session, revision, energy, maxEnergy, List.of(), List.of(), List.of());
    }

    public static MessageIdeaStorageSync quantityUpdate(int containerId, UUID session, long revision,
                                                       long energy, long maxEnergy, List<QuantityUpdate> items,
                                                       List<QuantityUpdate> fluids, List<QuantityUpdate> chemicals) {
        return new MessageIdeaStorageSync(containerId, session, revision, false, List.of(), List.of(), List.of(),
                0, 0, 0, energy, maxEnergy, false, List.copyOf(items), List.copyOf(fluids), List.copyOf(chemicals));
    }

    private static void writeAmounts(RegistryFriendlyByteBuf buf, List<QuantityUpdate> updates) {
        buf.writeVarInt(updates.size());
        for (var update : updates) {
            buf.writeVarInt(update.index());
            buf.writeLong(update.amount());
        }
    }

    private static List<QuantityUpdate> readAmounts(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        // Each entry occupies at least one index byte and eight amount bytes.
        if (size < 0 || size > buf.readableBytes() / 9) throw new DecoderException("Invalid storage quantity update size");
        var updates = new ArrayList<QuantityUpdate>(size);
        for (int i = 0; i < size; i++) updates.add(new QuantityUpdate(buf.readVarInt(), buf.readLong()));
        return List.copyOf(updates);
    }

    public static void handle(MessageIdeaStorageSync message, IPayloadContext context) {
        context.enqueueWork(() -> IdeaStorageClientHandler.handleSync(message));
    }
}
