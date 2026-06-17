package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import java.util.List;
import java.util.StringJoiner;

public final class ModeStringConversionHelper {
    private ModeStringConversionHelper() {
    }

    public static String anyToString(Object value) {
        if (value == null) {
            return "";
        }

        if (PsitweaksListAdapters.isListType(value.getClass())) {
            return joinStrings(PsitweaksListAdapters.listToStrings(value));
        }

        return PsitweaksListAdapters.findElementAdapter(value.getClass())
                .map(adapter -> adapter.elementToString(value))
                .map(StringSpellHelper::sanitize)
                .orElseGet(() -> debugString(value));
    }

    public static StringListWrapper anyToStringList(Object value) {
        if (value == null) {
            return StringListWrapper.EMPTY;
        }

        if (PsitweaksListAdapters.isListType(value.getClass())) {
            return StringListWrapper.make(PsitweaksListAdapters.listToStrings(value));
        }

        return StringListWrapper.make(List.of(anyToString(value)));
    }

    private static String debugString(Object value) {
        return StringSpellHelper.sanitize(String.valueOf(value));
    }

    private static String joinStrings(List<String> values) {
        StringJoiner joiner = new StringJoiner(",");
        for (String value : values) {
            joiner.add(value == null ? "" : value);
        }
        return StringSpellHelper.sanitize(joiner.toString());
    }
}
