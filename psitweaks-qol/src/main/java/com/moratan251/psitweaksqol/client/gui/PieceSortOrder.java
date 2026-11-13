package com.moratan251.psitweaksqol.client.gui;

import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.SpellPiece;

import java.util.Locale;

/**
 * ピース一覧の並び順。検索語があるときは Psi の当たりの強い順が先で、ここの順は同点のときに使われる。
 * 設定ファイルには名前で保存するので、あとから種類を足せる（登録順は Psitweaks のグループを整理したあとに足す予定）。
 */
public enum PieceSortOrder {
    /** Psi の元の並び。いまの言語での名前の文字コード順。 */
    NAME,
    /** 言語設定に関係なく英語名の順。大文字と小文字は区別しない。 */
    ENGLISH_NAME;

    /**
     * 並べるときに比べる名前を決める。
     *
     * @param piece       並べるピース
     * @param psiSortName Psi が決めた並び用の名前（{@code SpellPiece.getSortingName()}）
     */
    public String sortName(SpellPiece piece, String psiSortName) {
        if (this != ENGLISH_NAME) {
            return psiSortName;
        }

        // コネクタとエラー抑止は、Psi が並び用の名前を「0000…」に固定して先頭に置いている。それは変えない。
        String localizedName = Component.translatable(piece.getUnlocalizedName()).getString();
        if (!psiSortName.equals(localizedName)) {
            return psiSortName;
        }

        String translationKey = piece.getUnlocalizedName();
        String englishName = EnglishTranslations.get(translationKey);
        // en_us に訳がないピースは、いまの言語の名前のまま並べる。
        return englishName.equals(translationKey) ? psiSortName : englishName.toLowerCase(Locale.ROOT);
    }
}
