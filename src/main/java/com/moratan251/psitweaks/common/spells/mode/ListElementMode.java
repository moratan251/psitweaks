package com.moratan251.psitweaks.common.spells.mode;

import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksModeOptions;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum ListElementMode {
    STRING(PsitweaksModeOptions.STRING),
    NUMBER(PsitweaksModeOptions.NUMBER),
    VECTOR(PsitweaksModeOptions.VECTOR),
    ENTITY(PsitweaksModeOptions.ENTITY),
    ITEM(PsitweaksModeOptions.ITEM);

    private final PsitweaksModeOption option;

    ListElementMode(PsitweaksModeOption option) {
        this.option = option;
    }

    public String id() {
        return option.serializedId();
    }

    public String buttonLabel() {
        return option.buttonLabel();
    }

    public String elementTranslationKey() {
        return option.elementTranslationKey();
    }

    public String listTranslationKey() {
        return option.listTranslationKey();
    }

    public PsitweaksModeOption option() {
        return option;
    }

    public ListElementMode next() {
        ListElementMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static List<PsitweaksModeOption> options() {
        return Arrays.stream(values()).map(ListElementMode::option).toList();
    }

    public static ListElementMode byId(String id) {
        if (id == null || id.isBlank()) {
            return STRING;
        }

        String normalized = id.toLowerCase(Locale.ROOT);
        for (ListElementMode mode : values()) {
            if (mode.id().equals(normalized)) {
                return mode;
            }
        }
        return fromOption(PsitweaksModeOptions.byId(id).orElse(null), STRING);
    }

    public static ListElementMode fromOption(PsitweaksModeOption option) {
        return fromOption(option, null);
    }

    public static ListElementMode fromOption(PsitweaksModeOption option, ListElementMode fallback) {
        if (option == null) {
            return fallback;
        }

        for (ListElementMode mode : values()) {
            if (mode.option.id().equals(option.id())) {
                return mode;
            }
        }
        return fallback;
    }
}
