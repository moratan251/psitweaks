package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import net.minecraft.network.chat.Component;
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

public class PieceTrickMassBlockBreak extends PieceTrick {
    private static final int COST_BASE = 500;
    private static final double COST_MULTIPLIER = 0.5;
    private static final int POTENCY_BASE = 300;
    private static final double POTENCY_MULTIPLIER = 0.25;

    private SpellParam<VectorListWrapper> positions;
    private SpellParam<Number> maxBlocks;

    public PieceTrickMassBlockBreak(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel((double) POTENCY_BASE).add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER1, true).mul(POTENCY_MULTIPLIER).floor()));
        setStatLabel(EnumSpellStat.COST, new StatLabel((double) COST_BASE).add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER1, true).mul(COST_MULTIPLIER).floor()));
    }

    @Override
    public void initParams() {
        addParam(positions = new ParamVectorListWrapper(SpellParam.GENERIC_NAME_LIST, PsitweaksSpellParams.VECTOR_LIST_COLOR, false, false));
        addParam(maxBlocks = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER1, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Object evaluation = getParamEvaluation(maxBlocks);
        int count = MassBlockBreakHelper.MAX_REGION_BLOCKS;
        if (evaluation instanceof Number number) {
            if (!isPositiveInteger(number)) {
                throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);
            }
            count = Math.min(number.intValue(), MassBlockBreakHelper.MAX_REGION_BLOCKS);
        }
        meta.addStat(EnumSpellStat.POTENCY, (int) (POTENCY_BASE + count * POTENCY_MULTIPLIER));
        meta.addStat(EnumSpellStat.COST, (int) (COST_BASE + count * COST_MULTIPLIER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        VectorListWrapper positionsValue = getNotNullParamValue(context, positions);
        Number maxBlocksValue = getNotNullParamValue(context, maxBlocks);
        if (!isPositiveInteger(maxBlocksValue)) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        int limit = Math.min(maxBlocksValue.intValue(), MassBlockBreakHelper.MAX_REGION_BLOCKS);
        MassBlockBreakHelper.breakBlocks(context, positionsValue, limit);
        return null;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.empty();
    }

    private static boolean isPositiveInteger(Number number) {
        double value = number.doubleValue();
        return Double.isFinite(value) && value > 0 && value == Math.floor(value);
    }
}
