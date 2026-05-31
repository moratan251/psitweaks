package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.param.ParamString;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceSelectorNbtValue extends PieceSelectorNbtBase {
    private SpellParam<String> key;

    public PieceSelectorNbtValue(Spell spell) {
        super(spell);
    }

    @Override
    protected void addAdditionalParams() {
        addParam(key = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String nbtKey = getNotNullParamValue(context, key);
        return NbtTargetHelper.stringValue(getTargetData(context), nbtKey);
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
