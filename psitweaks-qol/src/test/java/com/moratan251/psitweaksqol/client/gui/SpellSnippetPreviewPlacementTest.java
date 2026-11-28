package com.moratan251.psitweaksqol.client.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpellSnippetPreviewPlacementTest {
    private static final int GUI_WIDTH = 174;

    private static int centeredLeft(int screenWidth) {
        return (screenWidth - GUI_WIDTH) / 2;
    }

    @Test
    void maxWidthIsTheWiderSideRoom() {
        assertEquals(147, SpellSnippetPreviewPlacement.maxWidth(480, centeredLeft(480), GUI_WIDTH));
        assertEquals(387, SpellSnippetPreviewPlacement.maxWidth(960, centeredLeft(960), GUI_WIDTH));
        assertEquals(150, SpellSnippetPreviewPlacement.maxWidth(480, 150, GUI_WIDTH));
    }

    @Test
    void boxGoesRightWhenItFits() {
        assertEquals(331, SpellSnippetPreviewPlacement.left(480, centeredLeft(480), GUI_WIDTH, 110));
        assertEquals(331, SpellSnippetPreviewPlacement.left(480, centeredLeft(480), GUI_WIDTH, 147));
    }

    @Test
    void boxGoesLeftWhenOnlyTheLeftSideFits() {
        int guiLeft = 200;
        int boxWidth = 120;
        assertEquals(guiLeft - SpellSnippetPreviewPlacement.GAP - boxWidth,
                SpellSnippetPreviewPlacement.left(480, guiLeft, GUI_WIDTH, boxWidth));
    }

    @Test
    void boxWiderThanBothSidesIsKeptOnScreen() {
        int maxWidth = SpellSnippetPreviewPlacement.maxWidth(320, centeredLeft(320), GUI_WIDTH);
        assertEquals(SpellSnippetPreviewPlacement.MIN_WIDTH, maxWidth);
        assertEquals(320 - SpellSnippetPreviewPlacement.MARGIN - maxWidth,
                SpellSnippetPreviewPlacement.left(320, centeredLeft(320), GUI_WIDTH, maxWidth));
        assertEquals(SpellSnippetPreviewPlacement.MARGIN,
                SpellSnippetPreviewPlacement.left(200, 10, GUI_WIDTH, 300));
    }
}
