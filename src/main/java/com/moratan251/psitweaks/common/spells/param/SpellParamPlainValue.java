package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.PsitweaksPlainValues;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

public class SpellParamPlainValue extends SpellParam<Object> {
    public SpellParamPlainValue(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    protected Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.plain_value");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return PsitweaksPlainValues.findByClass(type).isPresent();
    }
}
