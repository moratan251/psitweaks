package com.moratan251.psitweaks.common.spells.param;

import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

public class ParamAnyList extends SpellParam<Object> {
    public ParamAnyList(String name, int color, boolean canDisable) {
        super(name, color, canDisable);
    }

    @Override
    protected Class<Object> getRequiredType() {
        return Object.class;
    }

    @Override
    public Component getRequiredTypeString() {
        return Component.translatable("psitweaks.datatype.any_list");
    }

    @Override
    public boolean canAccept(SpellPiece piece) {
        Class<?> type = piece.getEvaluationType();
        return type != null && PsitweaksListAdapters.isListType(type);
    }
}
