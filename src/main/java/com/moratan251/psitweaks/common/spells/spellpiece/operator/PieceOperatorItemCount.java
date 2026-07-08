package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemValue;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorItemCount extends PsitweaksPieceOperator {
    private SpellParam<SpellItemValue> item;

    public PieceOperatorItemCount(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(item = new ParamSpellItemValue(PsitweaksSpellParams.ITEM, PsitweaksSpellParams.ITEM_COLOR,
                false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        SpellItemValue value = getNotNullParamValue(context, item);
        return value.isEmpty() ? 0.0D : (double) value.copyStack().getCount();
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
