package com.moratan251.psitweaks.common.spells.util;

import java.math.BigDecimal;
import java.util.OptionalDouble;

public final class StringSpellHelper {
    public static final int MAX_STRING_LENGTH = 8192;

    private StringSpellHelper() {
    }

    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder(Math.min(input.length(), MAX_STRING_LENGTH));
        for (int i = 0; i < input.length() && builder.length() < MAX_STRING_LENGTH; i++) {
            char c = input.charAt(i);
            if (c == '\n') {
                builder.append('\n');
            } else if (c == '\r') {
                if (i + 1 >= input.length() || input.charAt(i + 1) != '\n') {
                    builder.append('\n');
                }
            } else if (c == '\t') {
                builder.append(' ');
            } else if (!Character.isISOControl(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static String clamp(String input) {
        if (input == null) {
            return "";
        }
        return input.length() <= MAX_STRING_LENGTH ? input : input.substring(0, MAX_STRING_LENGTH);
    }

    public static double bool(boolean value) {
        return value ? 1D : 0D;
    }

    public static OptionalDouble parseFiniteDouble(String input) {
        if (input == null) {
            return OptionalDouble.empty();
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return OptionalDouble.empty();
        }

        try {
            double value = Double.parseDouble(trimmed);
            return Double.isFinite(value) ? OptionalDouble.of(value) : OptionalDouble.empty();
        } catch (NumberFormatException ignored) {
            return OptionalDouble.empty();
        }
    }

    public static String numberToString(Number number) {
        if (number == null) {
            return "";
        }

        double value = number.doubleValue();
        if (!Double.isFinite(value)) {
            return "";
        }
        if (value == 0D) {
            return "0";
        }

        String result;
        if (value == Math.rint(value) && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            result = Long.toString((long) value);
        } else {
            result = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        }

        return sanitize(result);
    }
}
