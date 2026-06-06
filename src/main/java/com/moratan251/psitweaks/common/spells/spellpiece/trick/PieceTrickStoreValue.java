package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;

import com.moratan251.psitweaks.common.spells.memory.CadPlainMemory;
import com.moratan251.psitweaks.common.spells.param.SpellParamPlainValue;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickStoreValue extends PieceTrick {
    private SpellParam<Number> number;
    private SpellParam<Object> value;

    public PieceTrickStoreValue(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.GREEN, false, false));
        addParam(value = new SpellParamPlainValue("psi.spellparam.target", PsitweaksSpellParams.PLAIN_VALUE_COLOR,
                false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
        Double numberVal = (Double) getParamEvaluation(number);
        if (numberVal != null && numberVal > 0D && numberVal == numberVal.intValue()) {
            meta.addStat(EnumSpellStat.POTENCY, numberVal.intValue() * 8);
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositiveinteger", x, y);
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int internalSlot = CadPlainMemory.internalSlot(getParamValue(context, number));
        if (CadPlainMemory.isSlotLocked(context, internalSlot)) {
            return null;
        }

        Object targetValue = getNotNullParamValue(context, value);
        CadPlainMemory.store(context, internalSlot, targetValue);
        CadPlainMemory.markSlotLocked(context, internalSlot);
        return null;
    }
}
