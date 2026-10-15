package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.common.spells.wrapper.BlockListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellParam;

/**
 * Param value conversions applied when a piece reads its inputs. 1.20.1 keeps only the Block List to Vector List
 * conversion; the general Block and Vector conversions of 1.21.1 were never backported.
 */
public final class SpellParamValueConversionHelper {
    private SpellParamValueConversionHelper() {
    }

    public static Object convert(SpellParam<?> param, Object value) {
        if (value instanceof BlockListWrapper blockList && param instanceof ParamVectorListWrapper) {
            return positionList(blockList);
        }
        return value;
    }

    public static VectorListWrapper positionList(BlockListWrapper blockList) {
        List<Vector3> positions = new ArrayList<>(blockList.size());
        for (BlockValue block : blockList) {
            positions.add(block.positionVector());
        }
        return VectorListWrapper.make(positions);
    }
}
