package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.api.PsitweaksListAdapters;
import com.moratan251.psitweaks.common.spells.param.ParamAnyList;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorListSize extends PieceOperator {
    private SpellParam<?> list;

    public PieceOperatorListSize(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamAnyList(SpellParam.GENERIC_NAME_LIST, SpellParam.GRAY, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        return (double) PsitweaksListAdapters.size(source);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }

    @SuppressWarnings("unchecked")
    private static <T> SpellParam<T> typed(SpellParam<?> param) {
        return (SpellParam<T>) param;
    }
}
