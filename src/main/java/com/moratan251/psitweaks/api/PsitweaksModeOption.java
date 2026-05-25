package com.moratan251.psitweaks.api;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Display option used by mode-configurable spell pieces.
 */
public record PsitweaksModeOption(
        ResourceLocation id,
        String serializedId,
        String buttonLabel,
        String elementTranslationKey,
        String listTranslationKey,
        int sortOrder,
        ResourceLocation overlayMaterialId
) {
    public PsitweaksModeOption(
            ResourceLocation id,
            String buttonLabel,
            String elementTranslationKey,
            String listTranslationKey,
            int sortOrder
    ) {
        this(id, id.toString(), buttonLabel, elementTranslationKey, listTranslationKey, sortOrder,
                defaultOverlayMaterialId(id));
    }

    public PsitweaksModeOption(
            ResourceLocation id,
            String buttonLabel,
            String elementTranslationKey,
            String listTranslationKey,
            int sortOrder,
            ResourceLocation overlayMaterialId
    ) {
        this(id, id.toString(), buttonLabel, elementTranslationKey, listTranslationKey, sortOrder, overlayMaterialId);
    }

    public PsitweaksModeOption(
            ResourceLocation id,
            String serializedId,
            String buttonLabel,
            String elementTranslationKey,
            String listTranslationKey,
            int sortOrder
    ) {
        this(id, serializedId, buttonLabel, elementTranslationKey, listTranslationKey, sortOrder,
                defaultOverlayMaterialId(id));
    }

    public PsitweaksModeOption {
        Objects.requireNonNull(id, "id");
        serializedId = requireText(serializedId, "serializedId");
        buttonLabel = requireText(buttonLabel, "buttonLabel");
        elementTranslationKey = requireText(elementTranslationKey, "elementTranslationKey");
        listTranslationKey = requireText(listTranslationKey, "listTranslationKey");
        overlayMaterialId = Objects.requireNonNull(overlayMaterialId, "overlayMaterialId");
    }

    private static ResourceLocation defaultOverlayMaterialId(ResourceLocation id) {
        Objects.requireNonNull(id, "id");
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "mode/" + id.getPath());
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
