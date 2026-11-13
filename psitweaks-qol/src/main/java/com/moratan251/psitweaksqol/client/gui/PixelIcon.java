package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/** フォントの記号は小さくつぶれることがあるので、ボタンの印を点で描く。'#' の位置を 1 ピクセルずつ塗る。 */
public final class PixelIcon {
    /** どの絵も一辺はこの大きさ。 */
    public static final int SIZE = 9;

    public static final String[] CLOCK = {
            "..#####..",
            ".#.....#.",
            "#...#...#",
            "#...#...#",
            "#...###.#",
            "#.......#",
            "#.......#",
            ".#.....#.",
            "..#####.."
    };

    public static final String[] GEAR = {
            "....#....",
            ".#.###.#.",
            "..#####..",
            ".##...##.",
            "###...###",
            ".##...##.",
            "..#####..",
            ".#.###.#.",
            "....#...."
    };

    /** 入り切りの四角。入のときは上に {@link #CHECK} を重ねる。 */
    public static final String[] CHECK_BOX = {
            "#########",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#.......#",
            "#########"
    };

    public static final String[] CHECK = {
            ".........",
            ".........",
            ".......#.",
            "......#..",
            ".#...#...",
            "..#.#....",
            "...#.....",
            ".........",
            "........."
    };

    private PixelIcon() {
    }

    public static void draw(GuiGraphics guiGraphics, String[] icon, int left, int top, int color) {
        for (int row = 0; row < icon.length; row++) {
            String line = icon[row];
            for (int column = 0; column < line.length(); column++) {
                if (line.charAt(column) == '#') {
                    guiGraphics.fill(left + column, top + row, left + column + 1, top + row + 1, color);
                }
            }
        }
    }
}
