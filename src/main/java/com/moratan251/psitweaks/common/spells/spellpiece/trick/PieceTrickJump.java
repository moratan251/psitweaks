package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.spellpiece.etc.JumpAnchorHelper;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;

import com.moratan251.psitweaks.common.spells.param.ParamConstantString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickJump extends PieceTrick {
    private static final double JUMP_THRESHOLD = 1.0D;
    private static final String ERROR_NO_JUMP_ANCHOR = "psitweaks.spellerror.no_jump_anchor";

    private SpellParam<Number> target;
    private SpellParam<String> label;

    public PieceTrickJump(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1.0D));
        this.setStatLabel(EnumSpellStat.PROJECTION, new StatLabel(0.0D));
    }

    @Override
    public void initParams() {
        this.addParam(this.target = new ParamNumber("psi.spellparam.target", SpellParam.BLUE, true, false));
        this.addParam(this.label = new ParamConstantString(PsitweaksSpellParams.LABEL,
                PsitweaksSpellParams.STRING_COLOR, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
        String targetLabel = this.getConfiguredLabel();
        if (JumpAnchorHelper.findMatchingAnchorOrder(this.spell, this, targetLabel) < 0) {
            throw new SpellCompilationException(ERROR_NO_JUMP_ANCHOR, this.x, this.y);
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Number targetValue = this.getParamValue(context, this.target);
        if (targetValue != null && Math.abs(targetValue.doubleValue()) >= JUMP_THRESHOLD) {
            return null;
        }

        String targetLabel = this.getConfiguredLabelRuntime();
        if (!this.jumpToMatchingAnchor(context, targetLabel)) {
            throw new SpellRuntimeException(ERROR_NO_JUMP_ANCHOR);
        }
        return null;
    }

    protected boolean jumpToMatchingAnchor(SpellContext context, String targetLabel) throws SpellRuntimeException {
        return JumpAnchorHelper.jumpToMatchingAnchor(context, this, targetLabel);
    }

    private String getConfiguredLabel() throws SpellCompilationException {
        String value = this.getParamEvaluationeOrDefault(this.label, "");
        return StringSpellHelper.sanitize(value);
    }

    private String getConfiguredLabelRuntime() throws SpellRuntimeException {
        try {
            return this.getConfiguredLabel();
        } catch (SpellCompilationException exception) {
            throw new SpellRuntimeException(ERROR_NO_JUMP_ANCHOR);
        }
    }
}
