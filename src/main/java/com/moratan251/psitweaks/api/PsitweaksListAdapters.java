package com.moratan251.psitweaks.api;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import vazkii.psi.api.spell.SpellParam;

/**
 * Registry for list-like spell value adapters.
 *
 * <p>Exact class registrations take priority. If no exact registration exists,
 * the registry picks the most specific assignable adapter. Ambiguous assignable
 * matches are treated as errors so spell compilation can reject the input type.
 */
public final class PsitweaksListAdapters {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Class<?>, PsitweaksListAdapter<?>> ADAPTERS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Class<?>> MODE_LIST_TYPES = new LinkedHashMap<>();
    private static final List<String> WARNED_AMBIGUITIES = new ArrayList<>();

    static {
        register(PsitweaksListLike.class, PsitweaksListLike::size);
    }

    private PsitweaksListAdapters() {
    }

    public static synchronized <T> void register(Class<T> listType, PsitweaksListAdapter<T> adapter) {
        Objects.requireNonNull(listType, "listType");
        Objects.requireNonNull(adapter, "adapter");
        if (ADAPTERS.containsKey(listType)) {
            throw new IllegalStateException("PsiTweaks list adapter is already registered for " + listType.getName());
        }

        ADAPTERS.put(listType, adapter);
        registerModeAdapter(listType, adapter);
    }

    public static synchronized boolean isListType(Class<?> listType) {
        try {
            return findAdapter(listType).isPresent();
        } catch (IllegalStateException exception) {
            warnAmbiguousOnce(listType, exception);
            return false;
        }
    }

    public static synchronized Optional<PsitweaksListAdapter<Object>> findAdapter(Class<?> listType) {
        Objects.requireNonNull(listType, "listType");
        PsitweaksListAdapter<?> exact = ADAPTERS.get(listType);
        if (exact != null) {
            return Optional.of(cast(exact));
        }

        List<ResolvedAdapter> bestMatches = new ArrayList<>();
        for (Map.Entry<Class<?>, PsitweaksListAdapter<?>> entry : ADAPTERS.entrySet()) {
            Class<?> adapterType = entry.getKey();
            if (!adapterType.isAssignableFrom(listType)) {
                continue;
            }

            ResolvedAdapter candidate = new ResolvedAdapter(adapterType, entry.getValue());
            addMostSpecific(bestMatches, candidate);
        }

        if (bestMatches.isEmpty()) {
            return Optional.empty();
        }
        if (bestMatches.size() > 1) {
            throw ambiguous(listType, bestMatches);
        }
        return Optional.of(cast(bestMatches.get(0).adapter()));
    }

    public static synchronized Optional<PsitweaksListAdapter<Object>> findElementAdapter(Class<?> elementType) {
        Objects.requireNonNull(elementType, "elementType");

        List<ResolvedAdapter> bestMatches = new ArrayList<>();
        for (PsitweaksListAdapter<?> adapter : ADAPTERS.values()) {
            Class<?> adapterElementType = adapter.elementType();
            if (adapterElementType == null || adapterElementType == Object.class
                    || !adapterElementType.isAssignableFrom(elementType)) {
                continue;
            }

            addMostSpecific(bestMatches, new ResolvedAdapter(adapterElementType, adapter));
        }

        if (bestMatches.isEmpty()) {
            return Optional.empty();
        }
        if (bestMatches.size() > 1) {
            throw ambiguous(elementType, bestMatches);
        }
        return Optional.of(cast(bestMatches.get(0).adapter()));
    }

    public static int size(Object list) {
        Objects.requireNonNull(list, "list");
        PsitweaksListAdapter<Object> adapter = findAdapter(list.getClass()).orElseThrow(
                () -> new IllegalStateException("No PsiTweaks list adapter registered for " + list.getClass().getName())
        );
        return adapter.size(list);
    }

    public static synchronized List<PsitweaksModeOption> modeOptions() {
        List<PsitweaksModeOption> options = new ArrayList<>();
        for (ResourceLocation modeId : MODE_LIST_TYPES.keySet()) {
            PsitweaksModeOptions.byId(modeId).ifPresent(options::add);
        }
        options.sort(Comparator
                .comparingInt(PsitweaksModeOption::sortOrder)
                .thenComparing(option -> option.id().toString()));
        return List.copyOf(options);
    }

    public static synchronized List<PsitweaksModeOption> modeOptions(PsitweaksValueKind valueKind) {
        return PsitweaksModeOptions.filterByKind(modeOptions(), valueKind);
    }

    public static synchronized Optional<PsitweaksListAdapter<Object>> findModeAdapter(PsitweaksModeOption option) {
        if (option == null) {
            return Optional.empty();
        }

        Class<?> listType = MODE_LIST_TYPES.get(option.id());
        if (listType == null) {
            return Optional.empty();
        }

        PsitweaksListAdapter<?> adapter = ADAPTERS.get(listType);
        return adapter == null ? Optional.empty() : Optional.of(cast(adapter));
    }

    public static synchronized Optional<Class<?>> listType(PsitweaksModeOption option) {
        if (option == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(MODE_LIST_TYPES.get(option.id()));
    }

    public static synchronized Optional<Class<?>> elementType(PsitweaksModeOption option) {
        return findModeAdapter(option).map(PsitweaksListAdapter::elementType);
    }

    public static SpellParam<?> createListParam(PsitweaksModeOption option, String name, boolean canDisable) {
        return findModeAdapter(option).orElseThrow(() -> noModeAdapter(option)).createListParam(name, canDisable);
    }

    public static SpellParam<?> createElementParam(PsitweaksModeOption option, String name, boolean canDisable) {
        return findModeAdapter(option).orElseThrow(() -> noModeAdapter(option)).createElementParam(name, canDisable);
    }

    public static Object emptyList(PsitweaksModeOption option) {
        return findModeAdapter(option).orElseThrow(() -> noModeAdapter(option)).emptyList();
    }

    public static Object emptyElement(PsitweaksModeOption option) {
        return findModeAdapter(option).orElseThrow(() -> noModeAdapter(option)).emptyElement();
    }

    public static Object elementFromString(PsitweaksModeOption option, String value) {
        return findModeAdapter(option).orElseThrow(() -> noModeAdapter(option)).elementFromString(value);
    }

    public static Object listFromStrings(PsitweaksModeOption option, Iterable<String> values) {
        return findModeAdapter(option).orElseThrow(() -> noModeAdapter(option)).listFromStrings(values);
    }

    public static List<String> listToStrings(Object list) {
        Objects.requireNonNull(list, "list");
        PsitweaksListAdapter<Object> adapter = findAdapter(list.getClass()).orElseThrow(
                () -> new IllegalStateException("No PsiTweaks list adapter registered for " + list.getClass().getName())
        );
        return adapter.listToStrings(list);
    }

    public static synchronized Map<Class<?>, PsitweaksListAdapter<?>> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(ADAPTERS));
    }

    private static <T> void registerModeAdapter(Class<T> listType, PsitweaksListAdapter<T> adapter) {
        PsitweaksModeOption option = adapter.modeOption();
        if (option == null) {
            return;
        }

        PsitweaksModeOption registeredOption = PsitweaksModeOptions.byId(option.id()).orElseGet(
                () -> PsitweaksModeOptions.register(option)
        );
        Class<?> previousType = MODE_LIST_TYPES.putIfAbsent(registeredOption.id(), listType);
        if (previousType != null) {
            throw new IllegalStateException("PsiTweaks list mode is already registered for "
                    + registeredOption.id() + " by " + previousType.getName());
        }
    }

    private static void addMostSpecific(List<ResolvedAdapter> bestMatches, ResolvedAdapter candidate) {
        boolean candidateShadowed = false;
        for (Iterator<ResolvedAdapter> iterator = bestMatches.iterator(); iterator.hasNext(); ) {
            ResolvedAdapter existing = iterator.next();
            if (existing.type().isAssignableFrom(candidate.type())) {
                iterator.remove();
            } else if (candidate.type().isAssignableFrom(existing.type())) {
                candidateShadowed = true;
                break;
            }
        }

        if (!candidateShadowed) {
            bestMatches.add(candidate);
        }
    }

    private static IllegalStateException ambiguous(Class<?> listType, List<ResolvedAdapter> matches) {
        StringBuilder message = new StringBuilder("Ambiguous PsiTweaks list adapters for ")
                .append(listType.getName())
                .append(": ");
        for (int i = 0; i < matches.size(); i++) {
            if (i > 0) {
                message.append(", ");
            }
            message.append(matches.get(i).type().getName());
        }
        return new IllegalStateException(message.toString());
    }

    private static IllegalStateException noModeAdapter(PsitweaksModeOption option) {
        return new IllegalStateException("No PsiTweaks list adapter registered for mode "
                + (option == null ? "<null>" : option.id()));
    }

    private static void warnAmbiguousOnce(Class<?> listType, IllegalStateException exception) {
        String typeName = listType == null ? "<null>" : listType.getName();
        if (WARNED_AMBIGUITIES.contains(typeName)) {
            return;
        }

        WARNED_AMBIGUITIES.add(typeName);
        LOGGER.warn(exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    private static PsitweaksListAdapter<Object> cast(PsitweaksListAdapter<?> adapter) {
        return (PsitweaksListAdapter<Object>) adapter;
    }

    private record ResolvedAdapter(Class<?> type, PsitweaksListAdapter<?> adapter) {
    }
}
