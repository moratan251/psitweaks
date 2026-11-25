package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaksqol.api.StringSpellHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Matches the spell wildcards: * is any text, ? is any one character, [abc] is one of a, b or c, and a backslash
 * escapes *, ?, [, ] and itself. Text in between must match exactly, case included.
 *
 * <p>The pattern is split at each * into pieces of single-character tests. A value matches when the first piece
 * matches its start, the last piece its end, and the pieces in between appear in order; taking each one as early as
 * possible is enough, so there is no backtracking and a match takes at most (value length) x (piece length) tests.
 * Spell pieces match through {@link #matches(String, WildcardBudget)}, which counts those tests against the cast.
 */
public final class WildcardStringMatcher {
    private static final int ANY = -1;
    /** {@link #characterClassEnd}: the class has nothing in it. */
    private static final int EMPTY_CLASS = -1;
    /** {@link #characterClassEnd}: no ] closes the class. */
    private static final int UNCLOSED_CLASS = -2;

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

    /** Whether the value matches, with no limit on the work. */
    public boolean matches(String value) {
        return run(codePoints(value), null);
    }

    /**
     * Whether the value matches, taking every character test from the budget: the tests that pass and the one that
     * fails. Going past the budget stops the spell with {@link WildcardBudget#ERROR_TOO_MANY_COMPARISONS}.
     */
    public boolean matches(String value, WildcardBudget budget) throws SpellRuntimeException {
        boolean result = run(codePoints(value), budget);
        budget.throwIfSpent();
        return result;
    }

    private static int[] codePoints(String value) {
        return (value == null ? "" : value).codePoints().toArray();
    }

    /** The match; false as soon as the budget runs out, which the caller then reports. */
    private boolean run(int[] text, @Nullable WildcardBudget budget) {
        if (pieces.length == 1) {
            return text.length == pieces[0].length && matchesAt(pieces[0], text, 0, budget);
        }

        int[] first = pieces[0];
        int[] last = pieces[pieces.length - 1];
        int end = text.length - last.length;
        if (end < first.length || !matchesAt(first, text, 0, budget) || !matchesAt(last, text, end, budget)) {
            return false;
        }

        int position = first.length;
        for (int i = 1; i < pieces.length - 1; i++) {
            int found = find(pieces[i], text, position, end, budget);
            if (found < 0) {
                return false;
            }
            position = found + pieces[i].length;
        }
        return true;
    }

    private int find(int[] piece, int[] text, int from, int end, @Nullable WildcardBudget budget) {
        for (int start = from; start + piece.length <= end; start++) {
            int passed = passed(piece, text, start);
            if (!charge(budget, passed, piece.length)) {
                return -1;
            }
            if (passed == piece.length) {
                return start;
            }
        }
        return -1;
    }

    private boolean matchesAt(int[] piece, int[] text, int start, @Nullable WildcardBudget budget) {
        int passed = passed(piece, text, start);
        return charge(budget, passed, piece.length) && passed == piece.length;
    }

    /** How many tests of the piece pass in a row when it is laid at start. */
    private int passed(int[] piece, int[] text, int start) {
        int passed = 0;
        while (passed < piece.length && accepts(piece[passed], text[start + passed])) {
            passed++;
        }
        return passed;
    }

    /** Takes the tests of one placement from the budget (the passing ones and the failing one); false once it runs out. */
    private static boolean charge(@Nullable WildcardBudget budget, int passed, int length) {
        return budget == null || budget.spend(passed < length ? passed + 1L : passed);
    }

    private boolean accepts(int test, int codePoint) {
        if (test == ANY) {
            return true;
        }
        if (test < ANY) {
            return Arrays.binarySearch(choices[choiceIndex(test)], codePoint) >= 0;
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
        // Once a [ finds no ] after it, no later [ can, so those are read as text without looking again: otherwise n
        // unclosed [ would cost about n^2 / 2 steps.
        boolean unclosed = false;
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
                // Only the first piece may be empty here: a run of * needs no pieces in between.
                if (pieces.isEmpty() || current.size > 0) {
                    pieces.add(current.toArray());
                    current = new Piece();
                }
            } else if (c == '?') {
                current.add(ANY);
            } else if (c == '[') {
                int close = unclosed ? UNCLOSED_CLASS : characterClassEnd(pattern, i);
                if (close == UNCLOSED_CLASS) {
                    unclosed = true;
                }
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

    /**
     * Where the class opened at {@code open} closes, or {@link #EMPTY_CLASS} / {@link #UNCLOSED_CLASS} when it is empty or
     * never closes (then [ is literal). An empty class is found at the first ], so only an unclosed one reads to the end.
     */
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
                return empty ? EMPTY_CLASS : i;
            } else {
                empty = false;
            }
        }
        return UNCLOSED_CLASS;
    }

    /** The characters of a class, sorted for {@link #accepts}. */
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
        int[] sorted = result.toArray();
        Arrays.sort(sorted);
        return sorted;
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
