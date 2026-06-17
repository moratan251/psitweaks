package com.moratan251.psitweaks.common.spells.util;

import java.util.regex.Pattern;

public final class WildcardStringMatcher {
    private final Pattern pattern;
    private final boolean partial;

    private WildcardStringMatcher(Pattern pattern, boolean partial) {
        this.pattern = pattern;
        this.partial = partial;
    }

    public static WildcardStringMatcher compile(String wildcard) {
        return new WildcardStringMatcher(Pattern.compile("^" + toRegex(StringSpellHelper.sanitize(wildcard)) + "$"),
                false);
    }

    public static WildcardStringMatcher compilePartial(String wildcard) {
        return new WildcardStringMatcher(Pattern.compile(toRegex(StringSpellHelper.sanitize(wildcard))), true);
    }

    public boolean matches(String value) {
        return partial ? pattern.matcher(value == null ? "" : value).find()
                : pattern.matcher(value == null ? "" : value).matches();
    }

    private static String toRegex(String wildcard) {
        StringBuilder regex = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i);
            if (escaped) {
                if (c == '*' || c == '?' || c == '[' || c == ']' || c == '\\') {
                    appendLiteral(regex, c);
                } else {
                    appendLiteral(regex, '\\');
                    appendLiteral(regex, c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '*') {
                regex.append(".*");
            } else if (c == '?') {
                regex.append('.');
            } else if (c == '[') {
                i = appendCharacterClass(regex, wildcard, i);
            } else {
                appendLiteral(regex, c);
            }
        }
        if (escaped) {
            appendLiteral(regex, '\\');
        }
        return regex.toString();
    }

    private static void appendLiteral(StringBuilder regex, char c) {
        regex.append(Pattern.quote(String.valueOf(c)));
    }

    private static int appendCharacterClass(StringBuilder regex, String wildcard, int start) {
        StringBuilder choices = new StringBuilder();
        boolean escaped = false;
        for (int i = start + 1; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i);
            if (escaped) {
                choices.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == ']') {
                if (choices.isEmpty()) {
                    appendLiteral(regex, '[');
                    return start;
                }

                appendChoice(regex, choices);
                return i;
            } else {
                choices.append(c);
            }
        }

        appendLiteral(regex, '[');
        return start;
    }

    private static void appendChoice(StringBuilder regex, StringBuilder choices) {
        regex.append("(?:");
        for (int i = 0; i < choices.length(); i++) {
            if (i > 0) {
                regex.append('|');
            }
            appendLiteral(regex, choices.charAt(i));
        }
        regex.append(')');
    }
}
