package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

/** Accepts a Number or a Number List; the element-wise Number List operators read a Number as the same value everywhere. */
public class ParamNumberOrNumberList extends SpellParam<Object> {
    public ParamNumberOrNumberList(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    public Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.number_or_number_list");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return type != null && (Number.class.isAssignableFrom(type) || NumberListWrapper.class.isAssignableFrom(type));
    }
}
