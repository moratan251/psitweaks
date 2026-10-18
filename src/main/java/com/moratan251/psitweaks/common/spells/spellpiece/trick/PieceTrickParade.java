package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Trick: Parade. Conjures a lookalike of the caster at the position that lasts for Time seconds or 2 x Power hits.
 * While it stands, hostile mobs go for it, other players' spells aimed at the caster fail, and Operator: Entity
 * Position reports the lookalike's position to them (see {@link Parades}).
 */
public class PieceTrickParade extends PieceTrick {
    public static final int HITS_PER_POWER = 2;
    private static final double POTENCY_BASE = 300.0;
    private static final double POTENCY_PER_POWER = 150.0;
    private static final double POTENCY_PER_SECOND = 2.0;
    private static final double COST_BASE = 400.0;
    private static final double COST_PER_POWER = 100.0;
    private static final double COST_PER_SECOND = 10.0;

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;
    private SpellParam<Number> time;

    public PieceTrickParade(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true)
                .mul(POTENCY_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_TIME, true).mul(POTENCY_PER_SECOND))
                .add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true)
                .mul(COST_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_TIME, true).mul(COST_PER_SECOND))
                .add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.PURPLE, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        int powerVal = CombatTrickStats.positiveInteger(getParamEvaluation(power), x, y);
        int timeVal = CombatTrickStats.positiveInteger(getParamEvaluation(time), x, y);
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + (double) powerVal * POTENCY_PER_POWER
                + timeVal * POTENCY_PER_SECOND));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE + (double) powerVal * COST_PER_POWER
                + timeVal * COST_PER_SECOND));
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
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        int powerVal = Math.max(1, getParamValue(context, power).intValue());
        int seconds = Math.max(1, getParamValue(context, time).intValue());
        Parades.conjure(level, context.caster, positionVal.toVec3D(), powerVal * HITS_PER_POWER, seconds * 20);
        return null;
    }
}
