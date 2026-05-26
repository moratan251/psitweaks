package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vazkii.psi.api.internal.Vector3;

public final class ModeStringParsingHelper {
    private static final Pattern VECTOR_PATTERN = Pattern.compile(
            "^\\s*(?:(?i:vector)\\s*)?(\\[|\\()\\s*(.*?)\\s*(\\]|\\))\\s*$");

    private ModeStringParsingHelper() {
    }

    public static Object elementFromString(ListElementMode mode, String value) {
        return switch (mode) {
            case NUMBER -> StringSpellHelper.parseFiniteDouble(value).orElse(0D);
            case VECTOR -> parseVector(value).orElseGet(() -> Vector3.zero.copy());
            case STRING, ENTITY, ITEM, BLOCK -> value;
        };
    }

    public static Object listFromStringList(ListElementMode mode, StringListWrapper source) {
        return switch (mode) {
            case NUMBER -> numbersFromStringList(source);
            case VECTOR -> vectorsFromStringList(source);
            case STRING, ENTITY, ITEM, BLOCK -> source;
        };
    }

    private static NumberListWrapper numbersFromStringList(StringListWrapper source) {
        List<Double> result = new ArrayList<>();
        for (String value : source) {
            result.add(StringSpellHelper.parseFiniteDouble(value).orElse(0D));
        }
        return NumberListWrapper.make(result);
    }

    private static VectorListWrapper vectorsFromStringList(StringListWrapper source) {
        List<Vector3> result = new ArrayList<>();
        for (String value : source) {
            parseVector(value).ifPresent(result::add);
        }
        return VectorListWrapper.make(result);
    }

    private static Optional<Vector3> parseVector(String input) {
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

        OptionalDouble x = StringSpellHelper.parseFiniteDouble(parts[0].trim());
        OptionalDouble y = StringSpellHelper.parseFiniteDouble(parts[1].trim());
        OptionalDouble z = StringSpellHelper.parseFiniteDouble(parts[2].trim());
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Vector3(x.getAsDouble(), y.getAsDouble(), z.getAsDouble()));
    }

    private static boolean bracketsMatch(String open, String close) {
        return ("[".equals(open) && "]".equals(close)) || ("(".equals(open) && ")".equals(close));
    }
}
