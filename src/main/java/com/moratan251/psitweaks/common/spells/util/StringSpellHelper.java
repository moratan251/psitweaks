package com.moratan251.psitweaks.common.spells.util;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vazkii.psi.api.internal.Vector3;

public final class StringSpellHelper {
    public static final int MAX_STRING_LENGTH = 8192;
    public static final int MAX_CONSTANT_STRING_LENGTH = 1024;
    public static final int MAX_CAD_STORED_STRING_LENGTH = 128;
    private static final Pattern VECTOR_PATTERN = Pattern.compile(
            "^\\s*(?:(?i:vector)\\s*)?(\\[|\\()\\s*(.*?)\\s*(\\]|\\))\\s*$");

    private StringSpellHelper() {
    }

    public static String sanitize(String input) {
        return sanitize(input, MAX_STRING_LENGTH);
    }

    public static String sanitize(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        if (maxLength <= 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(Math.min(input.length(), maxLength));
        for (int i = 0; i < input.length() && builder.length() < maxLength; i++) {
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
        return clamp(input, MAX_STRING_LENGTH);
    }

    public static String clamp(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        if (maxLength <= 0) {
            return "";
        }
        return input.length() <= maxLength ? input : input.substring(0, maxLength);
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

    public static Optional<Vector3> parseVector(String input) {
        if (input == null) {
            return Optional.empty();
        }

        Matcher matcher = VECTOR_PATTERN.matcher(input);
        if (!matcher.matches() || !bracketsMatch(matcher.group(1), matcher.group(3))) {
            return Optional.empty();
        }

        String[] parts = matcher.group(2).split(",", -1);
        if (parts.length != 3) {
            return Optional.empty();
        }

        OptionalDouble x = parseFiniteDouble(parts[0].trim());
        OptionalDouble y = parseFiniteDouble(parts[1].trim());
        OptionalDouble z = parseFiniteDouble(parts[2].trim());
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Vector3(x.getAsDouble(), y.getAsDouble(), z.getAsDouble()));
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

    private static boolean bracketsMatch(String open, String close) {
        return ("[".equals(open) && "]".equals(close)) || ("(".equals(open) && ")".equals(close));
    }
}
