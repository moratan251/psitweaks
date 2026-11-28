package com.moratan251.psitweaksqol.client.snippet;

/**
 * SNBT の文字の入れ子の深さを、読む前に確かめる。Minecraft の {@code TagParser} は入れ子を再帰で読むので、深すぎる入れ子は
 * {@link StackOverflowError} になる（バイナリの NBT のような深さの上限がない）。
 */
public final class SnbtNesting {
    /** 入れ子の深さの上限。バイナリの NBT を読むときの上限（{@code NbtAccounter}）と同じ。 */
    public static final int MAX_DEPTH = 512;

    private SnbtNesting() {
    }

    /**
     * {@code text} の {@code {}} と {@code []} の入れ子が {@code maxDepth} より深いか。引用符（{@code "} と {@code '}）の中の
     * 括弧は数えない。引用符のない文字列には括弧が入らない。
     */
    public static boolean deeperThan(String text, int maxDepth) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quote != 0) {
                if (c == '\\') {
                    i++;
                } else if (c == quote) {
                    quote = 0;
                }
                continue;
            }
            switch (c) {
                case '"', '\'' -> quote = c;
                case '{', '[' -> {
                    if (++depth > maxDepth) {
                        return true;
                    }
                }
                case '}', ']' -> depth--;
                default -> {
                }
            }
        }
        return false;
    }
}
