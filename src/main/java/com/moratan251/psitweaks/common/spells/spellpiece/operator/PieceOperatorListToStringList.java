package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.util.ModeStringConversionHelper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamAny;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorListToStringList extends PsitweaksPieceOperator {
    private SpellParam<?> list;

    public PieceOperatorListToStringList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamAny(SpellParam.GENERIC_NAME_LIST, SpellParam.GRAY, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Object source = getNotNullParamValue(context, typed(list));
        return ModeStringConversionHelper.anyToStringList(source);
    }

    @Override
    public Class<?> getEvaluationType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }

    @SuppressWarnings("unchecked")
    private static <T> SpellParam<T> typed(SpellParam<?> param) {
        return (SpellParam<T>) param;
    }
}
