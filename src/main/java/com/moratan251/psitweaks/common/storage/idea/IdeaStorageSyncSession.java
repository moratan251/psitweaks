package com.moratan251.psitweaks.common.storage.idea;

import com.moratan251.psitweaks.common.network.*;
import io.netty.buffer.Unpooled;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;

/** Menu-owned index. IDs are never reused and are not persisted into world data. */
public final class IdeaStorageSyncSession {
    private final Map<Object, IdeaStorageSyncEntry> previous = new LinkedHashMap<>();
    private final Map<Long, Object> keys = new HashMap<>();
    private long nextId = 1;
    private long revision;

    public Object resolve(long id) { return keys.get(id); }

    public void send(PlayerIdeaStorage storage, IdeaStorageMenuToken token, Consumer<MessageIdeaStorageSyncPart> output) {
        Map<Object, Long> current = new LinkedHashMap<>();
        storage.itemEntries().forEach(e -> current.put(e.getKey(), e.getValue()));
        storage.fluidEntries().forEach(e -> current.put(e.getKey(), e.getValue()));
        storage.chemicalEntries().forEach(e -> current.put(e.getKey(), e.getValue()));
        List<IdeaStorageSyncEntry> changes = new ArrayList<>();
        var iterator = previous.entrySet().iterator();
        while (iterator.hasNext()) {
            var old = iterator.next();
            if (!current.containsKey(old.getKey())) {
                changes.add(new IdeaStorageSyncEntry(old.getValue().id(), null, 0));
                keys.remove(old.getValue().id());
                iterator.remove();
            }
        }
        current.forEach((key, count) -> {
            IdeaStorageSyncEntry old = previous.get(key);
            if (old == null || old.amount() != count) {
                var next = new IdeaStorageSyncEntry(old == null ? nextId++ : old.id(), key, count);
                previous.put(key, next);
                keys.put(next.id(), key);
                changes.add(new IdeaStorageSyncEntry(next.id(), old == null ? key : null, count));
            }
        });
        boolean reset = revision == 0;
        long currentRevision = ++revision;
        FriendlyByteBuf frame = new FriendlyByteBuf(Unpooled.buffer(256, MessageIdeaStorageSyncPart.MAX_DATA));
        int sequence = 0;
        try {
            for (var entry : changes) {
                FriendlyByteBuf record = new FriendlyByteBuf(Unpooled.buffer());
                try {
                    record.writeInt(0);
                    entry.write(record);
                    record.setInt(0, record.writerIndex() - Integer.BYTES);
                    while (record.isReadable()) {
                        if (frame.readableBytes() == MessageIdeaStorageSyncPart.MAX_DATA) {
                            output.accept(part(token, currentRevision, sequence++, reset, false, storage, frame));
                            frame.clear();
                        }
                        frame.writeBytes(record, Math.min(record.readableBytes(),
                                MessageIdeaStorageSyncPart.MAX_DATA - frame.readableBytes()));
                    }
                } finally { record.release(); }
            }
            output.accept(part(token, currentRevision, sequence, reset, true, storage, frame));
        } finally { frame.release(); }
    }

    private static MessageIdeaStorageSyncPart part(IdeaStorageMenuToken token, long revision, int sequence,
            boolean reset, boolean last, PlayerIdeaStorage storage, FriendlyByteBuf frame) {
        byte[] bytes = new byte[frame.readableBytes()];
        frame.getBytes(frame.readerIndex(), bytes);
        return new MessageIdeaStorageSyncPart(token, revision, sequence, reset && sequence == 0, last,
                storage.maxItemTypes(), storage.maxFluidTypes(), storage.maxChemicalTypes(), storage.isLoadFailed(), bytes);
    }
}
