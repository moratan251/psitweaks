package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
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
        return value;
    }

    private static boolean acceptsPlainVector(SpellParam<?> param) {
        return param instanceof ParamVector
                || param instanceof ParamNumberOrVector
                || param instanceof SpellParamPlainValue;
    }

    private static boolean acceptsBlockContext(SpellParam<?> param) {
        return param instanceof ParamBlockValue
                || param instanceof SpellParamContextualValue;
    }
}
