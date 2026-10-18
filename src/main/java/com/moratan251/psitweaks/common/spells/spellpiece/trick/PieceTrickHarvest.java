package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 収穫。
 * 位置の熟した作物を収穫して術者のインベントリへ入れ(入りきらない分は術者の位置に落とす)、作物を植え直す。
 * 熟していない・保護されている場合は何もしない。
 */
public class PieceTrickHarvest extends PieceTrick {
    private static final int POTENCY = 20;
    private static final int COST = 40;

    private SpellParam<Vector3> position;

    public PieceTrickHarvest(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        HarvestHelper.Harvest harvest = HarvestHelper.begin(context);
        if (harvest != null) {
            harvest.harvest(positionVal.toBlockPos());
            harvest.finish();
        }
        return null;
    }
}
