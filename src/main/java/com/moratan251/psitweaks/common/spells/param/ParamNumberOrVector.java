package com.moratan251.psitweaks.common.spells.param;

import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

public class ParamNumberOrVector extends SpellParam<Object> {
    public ParamNumberOrVector(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    protected Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.number_or_vector");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return Number.class.isAssignableFrom(type) || Vector3.class.isAssignableFrom(type);
    }
}
