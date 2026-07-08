package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.spellpiece.etc.JumpAnchorHelper;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;

import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickSwitch extends PieceTrick {
    private SpellParam<String> label;

    public PieceTrickSwitch(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1.0D));
        this.setStatLabel(EnumSpellStat.PROJECTION, new StatLabel(0.0D));
    }

    @Override
    public void initParams() {
        this.addParam(this.label = new ParamString(PsitweaksSpellParams.LABEL,
                PsitweaksSpellParams.STRING_COLOR, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        String targetLabel = StringSpellHelper.sanitize(this.getRequiredLabel(context));
        JumpAnchorHelper.jumpToMatchingAnchor(context, this, targetLabel);
        return null;
    }

    private String getRequiredLabel(SpellContext context) throws SpellRuntimeException {
        String value = this.getParamValue(context, this.label);
        if (value == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return value;
    }
}
