package com.moratan251.psitweaks.common.spells.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamStringListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorRandomString extends PieceOperator {
    private SpellParam<StringListWrapper> list;

    public PieceOperatorRandomString(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamStringListWrapper(SpellParam.GENERIC_NAME_LIST,
                PsitweaksSpellParams.STRING_LIST_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        StringListWrapper source = getNotNullParamValue(context, list);
        if (source.size() == 0) {
            return "";
        }

        return source.get(context.caster.getRandom().nextInt(source.size()));
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }
}
