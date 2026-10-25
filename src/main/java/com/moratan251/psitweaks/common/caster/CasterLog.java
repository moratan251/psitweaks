package com.moratan251.psitweaks.common.caster;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/**
 * 自律詠唱機の作動ログ。
 * <p>
 * サーバー側 BlockEntity が正本を持ち、GUI を開いているプレイヤーには revision 単位の差分を送る。
 * クライアント側では画面が同じクラスのインスタンスを持ち、{@link #apply(Delta)} で差分を反映する。
 * 直前と同じ通常メッセージは {@code ×N} にまとめ、スパム抑制デバッグは同じ置換キーの過去エントリを置き換える。
 */
public final class CasterLog {
    /** 差分で表現できなくなった時に snapshot へ切り替えるまでに覚えておく削除記録の数。 */
    private static final int MAX_TOMBSTONES = 128;
    private static final int MAX_WIRE_ENTRIES = 4096;
    private static final int MAX_SANITIZE_DEPTH = 32;
    private static final String ELLIPSIS = "…";
    private static final String TAG_NEXT_SEQUENCE = "NextSequence";
    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_SEQUENCE = "Seq";
    private static final String TAG_FIRST = "First";
    private static final String TAG_LAST = "Last";
    private static final String TAG_SOURCE = "Source";
    private static final String TAG_MESSAGE = "Message";
    private static final String TAG_REPEAT = "Repeat";
    private static final String TAG_KEY = "Key";

    public enum Source {
        /** Caster 宛てのチャット相当メッセージ（デバッグ出力、Psi の詠唱エラーなど）。 */
        CASTER_MESSAGE,
        /** 自律詠唱機自身が検出したエラー。 */
        MACHINE_ERROR,
        /** 「作動式: デバッグ（スパム抑制）」の出力。 */
        SPAMLESS;

        static Source byOrdinal(int ordinal) {
            Source[] values = values();
            return values[Mth.clamp(ordinal, 0, values.length - 1)];
        }

        static Source byName(String name) {
            try {
                return valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return CASTER_MESSAGE;
            }
        }
    }

    /**
     * ログ 1 行分。{@code replacementKey} は {@link Source#SPAMLESS} のときだけ意味を持つ。
     * {@code revision} はサーバー側で差分抽出に使う値で、通信・保存はしない。
     */
    public record Entry(long sequence, long firstGameTime, long lastGameTime, Source source, Component message,
                        int repeatCount, int replacementKey, long revision) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.of(
                (buf, entry) -> {
                    buf.writeVarLong(entry.sequence());
                    buf.writeLong(entry.firstGameTime());
                    buf.writeLong(entry.lastGameTime());
                    buf.writeVarInt(entry.source().ordinal());
                    ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, entry.message());
                    buf.writeVarInt(entry.repeatCount());
                    buf.writeInt(entry.replacementKey());
                },
                buf -> new Entry(
                        buf.readVarLong(),
                        buf.readLong(),
                        buf.readLong(),
                        Source.byOrdinal(buf.readVarInt()),
                        ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf),
                        Math.max(1, buf.readVarInt()),
                        buf.readInt(),
                        0L));

        public boolean isSpamless() {
            return source == Source.SPAMLESS;
        }

        private Entry repeated(long gameTime, long newRevision) {
            int repeats = repeatCount == Integer.MAX_VALUE ? repeatCount : repeatCount + 1;
            return new Entry(sequence, firstGameTime, gameTime, source, message, repeats, replacementKey, newRevision);
        }

        private Entry withRevision(long newRevision) {
            return new Entry(sequence, firstGameTime, lastGameTime, source, message, repeatCount, replacementKey, newRevision);
        }
    }

    /**
     * {@code baseRevision} の状態から {@code revision} の状態へ進める差分。
     * {@code snapshot} のときはクライアント側の内容を全て置き換える。
     */
    public record Delta(boolean snapshot, long baseRevision, long revision, long minSequence,
                        List<Long> removedSequences, List<Entry> upserts) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Delta> STREAM_CODEC = StreamCodec.of(
                (buf, delta) -> {
                    buf.writeBoolean(delta.snapshot());
                    buf.writeVarLong(delta.baseRevision());
                    buf.writeVarLong(delta.revision());
                    buf.writeVarLong(delta.minSequence());
                    buf.writeVarInt(delta.removedSequences().size());
                    for (long sequence : delta.removedSequences()) {
                        buf.writeVarLong(sequence);
                    }
                    buf.writeVarInt(delta.upserts().size());
                    for (Entry entry : delta.upserts()) {
                        Entry.STREAM_CODEC.encode(buf, entry);
                    }
                },
                buf -> {
                    boolean snapshot = buf.readBoolean();
                    long baseRevision = buf.readVarLong();
                    long revision = buf.readVarLong();
                    long minSequence = buf.readVarLong();
                    int removedCount = checkedSize(buf.readVarInt());
                    List<Long> removed = new ArrayList<>(removedCount);
                    for (int i = 0; i < removedCount; i++) {
                        removed.add(buf.readVarLong());
                    }
                    int upsertCount = checkedSize(buf.readVarInt());
                    List<Entry> upserts = new ArrayList<>(upsertCount);
                    for (int i = 0; i < upsertCount; i++) {
                        upserts.add(Entry.STREAM_CODEC.decode(buf));
                    }
                    return new Delta(snapshot, baseRevision, revision, minSequence, removed, upserts);
                });

        public Delta {
            removedSequences = List.copyOf(removedSequences);
            upserts = List.copyOf(upserts);
        }

        public boolean isEmpty() {
            return !snapshot && removedSequences.isEmpty() && upserts.isEmpty();
        }

        private static int checkedSize(int size) {
            if (size < 0 || size > MAX_WIRE_ENTRIES) {
                throw new IllegalArgumentException("Autonomous caster log delta is too large: " + size);
            }
            return size;
        }
    }

    private final IntSupplier capacity;
    private final IntSupplier maxMessageLength;
    private final ArrayDeque<Entry> entries = new ArrayDeque<>();
    /** [sequence, revision] の削除記録。 */
    private final ArrayDeque<long[]> tombstones = new ArrayDeque<>();
    private long nextSequence = 1L;
    private long revision = 1L;
    /** これより前の revision からは差分を作れない（消去・読込・削除記録の溢れ）。 */
    private long snapshotFloor = 1L;

    public CasterLog(IntSupplier capacity, IntSupplier maxMessageLength) {
        this.capacity = Objects.requireNonNull(capacity);
        this.maxMessageLength = Objects.requireNonNull(maxMessageLength);
    }

    /** クライアント側コピー用。サーバーから届いた内容をそのまま保持する。 */
    public static CasterLog clientCopy() {
        return new CasterLog(() -> Integer.MAX_VALUE, () -> Integer.MAX_VALUE);
    }

    public long revision() {
        return revision;
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public Optional<Entry> latest() {
        return Optional.ofNullable(entries.peekLast());
    }

    /** 通常メッセージを追加する。直前と同じ source・内容なら回数だけ増やす。 */
    public void add(Source source, Component message, long gameTime) {
        if (source == Source.SPAMLESS || message == null) {
            return;
        }
        Component limited = limitLength(sanitize(message), maxMessageLength());
        long newRevision = ++revision;
        Entry last = entries.peekLast();
        if (last != null && last.source() == source && last.message().equals(limited)) {
            entries.pollLast();
            entries.addLast(last.repeated(gameTime, newRevision));
            return;
        }
        entries.addLast(new Entry(nextSequence++, gameTime, gameTime, source, limited, 1, 0, newRevision));
        trimToCapacity();
    }

    /** スパム抑制デバッグの出力。同じ置換キーの過去エントリを消してから末尾へ追加する。 */
    public void addSpamless(Component message, int replacementKey, long gameTime) {
        if (message == null) {
            return;
        }
        Component limited = limitLength(sanitize(message), maxMessageLength());
        long newRevision = ++revision;
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.isSpamless() && entry.replacementKey() == replacementKey) {
                iterator.remove();
                recordTombstone(entry.sequence(), newRevision);
            }
        }
        entries.addLast(new Entry(nextSequence++, gameTime, gameTime, Source.SPAMLESS, limited, 1, replacementKey, newRevision));
        trimToCapacity();
    }

    public void clear() {
        entries.clear();
        tombstones.clear();
        revision++;
        snapshotFloor = revision;
    }

    /** {@code baseRevision} を知っている閲覧者へ送る差分。差分で表せなければ snapshot を返す。 */
    public Delta deltaSince(long baseRevision) {
        if (baseRevision <= 0L || baseRevision < snapshotFloor || baseRevision > revision) {
            return snapshot();
        }
        List<Long> removed = new ArrayList<>();
        for (long[] tombstone : tombstones) {
            if (tombstone[1] > baseRevision) {
                removed.add(tombstone[0]);
            }
        }
        List<Entry> upserts = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.revision() > baseRevision) {
                upserts.add(entry);
            }
        }
        return new Delta(false, baseRevision, revision, minSequence(), removed, upserts);
    }

    public Delta snapshot() {
        return new Delta(true, 0L, revision, minSequence(), List.of(), new ArrayList<>(entries));
    }

    /**
     * クライアント側コピーへ差分を反映する。
     *
     * @return 反映できた場合 true。手元の revision と差分の起点が合わない場合は false（snapshot の再要求が必要）。
     */
    public boolean apply(Delta delta) {
        if (delta.snapshot()) {
            entries.clear();
            tombstones.clear();
            for (Entry entry : delta.upserts()) {
                entries.addLast(entry.withRevision(delta.revision()));
            }
            revision = delta.revision();
            snapshotFloor = revision;
            return true;
        }
        if (delta.baseRevision() != revision) {
            return false;
        }
        if (!delta.removedSequences().isEmpty()) {
            entries.removeIf(entry -> delta.removedSequences().contains(entry.sequence()));
        }
        entries.removeIf(entry -> entry.sequence() < delta.minSequence());
        for (Entry upsert : delta.upserts()) {
            replaceOrAppend(upsert.withRevision(delta.revision()));
        }
        revision = delta.revision();
        return true;
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong(TAG_NEXT_SEQUENCE, nextSequence);
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong(TAG_SEQUENCE, entry.sequence());
            entryTag.putLong(TAG_FIRST, entry.firstGameTime());
            entryTag.putLong(TAG_LAST, entry.lastGameTime());
            entryTag.putString(TAG_SOURCE, entry.source().name());
            entryTag.putString(TAG_MESSAGE, toJson(entry.message(), registries));
            entryTag.putInt(TAG_REPEAT, entry.repeatCount());
            if (entry.isSpamless()) {
                entryTag.putInt(TAG_KEY, entry.replacementKey());
            }
            list.add(entryTag);
        }
        tag.put(TAG_ENTRIES, list);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        entries.clear();
        tombstones.clear();
        revision++;
        snapshotFloor = revision;
        long maxSequence = 0L;
        ListTag list = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            Component message = parseMessage(entryTag.getString(TAG_MESSAGE), registries);
            if (message == null) {
                continue;
            }
            long sequence = entryTag.getLong(TAG_SEQUENCE);
            if (sequence <= maxSequence) {
                continue;
            }
            maxSequence = sequence;
            entries.addLast(new Entry(
                    sequence,
                    entryTag.getLong(TAG_FIRST),
                    entryTag.getLong(TAG_LAST),
                    Source.byName(entryTag.getString(TAG_SOURCE)),
                    message,
                    Math.max(1, entryTag.getInt(TAG_REPEAT)),
                    entryTag.getInt(TAG_KEY),
                    revision));
        }
        nextSequence = Math.max(maxSequence + 1L, Math.max(1L, tag.getLong(TAG_NEXT_SEQUENCE)));
        trimToCapacity();
    }

    /**
     * 表示文字数の上限を超えるメッセージだけを、スタイルを保ったリテラルへ平坦化して切り詰める。
     * 上限内のメッセージは翻訳キーを保ったまま返す。
     */
    public static Component limitLength(Component message, int maxLength) {
        int limit = Math.max(1, maxLength);
        String plain = message.getString();
        if (plain.length() <= limit) {
            return message;
        }
        int keep = Math.max(0, limit - ELLIPSIS.length());
        MutableComponent result = Component.empty();
        int[] remaining = {keep};
        message.visit((style, text) -> {
            if (remaining[0] <= 0) {
                return Optional.of(Boolean.TRUE);
            }
            String part = text.length() <= remaining[0] ? text : text.substring(0, remaining[0]);
            remaining[0] -= part.length();
            if (!part.isEmpty()) {
                result.append(Component.literal(part).withStyle(style.withClickEvent(null)));
            }
            return remaining[0] <= 0 ? Optional.of(Boolean.TRUE) : Optional.empty();
        }, Style.EMPTY);
        return result.append(Component.literal(ELLIPSIS).withStyle(ChatFormatting.GRAY));
    }

    /**
     * NBT 保存と通信で必ず直列化できる形へ写す。
     * 翻訳引数の任意オブジェクトは文字列化し、ログ画面では実行しないクリックイベントを外す。
     */
    public static Component sanitize(Component message) {
        return sanitize(message, 0);
    }

    private static MutableComponent sanitize(Component message, int depth) {
        if (depth > MAX_SANITIZE_DEPTH) {
            return Component.literal(message.getString()).withStyle(message.getStyle().withClickEvent(null));
        }
        MutableComponent copy;
        if (message.getContents() instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs();
            Object[] safeArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                safeArgs[i] = sanitizeArgument(args[i], depth);
            }
            copy = MutableComponent.create(new TranslatableContents(translatable.getKey(), translatable.getFallback(), safeArgs));
        } else {
            copy = MutableComponent.create(message.getContents());
        }
        copy.setStyle(message.getStyle().withClickEvent(null));
        for (Component sibling : message.getSiblings()) {
            copy.append(sanitize(sibling, depth + 1));
        }
        return copy;
    }

    private static Object sanitizeArgument(Object argument, int depth) {
        if (argument instanceof Component component) {
            return sanitize(component, depth + 1);
        }
        if (argument instanceof String || argument instanceof Boolean || argument instanceof Integer
                || argument instanceof Long || argument instanceof Short || argument instanceof Byte
                || argument instanceof Float || argument instanceof Double) {
            return argument;
        }
        return String.valueOf(argument);
    }

    private static String toJson(Component message, HolderLookup.Provider registries) {
        try {
            return Component.Serializer.toJson(message, registries);
        } catch (RuntimeException e) {
            return Component.Serializer.toJson(Component.literal(message.getString()), registries);
        }
    }

    private void replaceOrAppend(Entry entry) {
        Entry last = entries.peekLast();
        if (last != null && last.sequence() == entry.sequence()) {
            entries.pollLast();
            entries.addLast(entry);
            return;
        }
        if (last == null || last.sequence() < entry.sequence()) {
            entries.addLast(entry);
            return;
        }
        List<Entry> copy = new ArrayList<>(entries);
        boolean replaced = false;
        for (int i = 0; i < copy.size(); i++) {
            if (copy.get(i).sequence() == entry.sequence()) {
                copy.set(i, entry);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            copy.add(entry);
            copy.sort((a, b) -> Long.compare(a.sequence(), b.sequence()));
        }
        entries.clear();
        entries.addAll(copy);
    }

    /** 最古側から削る。最古側の削除は差分の minSequence で伝わるため、削除記録は残さない。 */
    private void trimToCapacity() {
        int max = Math.max(1, capacity.getAsInt());
        while (entries.size() > max) {
            entries.pollFirst();
        }
    }

    private void recordTombstone(long sequence, long newRevision) {
        tombstones.addLast(new long[] {sequence, newRevision});
        while (tombstones.size() > MAX_TOMBSTONES) {
            long[] dropped = tombstones.pollFirst();
            snapshotFloor = Math.max(snapshotFloor, dropped[1]);
        }
    }

    private long minSequence() {
        Entry first = entries.peekFirst();
        return first == null ? nextSequence : first.sequence();
    }

    private int maxMessageLength() {
        return Math.max(1, maxMessageLength.getAsInt());
    }

    private static Component parseMessage(String json, HolderLookup.Provider registries) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return Component.Serializer.fromJson(json, registries);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
