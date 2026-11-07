package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaksqol.api.StringSpellHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Matches the spell wildcards: * is any text, ? is any one character, [abc] is one of a, b or c, and a backslash
 * escapes *, ?, [, ] and itself. Text in between must match exactly, case included.
 *
 * <p>The pattern is split at each * into pieces of single-character tests. A value matches when the first piece
 * matches its start, the last piece its end, and the pieces in between appear in order; taking each one as early as
 * possible is enough, so there is no backtracking and a match takes at most (value length) x (piece length) tests.
 */
public final class WildcardStringMatcher {
    private static final int ANY = -1;

    /** Each piece is a run of single-character tests: a code point, {@link #ANY}, or an index into {@link #choices}. */
    private final int[][] pieces;
    private final int[][] choices;

    private WildcardStringMatcher(int[][] pieces, int[][] choices) {
        this.pieces = pieces;
        this.choices = choices;
    }

    /** Matches the whole value. */
    public static WildcardStringMatcher compile(String wildcard) {
        return parse(StringSpellHelper.sanitize(wildcard), false);
    }

    /** Matches when some part of the value matches. */
    public static WildcardStringMatcher compilePartial(String wildcard) {
        return parse(StringSpellHelper.sanitize(wildcard), true);
    }

    public boolean matches(String value) {
        int[] text = (value == null ? "" : value).codePoints().toArray();
        if (pieces.length == 1) {
            return text.length == pieces[0].length && matchesAt(pieces[0], text, 0);
        }

        int[] first = pieces[0];
        int[] last = pieces[pieces.length - 1];
        int end = text.length - last.length;
        if (end < first.length || !matchesAt(first, text, 0) || !matchesAt(last, text, end)) {
            return false;
        }

        int position = first.length;
        for (int i = 1; i < pieces.length - 1; i++) {
            int found = find(pieces[i], text, position, end);
            if (found < 0) {
                return false;
            }
            position = found + pieces[i].length;
        }
        return true;
    }

    private int find(int[] piece, int[] text, int from, int end) {
        for (int start = from; start + piece.length <= end; start++) {
            if (matchesAt(piece, text, start)) {
                return start;
            }
        }
        return -1;
    }

    private boolean matchesAt(int[] piece, int[] text, int start) {
        for (int i = 0; i < piece.length; i++) {
            if (!accepts(piece[i], text[start + i])) {
                return false;
            }
        }
        return true;
    }

    private boolean accepts(int test, int codePoint) {
        if (test == ANY) {
            return true;
        }
        if (test < ANY) {
            for (int choice : choices[choiceIndex(test)]) {
                if (choice == codePoint) {
                    return true;
                }
            }
            return false;
        }
        return test == codePoint;
    }

    private static WildcardStringMatcher parse(String wildcard, boolean partial) {
        int[] pattern = wildcard.codePoints().toArray();
        List<int[]> pieces = new ArrayList<>();
        List<int[]> choices = new ArrayList<>();
        Piece current = new Piece();
        if (partial) {
            pieces.add(new int[0]);
        }

        boolean escaped = false;
        for (int i = 0; i < pattern.length; i++) {
            int c = pattern[i];
            if (escaped) {
                if (!isEscapable(c)) {
                    current.add('\\');
                }
                current.add(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '*') {
                pieces.add(current.toArray());
                current = new Piece();
            } else if (c == '?') {
                current.add(ANY);
            } else if (c == '[') {
                int close = characterClassEnd(pattern, i);
                if (close < 0) {
                    current.add('[');
                } else {
                    current.add(choiceTest(choices.size()));
                    choices.add(characterClass(pattern, i + 1, close));
                    i = close;
                }
            } else {
                current.add(c);
            }
        }
        if (escaped) {
            current.add('\\');
        }
        pieces.add(current.toArray());
        if (partial) {
            pieces.add(new int[0]);
        }
        return new WildcardStringMatcher(pieces.toArray(int[][]::new), choices.toArray(int[][]::new));
    }

    private static boolean isEscapable(int c) {
        return c == '*' || c == '?' || c == '[' || c == ']' || c == '\\';
    }

    /** Where the class opened at {@code open} closes, or -1 when it is empty or never closes (then [ is literal). */
    private static int characterClassEnd(int[] pattern, int open) {
        boolean escaped = false;
        boolean empty = true;
        for (int i = open + 1; i < pattern.length; i++) {
            int c = pattern[i];
            if (escaped) {
                escaped = false;
                empty = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == ']') {
                return empty ? -1 : i;
            } else {
                empty = false;
            }
        }
        return -1;
    }

    private static int[] characterClass(int[] pattern, int from, int close) {
        Piece result = new Piece();
        boolean escaped = false;
        for (int i = from; i < close; i++) {
            int c = pattern[i];
            if (!escaped && c == '\\') {
                escaped = true;
            } else {
                result.add(c);
                escaped = false;
            }
        }
        return result.toArray();
    }

    private static int choiceTest(int index) {
        return ANY - 1 - index;
    }

    private static int choiceIndex(int test) {
        return ANY - 1 - test;
    }

    private static final class Piece {
        private int[] values = new int[8];
        private int size;

        void add(int value) {
            if (size == values.length) {
                values = Arrays.copyOf(values, size * 2);
            }
            values[size++] = value;
        }

        int[] toArray() {
            return Arrays.copyOf(values, size);
        }
    }
}
