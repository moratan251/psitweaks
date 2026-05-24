package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
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
