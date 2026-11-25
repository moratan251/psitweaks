package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaksqol.api.StringSpellHelper;
import java.util.PriorityQueue;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;

/**
 * The SNBT of a tag, written only as far as it is kept. The text is {@code StringSpellHelper.sanitize(tag.toString(),
 * maxLength)}: vanilla's SNBT ({@code StringTagVisitor}) with the control characters handled as
 * {@link StringSpellHelper#sanitize(String, int)} does and cut at {@code maxLength}. Writing stops at the cut, so a huge
 * compound or list costs only about the characters it keeps. {@link #work()} counts the steps writing took: each
 * character written or skipped, and each character looked at to pick a String's quote, to tell whether a key needs
 * quotes and to put the keys of a compound in order. Not thread-safe.
 */
public final class SnbtText implements TagVisitor {
    private final int maxLength;
    private final StringBuilder out;
    /** A carriage return waits for the next character: before a line feed it is dropped, otherwise it is a line break. */
    private boolean carriageReturn;
    private long work;

    private SnbtText(int maxLength) {
        this.maxLength = Math.max(0, maxLength);
        this.out = new StringBuilder(Math.min(this.maxLength, 256));
    }

    /** The SNBT of the tag, cut at {@code maxLength} characters. */
    public static SnbtText write(Tag tag, int maxLength) {
        SnbtText text = new SnbtText(maxLength);
        if (text.maxLength > 0) {
            tag.accept(text);
            text.finish();
        }
        return text;
    }

    /** The text. */
    public String text() {
        return out.toString();
    }

    /** The steps writing took. */
    public long work() {
        return work;
    }

    private boolean full() {
        return out.length() >= maxLength;
    }

    private void finish() {
        if (carriageReturn && !full()) {
            out.append('\n');
        }
        carriageReturn = false;
    }

    private void put(char c) {
        if (full()) {
            return;
        }
        work++;
        if (carriageReturn) {
            carriageReturn = false;
            if (c != '\n') {
                out.append('\n');
                if (full()) {
                    return;
                }
            }
        }
        if (c == '\n') {
            out.append('\n');
        } else if (c == '\r') {
            carriageReturn = true;
        } else if (c == '\t') {
            out.append(' ');
        } else if (!Character.isISOControl(c)) {
            out.append(c);
        }
    }

    private void put(String text) {
        for (int i = 0; i < text.length() && !full(); i++) {
            put(text.charAt(i));
        }
    }

    /** {@code StringTag.quoteAndEscape}: quotes with " unless a " comes before any ', and escapes \ and that quote. */
    private void quoted(String text) {
        char quote = '"';
        for (int i = 0; i < text.length(); i++) {
            work++;
            char c = text.charAt(i);
            if (c == '"') {
                quote = '\'';
                break;
            }
            if (c == '\'') {
                break;
            }
        }
        put(quote);
        for (int i = 0; i < text.length() && !full(); i++) {
            char c = text.charAt(i);
            if (c == '\\' || c == quote) {
                put('\\');
            }
            put(c);
        }
        put(quote);
    }

    @Override
    public void visitString(StringTag tag) {
        quoted(tag.getAsString());
    }

    @Override
    public void visitByte(ByteTag tag) {
        put(Byte.toString(tag.getAsByte()));
        put('b');
    }

    @Override
    public void visitShort(ShortTag tag) {
        put(Short.toString(tag.getAsShort()));
        put('s');
    }

    @Override
    public void visitInt(IntTag tag) {
        put(Integer.toString(tag.getAsInt()));
    }

    @Override
    public void visitLong(LongTag tag) {
        put(Long.toString(tag.getAsLong()));
        put('L');
    }

    @Override
    public void visitFloat(FloatTag tag) {
        put(Float.toString(tag.getAsFloat()));
        put('f');
    }

    @Override
    public void visitDouble(DoubleTag tag) {
        put(Double.toString(tag.getAsDouble()));
        put('d');
    }

    @Override
    public void visitByteArray(ByteArrayTag tag) {
        put("[B;");
        byte[] values = tag.getAsByteArray();
        for (int i = 0; i < values.length && !full(); i++) {
            if (i != 0) {
                put(',');
            }
            put(Byte.toString(values[i]));
            put('B');
        }
        put(']');
    }

    @Override
    public void visitIntArray(IntArrayTag tag) {
        put("[I;");
        int[] values = tag.getAsIntArray();
        for (int i = 0; i < values.length && !full(); i++) {
            if (i != 0) {
                put(',');
            }
            put(Integer.toString(values[i]));
        }
        put(']');
    }

    @Override
    public void visitLongArray(LongArrayTag tag) {
        put("[L;");
        long[] values = tag.getAsLongArray();
        for (int i = 0; i < values.length && !full(); i++) {
            if (i != 0) {
                put(',');
            }
            put(Long.toString(values[i]));
            put('L');
        }
        put(']');
    }

    @Override
    public void visitList(ListTag tag) {
        put('[');
        for (int i = 0; i < tag.size() && !full(); i++) {
            if (i != 0) {
                put(',');
            }
            tag.get(i).accept(this);
        }
        put(']');
    }

    @Override
    public void visitCompound(CompoundTag tag) {
        put('{');
        if (full()) {
            return;
        }
        // Vanilla sorts every key first. A heap gives the keys in the same order, sorted only as far as the text keeps.
        PriorityQueue<String> keys = new PriorityQueue<>(Math.max(1, tag.size()), this::compareKeys);
        keys.addAll(tag.getAllKeys());
        boolean first = true;
        while (!keys.isEmpty() && !full()) {
            String key = keys.poll();
            if (!first) {
                put(',');
            }
            first = false;
            if (isSimpleKey(key)) {
                put(key);
            } else {
                quoted(key);
            }
            put(':');
            Tag value = tag.get(key);
            if (value != null) {
                value.accept(this);
            }
        }
        put('}');
    }

    @Override
    public void visitEnd(EndTag tag) {
        put("END");
    }

    /** {@link String#compareTo}, counting each character looked at. */
    private int compareKeys(String a, String b) {
        int shorter = Math.min(a.length(), b.length());
        int i = 0;
        while (i < shorter && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        work += i + 1;
        return i < shorter ? a.charAt(i) - b.charAt(i) : a.length() - b.length();
    }

    /** Whether {@code StringTagVisitor} writes the key without quotes (it matches [A-Za-z0-9._+-]+), counting each character looked at. */
    private boolean isSimpleKey(String key) {
        if (key.isEmpty()) {
            return false;
        }
        for (int i = 0; i < key.length(); i++) {
            work++;
            char c = key.charAt(i);
            boolean simple = c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9'
                    || c == '.' || c == '_' || c == '+' || c == '-';
            if (!simple) {
                return false;
            }
        }
        return true;
    }
}
