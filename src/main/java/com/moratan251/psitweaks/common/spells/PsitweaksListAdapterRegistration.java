package com.moratan251.psitweaks.common.spells;

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
import com.moratan251.psitweaks.common.spells.util.ModeListOperations;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
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
                ModeListOperations::addStrings,
                ModeListOperations::removeStrings,
                ModeListOperations::excludeStrings,
                ModeListOperations::intersectStrings,
                ModeListOperations::concatenateStrings,
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
                ModeListOperations::addNumbers,
                ModeListOperations::removeNumbers,
                ModeListOperations::excludeNumbers,
                ModeListOperations::intersectNumbers,
                ModeListOperations::concatenateNumbers,
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
                ModeListOperations::addVectors,
                ModeListOperations::removeVectors,
                ModeListOperations::excludeVectors,
                ModeListOperations::intersectVectors,
                ModeListOperations::concatenateVectors,
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
                ModeListOperations::addEntities,
                ModeListOperations::removeEntities,
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
                ModeListOperations::addItems,
                ModeListOperations::removeItems,
                ModeListOperations::excludeItems,
                ModeListOperations::intersectItems,
                ModeListOperations::concatenateItems,
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
                ModeListOperations::addBlocks,
                ModeListOperations::removeBlocks,
                ModeListOperations::excludeBlocks,
                ModeListOperations::intersectBlocks,
                ModeListOperations::concatenateBlocks,
                (name, canDisable) -> new ParamBlockListWrapper(name, PsitweaksSpellParams.BLOCK_LIST_COLOR,
                        canDisable, false),
                (name, canDisable) -> new ParamBlockValue(name, PsitweaksSpellParams.BLOCK_COLOR, canDisable, false),
                BlockValueHelper::coerce
        ));
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
    ) implements com.moratan251.psitweaks.api.PsitweaksListAdapter<T> {
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
    }

    @FunctionalInterface
    private interface ElementCoercer {
        Object coerce(SpellContext context, Object value) throws SpellRuntimeException;
    }
}
