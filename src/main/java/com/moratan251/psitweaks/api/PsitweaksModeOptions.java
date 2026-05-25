package com.moratan251.psitweaks.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry for mode buttons displayed by mode-configurable spell pieces.
 */
public final class PsitweaksModeOptions {
    public static final String BUILTIN_NAMESPACE = "psitweaks";

    private static final Map<ResourceLocation, RegisteredOption> OPTIONS = new LinkedHashMap<>();
    private static final Map<String, ResourceLocation> SERIALIZED_IDS = new LinkedHashMap<>();
    private static int nextRegistrationIndex;

    public static final PsitweaksModeOption STRING = registerBuiltin(
            "string", "S", "psitweaks.datatype.string", "psitweaks.datatype.string_list", 0);
    public static final PsitweaksModeOption NUMBER = registerBuiltin(
            "number", "N", "psi.datatype.number", "psitweaks.datatype.number_list", 10);
    public static final PsitweaksModeOption VECTOR = registerBuiltin(
            "vector", "V", "psi.datatype.vector3", "psitweaks.datatype.vector_list", 20);
    public static final PsitweaksModeOption ENTITY = registerBuiltin(
            "entity", "E", "psi.datatype.entity", "psi.datatype.entity_list_wrapper", 30);
    public static final PsitweaksModeOption ITEM = registerBuiltin(
            "item", "I", "psitweaks.datatype.item", "psitweaks.datatype.item_list", 40);

    private PsitweaksModeOptions() {
    }

    public static synchronized PsitweaksModeOption register(PsitweaksModeOption option) {
        Objects.requireNonNull(option, "option");
        if (OPTIONS.containsKey(option.id())) {
            throw new IllegalStateException("PsiTweaks mode option is already registered for " + option.id());
        }
        if (SERIALIZED_IDS.containsKey(option.serializedId())) {
            throw new IllegalStateException("PsiTweaks mode option serialized id is already registered for "
                    + option.serializedId());
        }

        OPTIONS.put(option.id(), new RegisteredOption(option, nextRegistrationIndex++));
        SERIALIZED_IDS.put(option.serializedId(), option.id());
        return option;
    }

    public static synchronized Optional<PsitweaksModeOption> byId(ResourceLocation id) {
        Objects.requireNonNull(id, "id");
        RegisteredOption option = OPTIONS.get(id);
        return option == null ? Optional.empty() : Optional.of(option.option());
    }

    public static synchronized Optional<PsitweaksModeOption> byId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        ResourceLocation serializedMatch = SERIALIZED_IDS.get(id);
        if (serializedMatch != null) {
            return byId(serializedMatch);
        }

        ResourceLocation parsed = ResourceLocation.tryParse(id);
        return parsed == null ? Optional.empty() : byId(parsed);
    }

    public static synchronized List<PsitweaksModeOption> snapshot() {
        List<RegisteredOption> values = new ArrayList<>(OPTIONS.values());
        values.sort(Comparator
                .comparingInt((RegisteredOption option) -> option.option().sortOrder())
                .thenComparingInt(RegisteredOption::registrationIndex)
                .thenComparing(option -> option.option().id().toString()));

        List<PsitweaksModeOption> result = new ArrayList<>(values.size());
        for (RegisteredOption value : values) {
            result.add(value.option());
        }
        return List.copyOf(result);
    }

    private static PsitweaksModeOption registerBuiltin(
            String id,
            String buttonLabel,
            String elementTranslationKey,
            String listTranslationKey,
            int sortOrder
    ) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(BUILTIN_NAMESPACE, id);
        return register(new PsitweaksModeOption(location, id, buttonLabel, elementTranslationKey, listTranslationKey,
                sortOrder));
    }

    private record RegisteredOption(PsitweaksModeOption option, int registrationIndex) {
    }
}
