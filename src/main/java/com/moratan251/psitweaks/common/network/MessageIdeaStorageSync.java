package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.client.gui.IdeaStorageClientHandler;
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
 * イデアストレージの全量スナップショット・FE差分同期(サーバー→本人のみ)。
 * クライアント側の処理は client パッケージのハンドラへ委譲し、dedicated server ではロードされない。
 */
public record MessageIdeaStorageSync(int containerId, UUID session, boolean full,
                                     List<Entry> entries, List<FluidEntry> fluidEntries,
                                     List<ChemicalEntry> chemicalEntries,
                                     int maxItemTypes, int maxFluidTypes, int maxChemicalTypes,
                                     long energy, long maxEnergy, boolean loadFailed) implements CustomPacketPayload {
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
        buf.writeVarInt(containerId);
        buf.writeUUID(session);
        buf.writeBoolean(full);
        buf.writeLong(energy);
        buf.writeLong(maxEnergy);
        if (!full) return;
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
        boolean full = buf.readBoolean();
        long energy = buf.readLong(), maxEnergy = buf.readLong();
        if (!full) return energyUpdate(containerId, session, energy, maxEnergy);
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
        return new MessageIdeaStorageSync(containerId, session, true, List.copyOf(entries), List.copyOf(fluidEntries),
                List.copyOf(chemicalEntries), maxItemTypes, maxFluidTypes, maxChemicalTypes,
                energy, maxEnergy, buf.readBoolean());
    }

    public static MessageIdeaStorageSync energyUpdate(int containerId, UUID session, long energy, long maxEnergy) {
        return new MessageIdeaStorageSync(containerId, session, false, List.of(), List.of(), List.of(),
                0, 0, 0, energy, maxEnergy, false);
    }

    public static void handle(MessageIdeaStorageSync message, IPayloadContext context) {
        context.enqueueWork(() -> IdeaStorageClientHandler.handleSync(message));
    }
}
