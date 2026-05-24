package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public final class ModeStringConversionHelper {
    private ModeStringConversionHelper() {
    }

    public static String elementToString(ListElementMode mode, Object value) {
        if (value == null) {
            return "";
        }

        return switch (mode) {
            case ENTITY -> entityToRegistryId((Entity) value);
            case ITEM -> itemToRegistryId((SpellItemValue) value);
            case VECTOR, NUMBER, STRING -> debugString(value);
        };
    }

    public static String anyToString(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String string) {
            return string;
        }
        if (value instanceof Entity entity) {
            return entityToRegistryId(entity);
        }
        if (value instanceof SpellItemValue item) {
            return itemToRegistryId(item);
        }
        if (value instanceof Vector3 vector) {
            return debugString(vector);
        }
        if (value instanceof Number number) {
            return debugString(number);
        }
        if (value instanceof EntityListWrapper list) {
            return joinConverted(list, ModeStringConversionHelper::entityToRegistryId);
        }
        if (value instanceof SpellItemListWrapper list) {
            return joinConverted(list, ModeStringConversionHelper::itemToRegistryId);
        }
        if (value instanceof VectorListWrapper list) {
            return joinConverted(list, vector -> debugString(vector));
        }
        if (value instanceof NumberListWrapper list) {
            return joinConverted(list, number -> debugString(number));
        }
        if (value instanceof StringListWrapper list) {
            return joinConverted(list, string -> string == null ? "" : string);
        }

        return debugString(value);
    }

    public static StringListWrapper listToStringList(ListElementMode mode, Object value) {
        if (value == null) {
            return StringListWrapper.EMPTY;
        }

        return switch (mode) {
            case ENTITY -> entitiesToStringList((EntityListWrapper) value);
            case ITEM -> itemsToStringList((SpellItemListWrapper) value);
            case VECTOR -> vectorsToStringList((VectorListWrapper) value);
            case NUMBER -> numbersToStringList((NumberListWrapper) value);
            case STRING -> (StringListWrapper) value;
        };
    }

    public static StringListWrapper anyToStringList(Object value) {
        if (value == null) {
            return StringListWrapper.EMPTY;
        }

        if (value instanceof EntityListWrapper list) {
            return entitiesToStringList(list);
        }
        if (value instanceof SpellItemListWrapper list) {
            return itemsToStringList(list);
        }
        if (value instanceof VectorListWrapper list) {
            return vectorsToStringList(list);
        }
        if (value instanceof NumberListWrapper list) {
            return numbersToStringList(list);
        }
        if (value instanceof StringListWrapper list) {
            return list;
        }

        return StringListWrapper.make(List.of(anyToString(value)));
    }

    private static String entityToRegistryId(Entity entity) {
        if (entity == null) {
            return "";
        }
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    private static String itemToRegistryId(SpellItemValue item) {
        if (item == null) {
            return "";
        }
        return BuiltInRegistries.ITEM.getKey(item.copyStack().getItem()).toString();
    }

    private static String debugString(Object value) {
        return StringSpellHelper.sanitize(String.valueOf(value));
    }

    private static <T> String joinConverted(Iterable<T> source, Function<T, String> converter) {
        if (source == null) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(",");
        for (T value : source) {
            joiner.add(converter.apply(value));
        }
        return StringSpellHelper.sanitize(joiner.toString());
    }

    private static StringListWrapper entitiesToStringList(EntityListWrapper source) {
        List<String> result = new ArrayList<>();
        for (Entity entity : source) {
            result.add(entityToRegistryId(entity));
        }
        return StringListWrapper.make(result);
    }

    private static StringListWrapper itemsToStringList(SpellItemListWrapper source) {
        List<String> result = new ArrayList<>();
        for (SpellItemValue item : source) {
            result.add(itemToRegistryId(item));
        }
        return StringListWrapper.make(result);
    }

    private static StringListWrapper vectorsToStringList(VectorListWrapper source) {
        List<String> result = new ArrayList<>();
        for (Vector3 vector : source) {
            result.add(debugString(vector));
        }
        return StringListWrapper.make(result);
    }

    private static StringListWrapper numbersToStringList(NumberListWrapper source) {
        List<String> result = new ArrayList<>();
        for (Number number : source) {
            result.add(debugString(number));
        }
        return StringListWrapper.make(result);
    }
}
