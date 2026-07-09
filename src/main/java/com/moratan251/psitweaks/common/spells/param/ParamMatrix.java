package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.value.MatrixValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.param.ParamSpecific;

public class ParamMatrix extends ParamSpecific<MatrixValue> {
    public ParamMatrix(String name, int color, boolean canDisable, boolean constant) {
        super(name, color, canDisable, constant);
    }

    @Override
    public Class<MatrixValue> getRequiredType() {
        return MatrixValue.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.matrix");
    }
}
