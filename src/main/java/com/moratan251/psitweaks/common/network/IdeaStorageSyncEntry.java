package com.moratan251.psitweaks.common.network;

import com.moratan251.psitweaks.common.storage.idea.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

/** Immutable resource keys are shared by the session cache; quantity zero is a tombstone. */
public record IdeaStorageSyncEntry(long id, Object key, long amount) {
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
        buf.writeLong(amount);
        if (amount == 0) return;
        if (key == null) {
            buf.writeByte(3);
        } else if (key instanceof ItemResourceKey item) {
            buf.writeByte(0);
            // Send the immutable identity that already passed the admission limit.
            buf.writeNbt((net.minecraft.nbt.CompoundTag) item.save());
        } else if (key instanceof FluidResourceKey fluid) {
            buf.writeByte(1);
            fluid.template().writeToPacket(buf);
        } else if (key instanceof ResourceLocation chemical) {
            buf.writeByte(2);
            buf.writeResourceLocation(chemical);
        } else throw new IllegalArgumentException("Unknown idea storage resource");
    }

    public static IdeaStorageSyncEntry read(FriendlyByteBuf buf) {
        long id = buf.readLong();
        long amount = buf.readLong();
        if (id <= 0 || amount < 0) throw new IllegalArgumentException("Invalid storage entry");
        if (amount == 0) return new IdeaStorageSyncEntry(id, null, 0);
        Object key = switch (buf.readUnsignedByte()) {
            case 0 -> ItemResourceKey.of(IdeaStorageNetwork.readStorageItem(buf)).orElseThrow();
            case 1 -> FluidResourceKey.of(FluidStack.readFromPacket(buf)).orElseThrow();
            case 2 -> buf.readResourceLocation();
            case 3 -> null;
            default -> throw new IllegalArgumentException("Invalid resource kind");
        };
        return new IdeaStorageSyncEntry(id, key, amount);
    }
}
