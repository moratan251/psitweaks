package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.util.ComparisonValueHelper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Keeps the first of each group of equal List elements (compared like Operator: Equal), in their order. Entity and
 * Block Lists are left out because they never hold duplicates.
 */
public class PieceOperatorListRemoveDuplicates extends PieceOperatorModeListBase {
    private SpellParam<?> list;

    public PieceOperatorListRemoveDuplicates(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean offersMode(PsitweaksModeOption option) {
        return !option.id().equals(PsitweaksModeOptions.ENTITY.id())
                && !option.id().equals(PsitweaksModeOptions.BLOCK.id());
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        int size = currentAdapter().size(source);
        List<Object> result = new ArrayList<>(size);
        // Elements that can be equal share a bucket key; the bucket is then checked with the full comparison.
        Map<Object, List<Object>> buckets = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Object value = currentAdapter().get(source, i);
            List<Object> bucket = buckets.computeIfAbsent(bucketKey(value), key -> new ArrayList<>(1));
            boolean seen = false;
            for (Object kept : bucket) {
                if (ComparisonValueHelper.equalsValue(kept, value)) {
                    seen = true;
                    break;
                }
            }
            if (!seen) {
                bucket.add(value);
                result.add(value);
            }
        }
        return currentAdapter().add(currentAdapter().emptyList(), result);
    }

    private static Object bucketKey(Object value) {
        if (value instanceof Vector3 vector) {
            // Vector3 compares with ==, so 0.0 and -0.0 must share a key.
            return List.of(vector.x + 0.0D, vector.y + 0.0D, vector.z + 0.0D);
        }
        if (value instanceof SpellItemValue item) {
            return item.isEmpty() ? SpellItemValue.EMPTY : item.snapshot().getItem();
        }
        return value;
    }

    @Override
    public Class<?> getEvaluationType() {
        return currentListType();
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(currentMode().listTranslationKey());
    }

    @Override
    protected void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        clearModeParams();
        addParam(list = currentAdapter().createListParam(SpellParam.GENERIC_NAME_LIST, false));
        restoreParamSides(savedSides);
    }
}
