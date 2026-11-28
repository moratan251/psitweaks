package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

/** Accepts a Vector, a Number List or a Vector List: the rows or columns that Matrix Append Column / Row add. */
public class ParamMatrixLineSource extends SpellParam<Object> {
    public ParamMatrixLineSource(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    public Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.matrix_line_source");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return type != null && (Vector3.class.isAssignableFrom(type)
                || NumberListWrapper.class.isAssignableFrom(type)
                || VectorListWrapper.class.isAssignableFrom(type));
    }
}
