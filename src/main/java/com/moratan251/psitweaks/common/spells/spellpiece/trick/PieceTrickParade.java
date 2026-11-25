package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.server.level.ServerLevel;
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

/**
 * Trick: Parade. Projects an image of the caster Number blocks in front of them (behind for a negative Number, a
 * constant), which stays there as they move and turn. A Resident SpellGram inside it holds its Time and integrity (see
 * {@link com.moratan251.psitweaks.common.entities.SpellGram.EntityParadeCore}). While it stands, mobs that go for the
 * caster go for it, attacks on the caster miss by a chance that grows with the distance, other players' spells aimed
 * at the caster fail, and Operator: Entity Position reports the image's position to them (see {@link Parades}). Cast
 * from an autonomous caster, it does nothing.
 */
public class PieceTrickParade extends PieceTrick {
    /** The farthest the image stands from the caster, Psi's spell range. */
    public static final int MAX_DISTANCE = (int) SpellContext.MAX_DISTANCE;
    public static final String ERROR_ZERO_DISTANCE = "psitweaks.spellerror.parade_zero_distance";
    private static final double POTENCY_BASE = 300.0;
    private static final double POTENCY_PER_POWER = 150.0;
    private static final double POTENCY_PER_SECOND = 2.0;
    private static final double POTENCY_PER_BLOCK = 20.0;
    private static final double COST_BASE = 400.0;
    private static final double COST_PER_POWER = 100.0;
    private static final double COST_PER_SECOND = 10.0;
    private static final double COST_PER_BLOCK = 50.0;

    private SpellParam<Number> number;
    private SpellParam<Number> power;
    private SpellParam<Number> time;

    public PieceTrickParade(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true)
                .mul(POTENCY_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_TIME, true).mul(POTENCY_PER_SECOND))
                .add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER, true).abs().mul(POTENCY_PER_BLOCK))
                .add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true)
                .mul(COST_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_TIME, true).mul(COST_PER_SECOND))
                .add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER, true).abs().mul(COST_PER_BLOCK))
                .add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.BLUE, false, true));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.PURPLE, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Number numberVal = getParamEvaluation(number);
        double value = numberVal == null ? 0.0 : numberVal.doubleValue();
        String error = distanceError(value);
        if (error != null) {
            throw new SpellCompilationException(error, x, y);
        }
        int blocks = Math.abs(wholeBlocks(value));
        int powerVal = CombatTrickStats.positiveInteger(getParamEvaluation(power), x, y);
        int timeVal = CombatTrickStats.positiveInteger(getParamEvaluation(time), x, y);
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + (double) powerVal * POTENCY_PER_POWER
                + timeVal * POTENCY_PER_SECOND + blocks * POTENCY_PER_BLOCK));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(costFor(powerVal, timeVal, blocks)));
    }

    /**
     * Cost: 400 + 100 x Power + 10 x Time + 50 x |Number|. Also what the core counts as its placement cost for Trick:
     * Extend SpellGram.
     */
    public static double costFor(int power, int seconds, int blocks) {
        return COST_BASE + (double) power * COST_PER_POWER + (double) seconds * COST_PER_SECOND
                + (double) Math.abs(blocks) * COST_PER_BLOCK;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Number numberVal = getParamValue(context, number);
        double value = numberVal == null ? 0.0 : numberVal.doubleValue();
        String error = distanceError(value);
        if (error != null) {
            throw new SpellRuntimeException(error);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        int blocks = wholeBlocks(value);
        int powerVal = Math.max(1, getParamValue(context, power).intValue());
        int seconds = Math.max(1, getParamValue(context, time).intValue());
        Parades.conjure(level, context.caster, blocks, powerVal, seconds * 20, (float) costFor(powerVal, seconds, blocks));
        return null;
    }

    /** The distance in whole blocks: fractions are dropped, toward zero. */
    public static int wholeBlocks(double value) {
        return (int) (value < 0.0 ? Math.ceil(value) : Math.floor(value));
    }

    /**
     * Why the image cannot stand {@code value} blocks away, or null if it can: it must be a whole block away at least,
     * and no farther than {@link #MAX_DISTANCE}.
     */
    public static String distanceError(double value) {
        if (Double.isNaN(value) || Math.abs(value) < 1.0) {
            return ERROR_ZERO_DISTANCE;
        }
        if (Math.abs(value) >= MAX_DISTANCE + 1.0) {
            return SpellRuntimeException.OUTSIDE_RADIUS;
        }
        return null;
    }
}
