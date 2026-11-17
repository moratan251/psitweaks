package com.moratan251.psitweaks.common.spells.util;

import com.mojang.brigadier.StringReader;
import com.moratan251.psitweaksqol.api.StringSpellHelper;
import java.util.List;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * Path lookup and value formatting shared by the NBT pieces. A path uses the vanilla NBT path syntax (keys joined by
 * dots, "quoted keys", [n] with negative n counting from the end, [] for every element, [{...}] and {...} filters).
 * An empty path is the whole root, an exact top-level key wins over path parsing, and an unquoted key that vanilla
 * would cut short (a space, a quote or a closing bracket in it) is quoted first. Whole numbers written the way spell
 * strings print them are accepted too: an index [3.0] reads as [3], and 3.0b, 3.0s or 3.0L in a {...} filter as 3b,
 * 3s or 3L. Lookups never throw: a bad path, a missing key and a wrong type all give no match.
 */
public final class NbtPathHelper {
    private static final Pattern WHOLE_INDEX = Pattern.compile("\\[\\s*(-?\\d+)\\.0+\\s*]");
    private static final Pattern WHOLE_INTEGER_TAG = Pattern.compile("(?<![\\w.])(-?\\d+)\\.0+([bBsSlL])(?!\\w)");

    private NbtPathHelper() {
    }

    /** Every tag the path reaches from the root, in vanilla order; empty when the root is null or nothing matches. */
    public static List<Tag> find(@Nullable CompoundTag root, @Nullable String path) {
        if (root == null) {
            return List.of();
        }
        String trimmed = path == null ? "" : path.trim();
        if (trimmed.isEmpty()) {
            return List.of(root);
        }
        Tag direct = root.get(trimmed);
        if (direct != null) {
            return List.of(direct);
        }
        try {
            StringReader reader = new StringReader(quoteBareKeys(trimmed));
            NbtPathArgument.NbtPath parsed = new NbtPathArgument().parse(reader);
            if (reader.canRead()) {
                return List.of();
            }
            return parsed.get(root);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    /**
     * The text of a tag: a String as it is, a number without its type suffix (1b is 1, 2.0d is 2.0), and anything
     * else (compounds, lists, arrays) as SNBT. Control characters are dropped and the length is capped.
     */
    public static String text(Tag tag) {
        if (tag instanceof StringTag string) {
            return StringSpellHelper.sanitize(string.getAsString());
        }
        if (tag instanceof FloatTag number) {
            return Float.toString(number.getAsFloat());
        }
        if (tag instanceof DoubleTag number) {
            return Double.toString(number.getAsDouble());
        }
        if (tag instanceof NumericTag number) {
            return Long.toString(number.getAsLong());
        }
        return StringSpellHelper.sanitize(tag.toString());
    }

    /** The value of a number tag; empty for every other tag, numeric-looking Strings included, and for NaN or infinity. */
    public static OptionalDouble number(Tag tag) {
        if (tag instanceof NumericTag number) {
            double value = number.getAsDouble();
            return Double.isFinite(value) ? OptionalDouble.of(value) : OptionalDouble.empty();
        }
        return OptionalDouble.empty();
    }

    /**
     * Quotes the unquoted key segments vanilla would read only in part, escaping backslashes and double quotes.
     * Quoted keys and [...] / {...} parts are copied as they are.
     */
    static String quoteBareKeys(String path) {
        StringBuilder out = new StringBuilder(path.length() + 8);
        int i = 0;
        while (i < path.length()) {
            char c = path.charAt(i);
            if (c == '"' || c == '\'') {
                int end = quotedEnd(path, i);
                out.append(path, i, end);
                i = end;
            } else if (c == '[' || c == '{') {
                int end = blockEnd(path, i);
                out.append(wholeNumbers(path.substring(i, end)));
                i = end;
            } else if (c == '.') {
                out.append('.');
                i++;
            } else {
                int end = i;
                while (end < path.length() && ".[{".indexOf(path.charAt(end)) < 0) {
                    end++;
                }
                String key = path.substring(i, end);
                out.append(needsQuotes(key) ? quote(key) : key);
                i = end;
            }
        }
        return out.toString();
    }

    /**
     * Rewrites whole numbers with a fraction in a [...] or {...} part: a whole index such as [3.0] becomes [3], and
     * outside quoted strings an integer tag such as 3.0b becomes 3b. Real fractions are left alone.
     */
    private static String wholeNumbers(String block) {
        Matcher index = WHOLE_INDEX.matcher(block);
        if (index.matches()) {
            return "[" + index.group(1) + "]";
        }
        StringBuilder out = new StringBuilder(block.length());
        int i = 0;
        while (i < block.length()) {
            char c = block.charAt(i);
            if (c == '"' || c == '\'') {
                int end = quotedEnd(block, i);
                out.append(block, i, end);
                i = end;
            } else {
                int end = i;
                while (end < block.length() && block.charAt(end) != '"' && block.charAt(end) != '\'') {
                    end++;
                }
                out.append(WHOLE_INTEGER_TAG.matcher(block.substring(i, end)).replaceAll("$1$2"));
                i = end;
            }
        }
        return out.toString();
    }

    private static boolean needsQuotes(String key) {
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == ' ' || c == '"' || c == '\'' || c == ']' || c == '}') {
                return true;
            }
        }
        return false;
    }

    private static String quote(String key) {
        return '"' + key.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    /** The index after the quoted string that starts at {@code start}, or the end of the path when it never closes. */
    private static int quotedEnd(String path, int start) {
        char quote = path.charAt(start);
        for (int i = start + 1; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == quote) {
                return i + 1;
            }
        }
        return path.length();
    }

    /** The index after the bracketed part that starts at {@code start}, or the end of the path when it never closes. */
    private static int blockEnd(String path, int start) {
        int depth = 0;
        int i = start;
        while (i < path.length()) {
            char c = path.charAt(i);
            if (c == '"' || c == '\'') {
                i = quotedEnd(path, i);
                continue;
            }
            if (c == '[' || c == '{') {
                depth++;
            } else if (c == ']' || c == '}') {
                depth--;
                if (depth == 0) {
                    return i + 1;
                }
            }
            i++;
        }
        return path.length();
    }
}
