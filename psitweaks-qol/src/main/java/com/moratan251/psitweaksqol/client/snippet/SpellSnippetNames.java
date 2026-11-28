package com.moratan251.psitweaksqol.client.snippet;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** スニペットの名前の決まりと、名前から保存先のファイル名を作る規則。 */
public final class SpellSnippetNames {
    public static final int MAX_NAME_LENGTH = 32;
    public static final String EXTENSION = ".snbt";

    private static final Set<String> RESERVED_FILE_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

    private SpellSnippetNames() {
    }

    /** 前後の空白を除いた名前。空、長すぎる、制御文字を含む名前は受け付けない。 */
    public static Optional<String> validateName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String stripped = name.strip();
        if (stripped.isEmpty()
                || stripped.codePointCount(0, stripped.length()) > MAX_NAME_LENGTH
                || stripped.codePoints().anyMatch(Character::isISOControl)) {
            return Optional.empty();
        }
        return Optional.of(stripped);
    }

    /**
     * 名前から作るファイル名。Windows でファイル名に使えない文字は {@code _} に替え、末尾の点と空白を除き、
     * 予約された名前（CON など）には先頭に {@code _} を付ける。
     */
    public static String fileNameFor(String name) {
        StringBuilder builder = new StringBuilder();
        name.strip().codePoints().forEach(codePoint -> {
            if (codePoint < 0x20 || "\\/:*?\"<>|".indexOf(codePoint) >= 0) {
                builder.append('_');
            } else {
                builder.appendCodePoint(codePoint);
            }
        });

        int end = builder.length();
        while (end > 0 && (builder.charAt(end - 1) == '.' || builder.charAt(end - 1) == ' ')) {
            end--;
        }
        String base = builder.substring(0, end);
        if (base.isEmpty()) {
            base = "_";
        }

        int dot = base.indexOf('.');
        String stem = (dot < 0 ? base : base.substring(0, dot)).toUpperCase(Locale.ROOT);
        if (RESERVED_FILE_NAMES.contains(stem)) {
            base = "_" + base;
        }
        return base + EXTENSION;
    }

    /**
     * 名前の {@code index} 番目（1 から）の保存先の候補。1 番目は {@link #fileNameFor(String)}、2 番目からは拡張子の前に
     * {@code ~2}・{@code ~3}… を付ける。「a:b」と「a_b」のように同じファイル名になる名前（Windows・macOS では大文字と小文字
     * だけ違う名前も）は、空いている次の候補に保存する。
     */
    public static String fileNameFor(String name, int index) {
        String fileName = fileNameFor(name);
        if (index <= 1) {
            return fileName;
        }
        return fileName.substring(0, fileName.length() - EXTENSION.length()) + "~" + index + EXTENSION;
    }

    /** 中身に名前がないファイルの表示名。拡張子を除いたファイル名を、長さの上限で切る。 */
    public static String nameFromFileName(String fileName) {
        String stem = fileName.endsWith(EXTENSION)
                ? fileName.substring(0, fileName.length() - EXTENSION.length())
                : fileName;
        int limit = stem.offsetByCodePoints(0, Math.min(MAX_NAME_LENGTH, stem.codePointCount(0, stem.length())));
        String name = stem.substring(0, limit).strip();
        return name.isEmpty() ? "_" : name;
    }
}
