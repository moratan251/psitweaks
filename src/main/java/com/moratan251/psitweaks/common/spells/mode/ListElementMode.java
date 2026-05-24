package com.moratan251.psitweaks.common.spells.mode;

import java.util.Locale;

public enum ListElementMode {
    STRING("string", "S", "psitweaks.datatype.string", "psitweaks.datatype.string_list"),
    NUMBER("number", "N", "psi.datatype.number", "psitweaks.datatype.number_list"),
    VECTOR("vector", "V", "psi.datatype.vector3", "psitweaks.datatype.vector_list"),
    ENTITY("entity", "E", "psi.datatype.entity", "psi.datatype.entity_list_wrapper"),
    ITEM("item", "I", "psitweaks.datatype.item", "psitweaks.datatype.item_list");

    private final String id;
    private final String buttonLabel;
    private final String elementTranslationKey;
    private final String listTranslationKey;

    ListElementMode(String id, String buttonLabel, String elementTranslationKey, String listTranslationKey) {
        this.id = id;
        this.buttonLabel = buttonLabel;
        this.elementTranslationKey = elementTranslationKey;
        this.listTranslationKey = listTranslationKey;
    }

    public String id() {
        return id;
    }

    public String buttonLabel() {
        return buttonLabel;
    }

    public String elementTranslationKey() {
        return elementTranslationKey;
    }

    public String listTranslationKey() {
        return listTranslationKey;
    }

    public ListElementMode next() {
        ListElementMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ListElementMode byId(String id) {
        if (id == null || id.isBlank()) {
            return STRING;
        }

        String normalized = id.toLowerCase(Locale.ROOT);
        for (ListElementMode mode : values()) {
            if (mode.id.equals(normalized)) {
                return mode;
            }
        }
        return STRING;
    }
}
