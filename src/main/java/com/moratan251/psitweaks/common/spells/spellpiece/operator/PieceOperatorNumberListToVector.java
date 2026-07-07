package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamNumberListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorNumberListToVector extends PsitweaksPieceOperator {
    private static final String ERROR_EXPECTED_THREE_NUMBERS = "psitweaks.spellerror.expected_three_numbers";

    private SpellParam<NumberListWrapper> target;

    public PieceOperatorNumberListToVector(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamNumberListWrapper(SpellParam.GENERIC_NAME_TARGET,
                PsitweaksSpellParams.NUMBER_LIST_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        NumberListWrapper numbers = getNotNullParamValue(context, target);
        if (numbers.size() != 3) {
            throw new SpellRuntimeException(ERROR_EXPECTED_THREE_NUMBERS);
        }

        return new Vector3(numbers.get(0), numbers.get(1), numbers.get(2));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.vector3");
    }
}
