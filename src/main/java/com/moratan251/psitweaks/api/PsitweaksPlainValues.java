package com.moratan251.psitweaks.api;

import com.moratan251.psitweaks.api.value.ContextualValue;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.PlainMemoryValue;
import com.moratan251.psitweaks.api.value.PlainValueType;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Registry and conversion helper for plain spell values that can be stored in
 * CAD memory without carrying world context.
 *
 * <p>Plain values are intentionally narrower than {@link ContextualValue}. The
 * built-in types are String, Number, and Vector; addons may register additional
 * {@link PlainValueType} instances when they need their own self-contained value
 * type to participate in storage, mode buttons, and string conversions.</p>
 */
public final class PsitweaksPlainValues {
    private static final String ERROR_TYPE_MISMATCH = "psitweaks.spellerror.plain_memory_type";
    private static final String ERROR_PARSE_FAILED = "psitweaks.spellerror.plain_memory_parse";

    private static final Map<ResourceLocation, PlainValueType<?>> TYPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, PlainValueType<?>> TYPES_BY_MODE = new LinkedHashMap<>();

    /**
     * Built-in String plain value type.
     */
    public static final PlainValueType<String> STRING = register(new PlainValueType<>(
            ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, "string"),
            String.class,
            PsitweaksModeOptions.STRING,
            StringSpellHelper::sanitize,
            value -> StringTag.valueOf(StringSpellHelper.sanitize(value)),
            tag -> tag instanceof StringTag stringTag
                    ? Optional.of(StringSpellHelper.sanitize(stringTag.getAsString()))
                    : Optional.empty(),
            StringSpellHelper::sanitize,
            value -> Optional.of(StringSpellHelper.sanitize(value))
    ));

    /**
     * Built-in Number plain value type. Values are copied and persisted as
     * finite doubles.
     */
    public static final PlainValueType<Number> NUMBER = register(new PlainValueType<>(
            ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, "number"),
            Number.class,
            PsitweaksModeOptions.NUMBER,
            value -> Double.valueOf(value.doubleValue()),
            value -> DoubleTag.valueOf(value.doubleValue()),
            tag -> tag instanceof NumericTag numericTag
                    ? Optional.of(Double.valueOf(numericTag.getAsDouble()))
                    : Optional.empty(),
            value -> StringSpellHelper.sanitize(String.valueOf(value)),
            value -> StringSpellHelper.parseFiniteDouble(value).isPresent()
                    ? Optional.of(Double.valueOf(StringSpellHelper.parseFiniteDouble(value).getAsDouble()))
                    : Optional.empty()
    ));

    /**
     * Built-in Vector plain value type. This also bridges to Psi's native CAD
     * vector memory through the CAD memory helper and ItemCAD mixin.
     */
    public static final PlainValueType<Vector3> VECTOR = register(new PlainValueType<>(
            ResourceLocation.fromNamespaceAndPath(PsitweaksModeOptions.BUILTIN_NAMESPACE, "vector"),
            Vector3.class,
            PsitweaksModeOptions.VECTOR,
            value -> new Vector3(value.x, value.y, value.z),
            PsitweaksPlainValues::serializeVector,
            PsitweaksPlainValues::deserializeVector,
            value -> StringSpellHelper.sanitize(String.valueOf(value)),
            StringSpellHelper::parseVector
    ));

    private PsitweaksPlainValues() {
    }

    /**
     * Registers a plain value type and makes its mode available to plain-value
     * selectors.
     *
     * @throws IllegalStateException if the type id or mode id is already used
     */
    public static synchronized <T> PlainValueType<T> register(PlainValueType<T> type) {
        Objects.requireNonNull(type, "type");
        if (TYPES.containsKey(type.id())) {
            throw new IllegalStateException("PsiTweaks plain value type is already registered for " + type.id());
        }
        if (TYPES_BY_MODE.containsKey(type.modeOption().id())) {
            throw new IllegalStateException("PsiTweaks plain value mode is already registered for "
                    + type.modeOption().id());
        }

        TYPES.put(type.id(), type);
        TYPES_BY_MODE.put(type.modeOption().id(), type);
        return type;
    }

    /**
     * Finds a registered type by the id persisted in CAD memory.
     */
    public static synchronized Optional<PlainValueType<?>> byId(ResourceLocation id) {
        return Optional.ofNullable(TYPES.get(id));
    }

    /**
     * Finds a registered type by the mode option selected in the spell
     * programmer UI.
     */
    public static synchronized Optional<PlainValueType<?>> byMode(PsitweaksModeOption mode) {
        if (mode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(TYPES_BY_MODE.get(mode.id()));
    }

    /**
     * Returns registered plain value types in registration order.
     */
    public static synchronized List<PlainValueType<?>> snapshot() {
        return List.copyOf(TYPES.values());
    }

    /**
     * Resolves the plain value type accepted by a spell output class.
     *
     * <p>Block values are accepted as their saved position vector. Other
     * contextual values that also extend {@link Vector3} are deliberately not
     * treated as plain vectors.</p>
     */
    public static Optional<PlainValueType<?>> findByClass(Class<?> type) {
        if (type == null) {
            return Optional.empty();
        }
        if (BlockValue.class.isAssignableFrom(type)) {
            return Optional.of(VECTOR);
        }

        for (PlainValueType<?> plainType : snapshot()) {
            if (isContextualVectorType(plainType, type)) {
                continue;
            }
            if (plainType.acceptsClass(type)) {
                return Optional.of(plainType);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves the plain value type for a runtime spell value.
     *
     * <p>Block values are accepted as their saved position vector. Other
     * contextual values are excluded so that contextual data is not stored as
     * ordinary vectors by accident.</p>
     */
    public static Optional<PlainValueType<?>> findByValue(Object value) {
        if (value == null) {
            return Optional.empty();
        }
        Object plainValue = normalizeForPlainValue(value);

        for (PlainValueType<?> plainType : snapshot()) {
            if (isContextualVectorValue(plainType, plainValue)) {
                continue;
            }
            if (plainType.acceptsValue(plainValue)) {
                return Optional.of(plainType);
            }
        }
        return Optional.empty();
    }

    /**
     * Wraps a runtime value in its registered plain memory representation.
     */
    public static PlainMemoryValue<?> memoryValue(Object value) throws SpellRuntimeException {
        Object plainValue = normalizeForPlainValue(value);
        PlainValueType<?> type = findByValue(plainValue)
                .orElseThrow(() -> new SpellRuntimeException(ERROR_TYPE_MISMATCH));
        return memoryValue(type, plainValue);
    }

    /**
     * Wraps a value with a known type. Use this only when the caller has already
     * selected the correct registered type.
     */
    public static <T> PlainMemoryValue<T> memoryValueUnchecked(PlainValueType<T> type, T value) {
        return new PlainMemoryValue<>(type, value);
    }

    /**
     * Converts a stored value to the requested type.
     *
     * <p>Same-type reads return a copied value. String can be parsed into other
     * registered types, and any registered type can stringify to String. Other
     * cross-type conversions, including Number to Vector and Vector to Number,
     * are rejected.</p>
     */
    public static Object convert(PlainMemoryValue<?> source, PlainValueType<?> target) throws SpellRuntimeException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        if (source.type().id().equals(target.id())) {
            return castSourceValue(source, target);
        }

        if (source.type().id().equals(STRING.id())) {
            return parseStringValue((String) source.copiedValue(), target);
        }

        if (target.id().equals(STRING.id())) {
            return source.stringValue();
        }

        throw new SpellRuntimeException(ERROR_TYPE_MISMATCH);
    }

    /**
     * Returns the empty value used when a caller needs a neutral value for a
     * plain type.
     */
    public static Object defaultValue(PlainValueType<?> type) throws SpellRuntimeException {
        if (type.id().equals(STRING.id())) {
            return "";
        }
        if (type.id().equals(NUMBER.id())) {
            return 0D;
        }
        if (type.id().equals(VECTOR.id())) {
            return Vector3.zero.copy();
        }
        throw new SpellRuntimeException(ERROR_TYPE_MISMATCH);
    }

    private static <T> PlainMemoryValue<T> memoryValue(PlainValueType<T> type, Object value)
            throws SpellRuntimeException {
        T castValue = type.cast(value).orElseThrow(() -> new SpellRuntimeException(ERROR_TYPE_MISMATCH));
        return new PlainMemoryValue<>(type, castValue);
    }

    private static <T> Object castSourceValue(PlainMemoryValue<?> source, PlainValueType<T> target)
            throws SpellRuntimeException {
        return target.cast(source.copiedValue())
                .orElseThrow(() -> new SpellRuntimeException(ERROR_TYPE_MISMATCH));
    }

    private static <T> Object parseStringValue(String value, PlainValueType<T> target) throws SpellRuntimeException {
        return target.parse(value).orElseThrow(() -> new SpellRuntimeException(ERROR_PARSE_FAILED));
    }

    private static CompoundTag serializeVector(Vector3 value) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", value.x);
        tag.putDouble("y", value.y);
        tag.putDouble("z", value.z);
        return tag;
    }

    private static Optional<Vector3> deserializeVector(net.minecraft.nbt.Tag tag) {
        if (!(tag instanceof CompoundTag compound)) {
            return Optional.empty();
        }
        return Optional.of(new Vector3(compound.getDouble("x"), compound.getDouble("y"), compound.getDouble("z")));
    }

    private static Object normalizeForPlainValue(Object value) {
        if (value instanceof BlockValue block) {
            return block.positionVector();
        }
        return value;
    }

    private static boolean isContextualVectorType(PlainValueType<?> plainType, Class<?> type) {
        return plainType.id().equals(VECTOR.id()) && ContextualValue.class.isAssignableFrom(type);
    }

    private static boolean isContextualVectorValue(PlainValueType<?> plainType, Object value) {
        return plainType.id().equals(VECTOR.id()) && value instanceof ContextualValue;
    }
}
