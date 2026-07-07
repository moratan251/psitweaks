package com.moratan251.psitweaks.api.value;

import com.moratan251.psitweaks.api.PsitweaksModeOption;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/**
 * Defines how one plain spell value type is identified, copied, serialized, and
 * converted to or from String.
 *
 * <p>A plain value type is also tied to a mode option. The mode option controls
 * how mode-configurable spell pieces expose the type in the programmer UI.</p>
 *
 * @param <T> runtime Java type accepted by this plain value type
 */
public final class PlainValueType<T> {
    private final ResourceLocation id;
    private final Class<T> valueClass;
    private final PsitweaksModeOption modeOption;
    private final Function<T, T> copier;
    private final Function<T, Tag> serializer;
    private final Function<Tag, Optional<T>> deserializer;
    private final Function<T, String> stringifier;
    private final Function<String, Optional<T>> parser;

    /**
     * Creates a plain value type definition.
     *
     * @param id stable type id stored in CAD memory
     * @param valueClass runtime class accepted by this type
     * @param modeOption UI mode used when selecting this type
     * @param copier creates an independent copy for storage and outputs
     * @param serializer writes copied values to NBT
     * @param deserializer restores values from NBT
     * @param stringifier converts values to the same string form used by spell
     *                    string conversion
     * @param parser parses values from String, returning empty when parsing
     *               fails
     */
    public PlainValueType(
            ResourceLocation id,
            Class<T> valueClass,
            PsitweaksModeOption modeOption,
            Function<T, T> copier,
            Function<T, Tag> serializer,
            Function<Tag, Optional<T>> deserializer,
            Function<T, String> stringifier,
            Function<String, Optional<T>> parser
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
        this.modeOption = Objects.requireNonNull(modeOption, "modeOption");
        this.copier = Objects.requireNonNull(copier, "copier");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.deserializer = Objects.requireNonNull(deserializer, "deserializer");
        this.stringifier = Objects.requireNonNull(stringifier, "stringifier");
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    /**
     * Stable id used for registration and persistence.
     */
    public ResourceLocation id() {
        return id;
    }

    /**
     * Runtime class accepted by this type.
     */
    public Class<T> valueClass() {
        return valueClass;
    }

    /**
     * Mode option shown by mode-configurable plain-value spell pieces.
     */
    public PsitweaksModeOption modeOption() {
        return modeOption;
    }

    /**
     * Returns whether values of the given class can be accepted by this type.
     */
    public boolean acceptsClass(Class<?> type) {
        return type != null && valueClass.isAssignableFrom(type);
    }

    /**
     * Returns whether the runtime value can be accepted by this type.
     */
    public boolean acceptsValue(Object value) {
        return value != null && valueClass.isInstance(value);
    }

    /**
     * Casts and copies a runtime value if it belongs to this type.
     */
    public Optional<T> cast(Object value) {
        if (!acceptsValue(value)) {
            return Optional.empty();
        }
        return Optional.of(copy(valueClass.cast(value)));
    }

    /**
     * Returns an independent copy of the value.
     */
    public T copy(T value) {
        return copier.apply(Objects.requireNonNull(value, "value"));
    }

    /**
     * Serializes a copied value into NBT.
     */
    public Tag serialize(T value) {
        return Objects.requireNonNull(serializer.apply(copy(value)), "serialized value");
    }

    /**
     * Restores and copies a value from NBT.
     */
    public Optional<T> deserialize(Tag tag) {
        if (tag == null) {
            return Optional.empty();
        }
        return deserializer.apply(tag).map(this::copy);
    }

    /**
     * Converts the value to its spell string form.
     */
    public String stringify(T value) {
        return Objects.requireNonNull(stringifier.apply(copy(value)), "string value");
    }

    /**
     * Parses the value from spell string form.
     */
    public Optional<T> parse(String value) {
        return parser.apply(value).map(this::copy);
    }
}
