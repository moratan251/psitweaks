package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.network.*;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.util.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Menu-owned receiver. A partial update never replaces the last complete visible snapshot. */
public final class IdeaStorageSyncAccumulator {
    private static final int MAX_RECORD_BYTES = 64 * 1024 * 1024;
    private Map<Long, IdeaStorageSyncEntry> visible = new LinkedHashMap<>();
    private Map<Long, IdeaStorageSyncEntry> staging;
    private long completedRevision;
    private long receivingRevision;
    private int nextSequence;
    private final byte[] header = new byte[4];
    private int headerBytes;
    private int recordLength;
    private ByteArrayOutputStream record;
    private boolean broken;

    public Optional<MessageIdeaStorageSync> accept(IdeaStorageMenuToken expected, MessageIdeaStorageSyncPart part) {
        if (!expected.equals(part.token()) || part.revision() <= completedRevision || broken) return Optional.empty();
        if (part.sequence() == 0) {
            if (staging != null || part.revision() != completedRevision + 1
                    || part.reset() != (completedRevision == 0)) return fail();
            receivingRevision = part.revision();
            nextSequence = 0;
            staging = part.reset() ? new LinkedHashMap<>() : new LinkedHashMap<>(visible);
        }
        if (staging == null || part.revision() != receivingRevision || part.sequence() != nextSequence++
                || (part.sequence() != 0 && part.reset())) return fail();
        try {
            int offset = 0;
            while (offset < part.data().length) {
                if (record == null) {
                    while (headerBytes < 4 && offset < part.data().length) header[headerBytes++] = part.data()[offset++];
                    if (headerBytes < 4) break;
                    recordLength = ((header[0] & 255) << 24) | ((header[1] & 255) << 16)
                            | ((header[2] & 255) << 8) | (header[3] & 255);
                    if (recordLength < 16 || recordLength > MAX_RECORD_BYTES) return fail();
                    record = new ByteArrayOutputStream(Math.min(recordLength, MessageIdeaStorageSyncPart.MAX_DATA));
                }
                int count = Math.min(recordLength - record.size(), part.data().length - offset);
                record.write(part.data(), offset, count);
                offset += count;
                if (record.size() == recordLength) {
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(record.toByteArray()));
                    try {
                        IdeaStorageSyncEntry entry = IdeaStorageSyncEntry.read(buf);
                        if (buf.isReadable()) return fail();
                        if (entry.amount() == 0) staging.remove(entry.id());
                        else {
                            if (entry.key() == null) {
                                IdeaStorageSyncEntry old = staging.get(entry.id());
                                if (old == null) return fail();
                                entry = new IdeaStorageSyncEntry(entry.id(), old.key(), entry.amount());
                            }
                            staging.put(entry.id(), entry);
                        }
                    } finally { buf.release(); }
                    record = null;
                    headerBytes = 0;
                }
            }
            if (!part.last()) return Optional.empty();
            if (record != null || headerBytes != 0) return fail();
            visible = staging;
            staging = null;
            completedRevision = receivingRevision;
            List<MessageIdeaStorageSync.Entry> items = new ArrayList<>();
            List<MessageIdeaStorageSync.FluidEntry> fluids = new ArrayList<>();
            List<MessageIdeaStorageSync.ChemicalEntry> chemicals = new ArrayList<>();
            for (var entry : visible.values()) {
                if (entry.key() instanceof ItemResourceKey item) {
                    items.add(new MessageIdeaStorageSync.Entry(entry.id(), item.template(), entry.amount()));
                } else if (entry.key() instanceof FluidResourceKey fluid) {
                    fluids.add(new MessageIdeaStorageSync.FluidEntry(entry.id(), fluid.template(), entry.amount()));
                } else if (entry.key() instanceof ResourceLocation chemical) {
                    chemicals.add(new MessageIdeaStorageSync.ChemicalEntry(entry.id(), chemical, entry.amount()));
                }
            }
            return Optional.of(new MessageIdeaStorageSync(items, fluids, chemicals,
                    part.maxItems(), part.maxFluids(), part.maxChemicals(), part.loadFailed()));
        } catch (RuntimeException malformed) {
            return fail();
        }
    }

    private Optional<MessageIdeaStorageSync> fail() {
        broken = true;
        staging = null;
        record = null;
        return Optional.empty();
    }
}
