package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

public class ParamVectorOrNumberList extends SpellParam<Object> {
    public ParamVectorOrNumberList(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    public Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.vector_or_number_list");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return Vector3.class.isAssignableFrom(type) || NumberListWrapper.class.isAssignableFrom(type);
    }
}
