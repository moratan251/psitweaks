package com.moratan251.psitweaks.api.value;

import com.moratan251.psitweaks.api.PsitweaksPlainValues;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/**
 * Immutable typed value stored in Psitweaks CAD memory.
 *
 * <p>The record stores both the registered type id and a copied value. This
 * allows one memory slot to hold different plain types over time while still
 * preserving enough information to validate and convert the stored value when
 * read later.</p>
 */
public record PlainMemoryValue<T>(PlainValueType<T> type, T value) {
    private static final String TAG_TYPE = "type";
    private static final String TAG_VALUE = "value";

    public PlainMemoryValue {
        Objects.requireNonNull(type, "type");
        value = type.copy(Objects.requireNonNull(value, "value"));
    }

    /**
     * Returns a defensive copy of the stored value.
     */
    public Object copiedValue() {
        return type.copy(value);
    }

    /**
     * Returns the stored value in the type's spell string form.
     */
    public String stringValue() {
        return type.stringify(value);
    }

    /**
     * Serializes the memory value as a compound containing its type id and
     * type-specific value payload.
     */
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_TYPE, type.id().toString());
        tag.put(TAG_VALUE, type.serialize(value));
        return tag;
    }

    /**
     * Restores a memory value if both the persisted type and value payload are
     * valid for a currently registered plain value type.
     */
    public static Optional<PlainMemoryValue<?>> deserialize(CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_TYPE) || !tag.contains(TAG_VALUE)) {
            return Optional.empty();
        }

        ResourceLocation typeId = ResourceLocation.tryParse(tag.getString(TAG_TYPE));
        if (typeId == null) {
            return Optional.empty();
        }

        return PsitweaksPlainValues.byId(typeId)
                .flatMap(type -> deserializeValue(type, tag.get(TAG_VALUE)));
    }

    private static <T> Optional<PlainMemoryValue<?>> deserializeValue(PlainValueType<T> type, Tag valueTag) {
        return type.deserialize(valueTag).map(value -> new PlainMemoryValue<>(type, value));
    }
}
