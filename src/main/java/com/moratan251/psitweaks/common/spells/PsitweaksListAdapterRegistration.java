package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.api.PsitweaksListAdapter;
import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.api.PsitweaksModeOption;
import com.moratan251.psitweaks.api.PsitweaksModeOptions;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamBlockListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamBlockValue;
import com.moratan251.psitweaks.common.spells.param.ParamNumberListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.param.ParamStringListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public final class PsitweaksListAdapterRegistration {
    private static boolean registered;

    private PsitweaksListAdapterRegistration() {
    }

    public static synchronized void registerBuiltins() {
        if (registered) {
            return;
        }

        registered = true;
        PsitweaksListAdapters.register(StringListWrapper.class, new BuiltinListAdapter<>(
                PsitweaksModeOptions.STRING,
                String.class,
                () -> StringListWrapper.EMPTY,
                () -> "",
                StringListWrapper::size,
                StringListWrapper::get,
                PsitweaksListAdapterRegistration::addStrings,
                PsitweaksListAdapterRegistration::removeStrings,
                PsitweaksListAdapterRegistration::excludeStrings,
                PsitweaksListAdapterRegistration::intersectStrings,
                PsitweaksListAdapterRegistration::concatenateStrings,
                (name, canDisable) -> new ParamStringListWrapper(name, PsitweaksSpellParams.STRING_LIST_COLOR,
                        canDisable, false),
                (name, canDisable) -> new ParamString(name, PsitweaksSpellParams.STRING_COLOR, canDisable, false)
        ));
        PsitweaksListAdapters.register(NumberListWrapper.class, new BuiltinListAdapter<>(
                PsitweaksModeOptions.NUMBER,
                Double.class,
                () -> NumberListWrapper.EMPTY,
                () -> 0.0D,
                NumberListWrapper::size,
                NumberListWrapper::get,
                PsitweaksListAdapterRegistration::addNumbers,
                PsitweaksListAdapterRegistration::removeNumbers,
                PsitweaksListAdapterRegistration::excludeNumbers,
                PsitweaksListAdapterRegistration::intersectNumbers,
                PsitweaksListAdapterRegistration::concatenateNumbers,
                (name, canDisable) -> new ParamNumberListWrapper(name, PsitweaksSpellParams.NUMBER_LIST_COLOR,
                        canDisable, false),
                (name, canDisable) -> new ParamNumber(name, SpellParam.RED, canDisable, false)
        ));
        PsitweaksListAdapters.register(VectorListWrapper.class, new BuiltinListAdapter<>(
                PsitweaksModeOptions.VECTOR,
                Vector3.class,
                () -> VectorListWrapper.EMPTY,
                () -> Vector3.zero.copy(),
                VectorListWrapper::size,
                VectorListWrapper::get,
                PsitweaksListAdapterRegistration::addVectors,
                PsitweaksListAdapterRegistration::removeVectors,
                PsitweaksListAdapterRegistration::excludeVectors,
                PsitweaksListAdapterRegistration::intersectVectors,
                PsitweaksListAdapterRegistration::concatenateVectors,
                (name, canDisable) -> new ParamVectorListWrapper(name, PsitweaksSpellParams.VECTOR_LIST_COLOR,
                        canDisable, false),
                (name, canDisable) -> new ParamVector(name, SpellParam.GREEN, canDisable, false)
        ));
        PsitweaksListAdapters.register(EntityListWrapper.class, new BuiltinListAdapter<>(
                PsitweaksModeOptions.ENTITY,
                Entity.class,
                () -> EntityListWrapper.EMPTY,
                () -> null,
                EntityListWrapper::size,
                EntityListWrapper::get,
                PsitweaksListAdapterRegistration::addEntities,
                PsitweaksListAdapterRegistration::removeEntities,
                EntityListWrapper::exclusion,
                EntityListWrapper::intersection,
                EntityListWrapper::union,
                (name, canDisable) -> new ParamEntityListWrapper(name, SpellParam.BLUE, canDisable, false),
                (name, canDisable) -> new ParamEntity(name, SpellParam.GREEN, canDisable, false)
        ));
        PsitweaksListAdapters.register(SpellItemListWrapper.class, new BuiltinListAdapter<>(
                PsitweaksModeOptions.ITEM,
                SpellItemValue.class,
                () -> SpellItemListWrapper.EMPTY,
                () -> SpellItemValue.EMPTY,
                SpellItemListWrapper::size,
                SpellItemListWrapper::get,
                PsitweaksListAdapterRegistration::addItems,
                PsitweaksListAdapterRegistration::removeItems,
                PsitweaksListAdapterRegistration::excludeItems,
                PsitweaksListAdapterRegistration::intersectItems,
                PsitweaksListAdapterRegistration::concatenateItems,
                (name, canDisable) -> new ParamSpellItemListWrapper(name, PsitweaksSpellParams.ITEM_LIST_COLOR,
                        canDisable, false),
                (name, canDisable) -> new ParamSpellItemValue(name, PsitweaksSpellParams.ITEM_COLOR, canDisable,
                        false)
        ));
        PsitweaksListAdapters.register(BlockListWrapper.class, new BuiltinListAdapter<>(
                PsitweaksModeOptions.BLOCK,
                BlockValue.class,
                () -> BlockListWrapper.EMPTY,
                () -> null,
                BlockListWrapper::size,
                BlockListWrapper::get,
                PsitweaksListAdapterRegistration::addBlocks,
                PsitweaksListAdapterRegistration::removeBlocks,
                PsitweaksListAdapterRegistration::excludeBlocks,
                PsitweaksListAdapterRegistration::intersectBlocks,
                PsitweaksListAdapterRegistration::concatenateBlocks,
                (name, canDisable) -> new ParamBlockListWrapper(name, PsitweaksSpellParams.BLOCK_LIST_COLOR,
                        canDisable, false),
                (name, canDisable) -> new ParamBlockValue(name, PsitweaksSpellParams.BLOCK_COLOR, canDisable, false),
                BlockValueHelper::coerce
        ));
    }

    private static StringListWrapper addStrings(StringListWrapper source, List<?> elements) {
        List<String> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof String value) {
                result.add(value);
            }
        }
        return StringListWrapper.make(result);
    }

    private static NumberListWrapper addNumbers(NumberListWrapper source, List<?> elements) {
        List<Double> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof Number value) {
                double number = value.doubleValue();
                if (Double.isFinite(number)) {
                    result.add(number);
                }
            }
        }
        return NumberListWrapper.make(result);
    }

    private static VectorListWrapper addVectors(VectorListWrapper source, List<?> elements) {
        List<Vector3> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof Vector3 value) {
                result.add(value.copy());
            }
        }
        return VectorListWrapper.make(result);
    }

    private static EntityListWrapper addEntities(EntityListWrapper source, List<?> elements) {
        EntityListWrapper result = source;
        for (Object element : elements) {
            if (element instanceof Entity value) {
                result = EntityListWrapper.withAdded(result, value);
            }
        }
        return result;
    }

    private static SpellItemListWrapper addItems(SpellItemListWrapper source, List<?> elements) {
        List<SpellItemValue> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof SpellItemValue value && !value.isEmpty()) {
                result.add(value);
            }
        }
        return SpellItemListWrapper.make(result);
    }

    private static BlockListWrapper addBlocks(BlockListWrapper source, List<?> elements) {
        List<BlockValue> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof BlockValue value) {
                result.add(value);
            }
        }
        return BlockListWrapper.make(result);
    }

    private static StringListWrapper removeStrings(StringListWrapper source, List<?> elements) {
        List<String> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof String value) {
                result.remove(StringSpellHelper.sanitize(value));
            }
        }
        return StringListWrapper.make(result);
    }

    private static NumberListWrapper removeNumbers(NumberListWrapper source, List<?> elements) {
        List<Double> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof Number value) {
                double number = value.doubleValue();
                if (Double.isFinite(number)) {
                    result.remove(number);
                }
            }
        }
        return NumberListWrapper.make(result);
    }

    private static VectorListWrapper removeVectors(VectorListWrapper source, List<?> elements) {
        List<Vector3> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof Vector3 value) {
                result.remove(value);
            }
        }
        return VectorListWrapper.make(result);
    }

    private static EntityListWrapper removeEntities(EntityListWrapper source, List<?> elements) {
        EntityListWrapper result = source;
        for (Object element : elements) {
            if (element instanceof Entity value) {
                result = EntityListWrapper.withRemoved(result, value);
            }
        }
        return result;
    }

    private static SpellItemListWrapper removeItems(SpellItemListWrapper source, List<?> elements) {
        List<SpellItemValue> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof SpellItemValue value) {
                removeEquivalentItem(result, value);
            }
        }
        return SpellItemListWrapper.make(result);
    }

    private static BlockListWrapper removeBlocks(BlockListWrapper source, List<?> elements) {
        List<BlockValue> result = new ArrayList<>(source.asList());
        for (Object element : elements) {
            if (element instanceof BlockValue value) {
                removeEquivalentBlock(result, value);
            }
        }
        return BlockListWrapper.make(result);
    }

    private static StringListWrapper excludeStrings(StringListWrapper left, StringListWrapper right) {
        List<String> result = new ArrayList<>();
        for (String value : left) {
            if (!right.asList().contains(value)) {
                result.add(value);
            }
        }
        return StringListWrapper.make(result);
    }

    private static NumberListWrapper excludeNumbers(NumberListWrapper left, NumberListWrapper right) {
        List<Double> result = new ArrayList<>();
        for (Double value : left) {
            if (!right.asList().contains(value)) {
                result.add(value);
            }
        }
        return NumberListWrapper.make(result);
    }

    private static VectorListWrapper excludeVectors(VectorListWrapper left, VectorListWrapper right) {
        List<Vector3> result = new ArrayList<>();
        List<Vector3> rightValues = right.asList();
        for (Vector3 value : left) {
            if (!rightValues.contains(value)) {
                result.add(value);
            }
        }
        return VectorListWrapper.make(result);
    }

    private static SpellItemListWrapper excludeItems(SpellItemListWrapper left, SpellItemListWrapper right) {
        List<SpellItemValue> result = new ArrayList<>();
        List<SpellItemValue> rightValues = right.asList();
        for (SpellItemValue value : left) {
            if (!containsEquivalentItem(rightValues, value)) {
                result.add(value);
            }
        }
        return SpellItemListWrapper.make(result);
    }

    private static BlockListWrapper excludeBlocks(BlockListWrapper left, BlockListWrapper right) {
        List<BlockValue> result = new ArrayList<>();
        List<BlockValue> rightValues = right.asList();
        for (BlockValue value : left) {
            if (!containsEquivalentBlock(rightValues, value)) {
                result.add(value);
            }
        }
        return BlockListWrapper.make(result);
    }

    private static StringListWrapper intersectStrings(StringListWrapper left, StringListWrapper right) {
        List<String> result = new ArrayList<>();
        for (String value : left) {
            if (right.asList().contains(value)) {
                result.add(value);
            }
        }
        return StringListWrapper.make(result);
    }

    private static NumberListWrapper intersectNumbers(NumberListWrapper left, NumberListWrapper right) {
        List<Double> result = new ArrayList<>();
        for (Double value : left) {
            if (right.asList().contains(value)) {
                result.add(value);
            }
        }
        return NumberListWrapper.make(result);
    }

    private static VectorListWrapper intersectVectors(VectorListWrapper left, VectorListWrapper right) {
        List<Vector3> result = new ArrayList<>();
        List<Vector3> rightValues = right.asList();
        for (Vector3 value : left) {
            if (rightValues.contains(value)) {
                result.add(value);
            }
        }
        return VectorListWrapper.make(result);
    }

    private static SpellItemListWrapper intersectItems(SpellItemListWrapper left, SpellItemListWrapper right) {
        List<SpellItemValue> result = new ArrayList<>();
        List<SpellItemValue> rightValues = right.asList();
        for (SpellItemValue value : left) {
            if (containsEquivalentItem(rightValues, value)) {
                result.add(value);
            }
        }
        return SpellItemListWrapper.make(result);
    }

    private static BlockListWrapper intersectBlocks(BlockListWrapper left, BlockListWrapper right) {
        List<BlockValue> result = new ArrayList<>();
        List<BlockValue> rightValues = right.asList();
        for (BlockValue value : left) {
            if (containsEquivalentBlock(rightValues, value)) {
                result.add(value);
            }
        }
        return BlockListWrapper.make(result);
    }

    private static StringListWrapper concatenateStrings(StringListWrapper left, StringListWrapper right) {
        List<String> result = new ArrayList<>(left.asList());
        result.addAll(right.asList());
        return StringListWrapper.make(result);
    }

    private static NumberListWrapper concatenateNumbers(NumberListWrapper left, NumberListWrapper right) {
        List<Double> result = new ArrayList<>(left.asList());
        result.addAll(right.asList());
        return NumberListWrapper.make(result);
    }

    private static VectorListWrapper concatenateVectors(VectorListWrapper left, VectorListWrapper right) {
        List<Vector3> result = new ArrayList<>(left.asList());
        result.addAll(right.asList());
        return VectorListWrapper.make(result);
    }

    private static SpellItemListWrapper concatenateItems(SpellItemListWrapper left, SpellItemListWrapper right) {
        List<SpellItemValue> result = new ArrayList<>(left.asList());
        result.addAll(right.asList());
        return SpellItemListWrapper.make(result);
    }

    private static BlockListWrapper concatenateBlocks(BlockListWrapper left, BlockListWrapper right) {
        List<BlockValue> result = new ArrayList<>(left.asList());
        result.addAll(right.asList());
        return BlockListWrapper.make(result);
    }

    private static boolean removeEquivalentItem(List<SpellItemValue> values, SpellItemValue target) {
        for (int i = 0; i < values.size(); i++) {
            if (SpellItemValue.equivalent(values.get(i), target)) {
                values.remove(i);
                return true;
            }
        }
        return false;
    }

    private static boolean containsEquivalentItem(List<SpellItemValue> values, SpellItemValue target) {
        for (SpellItemValue value : values) {
            if (SpellItemValue.equivalent(value, target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean removeEquivalentBlock(List<BlockValue> values, BlockValue target) {
        for (int i = 0; i < values.size(); i++) {
            if (BlockListWrapper.equivalent(values.get(i), target)) {
                values.remove(i);
                return true;
            }
        }
        return false;
    }

    private static boolean containsEquivalentBlock(List<BlockValue> values, BlockValue target) {
        for (BlockValue value : values) {
            if (BlockListWrapper.equivalent(value, target)) {
                return true;
            }
        }
        return false;
    }

    private static StringListWrapper stringsFromStrings(Iterable<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            result.add(StringSpellHelper.sanitize(value));
        }
        return StringListWrapper.make(result);
    }

    private static NumberListWrapper numbersFromStrings(Iterable<String> values) {
        List<Double> result = new ArrayList<>();
        for (String value : values) {
            result.add(StringSpellHelper.parseFiniteDouble(value).orElse(0D));
        }
        return NumberListWrapper.make(result);
    }

    private static VectorListWrapper vectorsFromStrings(Iterable<String> values) {
        List<Vector3> result = new ArrayList<>();
        for (String value : values) {
            StringSpellHelper.parseVector(value).ifPresent(result::add);
        }
        return VectorListWrapper.make(result);
    }

    private static String debugString(Object value) {
        return StringSpellHelper.sanitize(String.valueOf(value));
    }

    private record BuiltinListAdapter<T>(
            PsitweaksModeOption modeOption,
            Class<?> elementType,
            Supplier<?> emptyListSupplier,
            Supplier<?> emptyElementSupplier,
            ToIntFunction<T> size,
            BiFunction<T, Integer, Object> get,
            BiFunction<T, List<?>, Object> add,
            BiFunction<T, List<?>, Object> remove,
            BiFunction<T, T, Object> exclusion,
            BiFunction<T, T, Object> intersection,
            BiFunction<T, T, Object> concatenation,
            BiFunction<String, Boolean, SpellParam<?>> createListParam,
            BiFunction<String, Boolean, SpellParam<?>> createElementParam,
            ElementCoercer coerceElement
    ) implements PsitweaksListAdapter<T> {
        private BuiltinListAdapter(
                PsitweaksModeOption modeOption,
                Class<?> elementType,
                Supplier<?> emptyListSupplier,
                Supplier<?> emptyElementSupplier,
                ToIntFunction<T> size,
                BiFunction<T, Integer, Object> get,
                BiFunction<T, List<?>, Object> add,
                BiFunction<T, List<?>, Object> remove,
                BiFunction<T, T, Object> exclusion,
                BiFunction<T, T, Object> intersection,
                BiFunction<T, T, Object> concatenation,
                BiFunction<String, Boolean, SpellParam<?>> createListParam,
                BiFunction<String, Boolean, SpellParam<?>> createElementParam
        ) {
            this(modeOption, elementType, emptyListSupplier, emptyElementSupplier, size, get, add, remove, exclusion,
                    intersection, concatenation, createListParam, createElementParam, (context, value) -> value);
        }

        @Override
        public int size(T list) {
            return size.applyAsInt(list);
        }

        @Override
        public Object emptyList() {
            return emptyListSupplier.get();
        }

        @Override
        public Object emptyElement() {
            return emptyElementSupplier.get();
        }

        @Override
        public Object get(T list, int index) {
            return get.apply(list, index);
        }

        @Override
        public Object add(T list, List<?> elements) {
            return add.apply(list, elements);
        }

        @Override
        public Object remove(T list, List<?> elements) {
            return remove.apply(list, elements);
        }

        @Override
        public Object exclusion(T left, T right) {
            return exclusion.apply(left, right);
        }

        @Override
        public Object intersection(T left, T right) {
            return intersection.apply(left, right);
        }

        @Override
        public Object concatenation(T left, T right) {
            return concatenation.apply(left, right);
        }

        @Override
        public SpellParam<?> createListParam(String name, boolean canDisable) {
            return createListParam.apply(name, canDisable);
        }

        @Override
        public SpellParam<?> createElementParam(String name, boolean canDisable) {
            return createElementParam.apply(name, canDisable);
        }

        @Override
        public Object coerceElement(SpellContext context, Object value) throws SpellRuntimeException {
            return coerceElement.coerce(context, value);
        }

        @Override
        public Object elementFromString(String value) {
            if (modeOption.id().equals(PsitweaksModeOptions.STRING.id())) {
                return StringSpellHelper.sanitize(value);
            }
            if (modeOption.id().equals(PsitweaksModeOptions.NUMBER.id())) {
                return StringSpellHelper.parseFiniteDouble(value).orElse(0D);
            }
            if (modeOption.id().equals(PsitweaksModeOptions.VECTOR.id())) {
                return StringSpellHelper.parseVector(value).orElseGet(() -> Vector3.zero.copy());
            }
            return PsitweaksListAdapter.super.elementFromString(value);
        }

        @Override
        public Object listFromStrings(Iterable<String> values) {
            if (modeOption.id().equals(PsitweaksModeOptions.STRING.id())) {
                return stringsFromStrings(values);
            }
            if (modeOption.id().equals(PsitweaksModeOptions.NUMBER.id())) {
                return numbersFromStrings(values);
            }
            if (modeOption.id().equals(PsitweaksModeOptions.VECTOR.id())) {
                return vectorsFromStrings(values);
            }
            return PsitweaksListAdapter.super.listFromStrings(values);
        }

        @Override
        public String elementToString(Object value) {
            if (value == null) {
                return "";
            }
            return debugString(value);
        }
    }

    @FunctionalInterface
    private interface ElementCoercer {
        Object coerce(SpellContext context, Object value) throws SpellRuntimeException;
    }
}
