package com.moratan251.psitweaks.common.spells.util;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.mode.ListElementMode;
import com.moratan251.psitweaks.common.spells.param.ParamNumberListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.param.ParamStringListWrapper;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

public final class ModeListOperations {
    private ModeListOperations() {
    }

    public static SpellParam<?> createListParam(ListElementMode mode, String name, boolean canDisable) {
        return switch (mode) {
            case STRING -> new ParamStringListWrapper(name, PsitweaksSpellParams.STRING_LIST_COLOR, canDisable, false);
            case NUMBER -> new ParamNumberListWrapper(name, PsitweaksSpellParams.NUMBER_LIST_COLOR, canDisable, false);
            case VECTOR -> new ParamVectorListWrapper(name, PsitweaksSpellParams.VECTOR_LIST_COLOR, canDisable, false);
            case ENTITY -> new ParamEntityListWrapper(name, SpellParam.BLUE, canDisable, false);
            case ITEM -> new ParamSpellItemListWrapper(name, PsitweaksSpellParams.ITEM_LIST_COLOR, canDisable, false);
        };
    }

    public static SpellParam<?> createElementParam(ListElementMode mode, String name, boolean canDisable) {
        return switch (mode) {
            case STRING -> new ParamString(name, PsitweaksSpellParams.STRING_COLOR, canDisable, false);
            case NUMBER -> new ParamNumber(name, SpellParam.RED, canDisable, false);
            case VECTOR -> new ParamVector(name, SpellParam.GREEN, canDisable, false);
            case ENTITY -> new ParamEntity(name, SpellParam.GREEN, canDisable, false);
            case ITEM -> new ParamSpellItemValue(name, PsitweaksSpellParams.ITEM_COLOR, canDisable, false);
        };
    }

    public static Object emptyList(ListElementMode mode) {
        return switch (mode) {
            case STRING -> StringListWrapper.EMPTY;
            case NUMBER -> NumberListWrapper.EMPTY;
            case VECTOR -> VectorListWrapper.EMPTY;
            case ENTITY -> EntityListWrapper.EMPTY;
            case ITEM -> SpellItemListWrapper.EMPTY;
        };
    }

    public static Class<?> listType(ListElementMode mode) {
        return switch (mode) {
            case STRING -> StringListWrapper.class;
            case NUMBER -> NumberListWrapper.class;
            case VECTOR -> VectorListWrapper.class;
            case ENTITY -> EntityListWrapper.class;
            case ITEM -> SpellItemListWrapper.class;
        };
    }

    public static Object add(ListElementMode mode, Object source, List<?> elements) {
        return switch (mode) {
            case STRING -> addStrings((StringListWrapper) source, elements);
            case NUMBER -> addNumbers((NumberListWrapper) source, elements);
            case VECTOR -> addVectors((VectorListWrapper) source, elements);
            case ENTITY -> addEntities((EntityListWrapper) source, elements);
            case ITEM -> addItems((SpellItemListWrapper) source, elements);
        };
    }

    public static Object remove(ListElementMode mode, Object source, List<?> elements) {
        return switch (mode) {
            case STRING -> removeStrings((StringListWrapper) source, elements);
            case NUMBER -> removeNumbers((NumberListWrapper) source, elements);
            case VECTOR -> removeVectors((VectorListWrapper) source, elements);
            case ENTITY -> removeEntities((EntityListWrapper) source, elements);
            case ITEM -> removeItems((SpellItemListWrapper) source, elements);
        };
    }

    public static double size(ListElementMode mode, Object source) {
        return switch (mode) {
            case STRING -> ((StringListWrapper) source).size();
            case NUMBER -> ((NumberListWrapper) source).size();
            case VECTOR -> ((VectorListWrapper) source).size();
            case ENTITY -> ((EntityListWrapper) source).size();
            case ITEM -> ((SpellItemListWrapper) source).size();
        };
    }

    public static Object exclusion(ListElementMode mode, Object left, Object right) {
        return switch (mode) {
            case STRING -> excludeStrings((StringListWrapper) left, (StringListWrapper) right);
            case NUMBER -> excludeNumbers((NumberListWrapper) left, (NumberListWrapper) right);
            case VECTOR -> excludeVectors((VectorListWrapper) left, (VectorListWrapper) right);
            case ENTITY -> EntityListWrapper.exclusion((EntityListWrapper) left, (EntityListWrapper) right);
            case ITEM -> excludeItems((SpellItemListWrapper) left, (SpellItemListWrapper) right);
        };
    }

    public static Object intersection(ListElementMode mode, Object left, Object right) {
        return switch (mode) {
            case STRING -> intersectStrings((StringListWrapper) left, (StringListWrapper) right);
            case NUMBER -> intersectNumbers((NumberListWrapper) left, (NumberListWrapper) right);
            case VECTOR -> intersectVectors((VectorListWrapper) left, (VectorListWrapper) right);
            case ENTITY -> EntityListWrapper.intersection((EntityListWrapper) left, (EntityListWrapper) right);
            case ITEM -> intersectItems((SpellItemListWrapper) left, (SpellItemListWrapper) right);
        };
    }

    public static Object concatenation(ListElementMode mode, Object left, Object right) {
        return switch (mode) {
            case STRING -> concatenateStrings((StringListWrapper) left, (StringListWrapper) right);
            case NUMBER -> concatenateNumbers((NumberListWrapper) left, (NumberListWrapper) right);
            case VECTOR -> concatenateVectors((VectorListWrapper) left, (VectorListWrapper) right);
            case ENTITY -> EntityListWrapper.union((EntityListWrapper) left, (EntityListWrapper) right);
            case ITEM -> concatenateItems((SpellItemListWrapper) left, (SpellItemListWrapper) right);
        };
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

    private static boolean removeEquivalentItem(List<SpellItemValue> values, SpellItemValue target) {
        for (int i = 0; i < values.size(); i++) {
            if (isEquivalentItem(values.get(i), target)) {
                values.remove(i);
                return true;
            }
        }
        return false;
    }

    private static boolean containsEquivalentItem(List<SpellItemValue> values, SpellItemValue target) {
        for (SpellItemValue value : values) {
            if (isEquivalentItem(value, target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEquivalentItem(SpellItemValue left, SpellItemValue right) {
        if (left.source().isPresent() || right.source().isPresent()) {
            return left.source().equals(right.source());
        }
        return ItemStack.matches(left.copyStack(), right.copyStack());
    }
}
