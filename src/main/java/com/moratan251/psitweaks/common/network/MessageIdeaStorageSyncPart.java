package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.client.gui.IdeaStorageClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Bounded transport frames, including when one resource alone exceeds a frame. */
public record MessageIdeaStorageSyncPart(IdeaStorageMenuToken token, long revision, int sequence,
        boolean reset, boolean last, int maxItems, int maxFluids, int maxChemicals, boolean loadFailed, byte[] data, long energy, long maxEnergy) {
    public MessageIdeaStorageSyncPart(IdeaStorageMenuToken token, long revision, int sequence, boolean reset, boolean last,
            int maxItems, int maxFluids, int maxChemicals, boolean loadFailed, byte[] data) {
        this(token, revision, sequence, reset, last, maxItems, maxFluids, maxChemicals, loadFailed, data, 0, 0);
    }
    public static final int MAX_DATA = 480 * 1024;

    public void write(FriendlyByteBuf buf) {
        token.write(buf);
        buf.writeLong(revision);
        buf.writeVarInt(sequence);
        buf.writeBoolean(reset);
        buf.writeBoolean(last);
        buf.writeVarInt(maxItems);
        buf.writeVarInt(maxFluids);
        buf.writeVarInt(maxChemicals);
        buf.writeBoolean(loadFailed);
        if (data.length > MAX_DATA) throw new IllegalArgumentException("Storage frame too large");
        buf.writeByteArray(data);
        buf.writeLong(energy); buf.writeLong(maxEnergy);
    }

    public static MessageIdeaStorageSyncPart read(FriendlyByteBuf buf) {
        return new MessageIdeaStorageSyncPart(IdeaStorageMenuToken.read(buf), buf.readLong(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readByteArray(MAX_DATA), buf.readLong(), buf.readLong());
    }

    public static void handle(MessageIdeaStorageSyncPart message, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        context.enqueueWork(() -> IdeaStorageClientHandler.handleSyncPart(message));
    }
}
