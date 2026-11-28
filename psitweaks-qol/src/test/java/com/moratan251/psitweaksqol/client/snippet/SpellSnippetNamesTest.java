package com.moratan251.psitweaksqol.client.snippet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class SpellSnippetNamesTest {
    @Test
    void fileNameReplacesCharactersWindowsCannotUse() {
        assertEquals("a_b_c_.snbt", SpellSnippetNames.fileNameFor("a/b:c?"));
        assertEquals("ループ処理.snbt", SpellSnippetNames.fileNameFor("  ループ処理  "));
        assertEquals("名前.snbt", SpellSnippetNames.fileNameFor("名前. "));
        assertEquals("_con.snbt", SpellSnippetNames.fileNameFor("con"));
        assertEquals("_LPT1.txt.snbt", SpellSnippetNames.fileNameFor("LPT1.txt"));
        assertEquals("_.snbt", SpellSnippetNames.fileNameFor("..."));
    }

    @Test
    void laterFileNameCandidatesGetANumberBeforeTheExtension() {
        assertEquals("a_b.snbt", SpellSnippetNames.fileNameFor("a:b", 1));
        assertEquals("a_b~2.snbt", SpellSnippetNames.fileNameFor("a:b", 2));
        assertEquals("_con~3.snbt", SpellSnippetNames.fileNameFor("con", 3));
        assertEquals("名前~2.snbt", SpellSnippetNames.fileNameFor("名前. ", 2));
    }

    @Test
    void nameMustBeShortAndPrintable() {
        assertEquals(Optional.of("abc"), SpellSnippetNames.validateName("  abc "));
        assertEquals(Optional.empty(), SpellSnippetNames.validateName("   "));
        assertEquals(Optional.empty(), SpellSnippetNames.validateName(null));
        assertEquals(Optional.empty(), SpellSnippetNames.validateName("a\nb"));
        assertEquals(Optional.of("あ".repeat(32)), SpellSnippetNames.validateName("あ".repeat(32)));
        assertEquals(Optional.empty(), SpellSnippetNames.validateName("あ".repeat(33)));
        assertEquals(Optional.of("😀".repeat(32)), SpellSnippetNames.validateName("😀".repeat(32)));
    }

    @Test
    void nameFromFileNameDropsExtensionAndKeepsWholeCharacters() {
        assertEquals("手で置いた", SpellSnippetNames.nameFromFileName("手で置いた.snbt"));
        assertEquals("😀".repeat(32), SpellSnippetNames.nameFromFileName("😀".repeat(40) + ".snbt"));
        assertEquals("_", SpellSnippetNames.nameFromFileName(".snbt"));
    }
}
