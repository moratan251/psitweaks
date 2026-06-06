package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;

public final class SpellParamValueConversionHelper {
    private SpellParamValueConversionHelper() {
    }

    public static Object convert(SpellContext context, SpellParam<?> param, Object value)
            throws SpellRuntimeException {
        if (value instanceof BlockValue block && acceptsPlainVector(param)) {
            return block.positionVector();
        }
        if (value instanceof Vector3 vector && acceptsBlockContext(param)) {
            return BlockValueHelper.snapshotVector(context, vector);
        }
        if (value instanceof VectorListWrapper vectorList && acceptsContextualList(param)) {
            return snapshotVectorList(context, vectorList);
        }
        return value;
    }

    private static BlockListWrapper snapshotVectorList(SpellContext context, VectorListWrapper vectorList)
            throws SpellRuntimeException {
        List<BlockValue> blocks = new ArrayList<>();
        for (Vector3 vector : vectorList) {
            blocks.add(BlockValueHelper.snapshotVector(context, vector));
        }
        return BlockListWrapper.make(blocks);
    }

    private static boolean acceptsPlainVector(SpellParam<?> param) {
        return param instanceof ParamVector
                || param instanceof SpellParamPlainValue;
    }

    private static boolean acceptsBlockContext(SpellParam<?> param) {
        return param instanceof ParamBlockValue
                || param instanceof SpellParamContextualValue;
    }

    private static boolean acceptsContextualList(SpellParam<?> param) {
        return param instanceof SpellParamContextualValueList;
    }
}
