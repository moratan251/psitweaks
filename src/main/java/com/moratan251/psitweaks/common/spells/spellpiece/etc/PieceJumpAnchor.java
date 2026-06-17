package com.moratan251.psitweaks.common.spells.spellpiece.etc;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;

import com.moratan251.psitweaks.common.spells.param.ParamConstantString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceJumpAnchor extends PieceTrick {
    private SpellParam<String> label;

    public PieceJumpAnchor(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(0.0D));
        this.setStatLabel(EnumSpellStat.PROJECTION, new StatLabel(0.0D));
    }

    @Override
    public void initParams() {
        this.addParam(this.label = new ParamConstantString(PsitweaksSpellParams.LABEL,
                PsitweaksSpellParams.STRING_COLOR, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) {
        // Marker only. It participates in action order but does not add stats.
    }

    String getConfiguredLabel() throws SpellCompilationException {
        String value = this.getParamEvaluationeOrDefault(this.label, "");
        return StringSpellHelper.sanitize(value);
    }

    boolean matchesConfiguredLabel(String targetLabel) throws SpellCompilationException {
        return this.getConfiguredLabel().equals(targetLabel);
    }
}
