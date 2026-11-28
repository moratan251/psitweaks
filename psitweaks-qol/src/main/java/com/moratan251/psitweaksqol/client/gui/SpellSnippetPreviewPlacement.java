package com.moratan251.psitweaksqol.client.gui;

/**
 * スニペット一覧の横に出す絵の枠を、魔法構築機の GUI の右か左の空きに置く計算。
 * 座標と幅は GUI の座標系の値。
 */
final class SpellSnippetPreviewPlacement {
    /** GUI と枠の間の幅。 */
    static final int GAP = 4;
    /** 画面の端と枠の間の幅。 */
    static final int MARGIN = 2;
    /** 左右の空きがどちらも狭いときに枠へ与える幅。このときは GUI に重ねて置く。 */
    static final int MIN_WIDTH = 80;

    private SpellSnippetPreviewPlacement() {
    }

    /** 枠に使える最大の幅。右と左の空きの広い方を使う。どちらも狭ければ {@link #MIN_WIDTH}（画面の幅は超えない）。 */
    static int maxWidth(int screenWidth, int guiLeft, int guiWidth) {
        int room = Math.max(rightRoom(screenWidth, guiLeft, guiWidth), leftRoom(guiLeft));
        return Math.max(1, Math.min(screenWidth - MARGIN * 2, Math.max(MIN_WIDTH, room)));
    }

    /** 枠の左端。右に入れば右、だめなら左に置く。どちらにも入らなければ広い方へ寄せ、画面の中に押し込む。 */
    static int left(int screenWidth, int guiLeft, int guiWidth, int boxWidth) {
        int rightRoom = rightRoom(screenWidth, guiLeft, guiWidth);
        int leftRoom = leftRoom(guiLeft);
        int rightSide = guiLeft + guiWidth + GAP;
        int leftSide = guiLeft - GAP - boxWidth;
        if (boxWidth <= rightRoom) {
            return rightSide;
        }
        if (boxWidth <= leftRoom) {
            return leftSide;
        }
        int preferred = rightRoom >= leftRoom ? rightSide : leftSide;
        return Math.max(MARGIN, Math.min(preferred, screenWidth - MARGIN - boxWidth));
    }

    private static int rightRoom(int screenWidth, int guiLeft, int guiWidth) {
        return screenWidth - MARGIN - (guiLeft + guiWidth + GAP);
    }

    private static int leftRoom(int guiLeft) {
        return guiLeft - GAP - MARGIN;
    }
}
