package com.moratan251.psitweaksqol.client.snippet;

/**
 * 名前を付けて保存したピースの並び。
 *
 * @param fileName 保存先のファイル名（拡張子つき）。まだ保存していなければ {@code null}。
 */
public record SpellSnippet(String name, SpellPieceLayout layout, String fileName) {
}
