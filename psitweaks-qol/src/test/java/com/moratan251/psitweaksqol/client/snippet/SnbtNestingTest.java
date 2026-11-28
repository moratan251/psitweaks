package com.moratan251.psitweaksqol.client.snippet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SnbtNestingTest {
    @Test
    void countsCompoundsAndListsTogether() {
        assertFalse(SnbtNesting.deeperThan("{a:[{b:1}]}", 3));
        assertTrue(SnbtNesting.deeperThan("{a:[{b:[1]}]}", 3));
        assertFalse(SnbtNesting.deeperThan("{a:1}{b:{c:2}}", 2));
    }

    @Test
    void bracketsInsideQuotesDoNotCount() {
        assertFalse(SnbtNesting.deeperThan("{a:\"[[[[{{{{\"}", 1));
        assertFalse(SnbtNesting.deeperThan("{a:'[[\\'[[{{'}", 1));
        assertTrue(SnbtNesting.deeperThan("{a:\"\\\"\",b:{c:{}}}", 2));
    }

    @Test
    void deepFilesAreCaughtBeforeParsing() {
        String deep = "{a:" + "[".repeat(100_000) + "]".repeat(100_000) + "}";
        assertTrue(SnbtNesting.deeperThan(deep, SnbtNesting.MAX_DEPTH));
        String shallow = "{a:" + "[".repeat(SnbtNesting.MAX_DEPTH - 1) + "]".repeat(SnbtNesting.MAX_DEPTH - 1) + "}";
        assertFalse(SnbtNesting.deeperThan(shallow, SnbtNesting.MAX_DEPTH));
    }
}
