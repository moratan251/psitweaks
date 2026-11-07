package com.moratan251.psitweaks.common.spells.wrapper;

/**
 * Size limits of the String, Number and Vector lists. Without them, joining a list to itself a dozen times doubles
 * it each time and can use up the memory. Elements past a limit are dropped, as a string past its length limit is cut.
 */
public final class SpellListLimits {
    /** Splitting a string of the longest length into single characters still fits; regions stop at 4096 blocks. */
    public static final int MAX_ELEMENTS = 8192;
    /** The characters of all the strings in a String List together. */
    public static final int MAX_STRING_LIST_CHARACTERS = 65536;

    private SpellListLimits() {
    }
}
