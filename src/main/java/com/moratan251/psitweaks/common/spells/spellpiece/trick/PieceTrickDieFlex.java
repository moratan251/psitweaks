package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.util.SpellPsiRefundHelper;
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

public class PieceTrickDieFlex extends PieceTrick {
    private static final double STOP_THRESHOLD = 1.0D;

    private SpellParam<Number> target;

    public PieceTrickDieFlex(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1.0D));
    }

    @Override
    public void initParams() {
        this.addParam(this.target = new ParamNumber("psi.spellparam.target", SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double timeVal = this.getNotNullParamValue(context, this.target).doubleValue();
        if (Math.abs(timeVal) < STOP_THRESHOLD) {
            SpellPsiRefundHelper.refundRemainingActions(context);
            context.stopped = true;
        }
        return null;
    }
}
