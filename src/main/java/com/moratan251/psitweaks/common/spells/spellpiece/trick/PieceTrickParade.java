package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Trick: Parade. Projects an image of the caster Number blocks in front of them (behind for a negative Number, a
 * constant), which stays there as they move and turn, for Time seconds. With a Direction, the image stands Number blocks
 * along that way instead, up and down included ({@link #fixedDirection}), and does not turn with the caster's view. A
 * Resident SpellGram inside it holds that time and its integrity, Time x the caster's Interference (see
 * {@link com.moratan251.psitweaks.common.entities.SpellGram.EntityParadeCore}). While it stands, mobs that go for the
 * caster go for it, attacks on the caster miss by a chance that grows with the distance, other players' spells aimed
 * at the caster fail, and Operator: Entity Position reports the image's position to them (see {@link Parades}). Cast
 * from an autonomous caster, it does nothing.
 */
public class PieceTrickParade extends PieceTrick {
    /**
     * The farthest the image stands from the caster; a larger Number is taken as this (keeping its sign). Farther out,
     * mobs after the image lose sight of it.
     */
    public static final int MAX_DISTANCE = 16;
    private static final double POTENCY_BASE = 300.0;
    private static final double POTENCY_PER_SECOND = 10.0;
    private static final double POTENCY_PER_BLOCK = 20.0;
    private static final double COST_BASE = 400.0;
    private static final double COST_PER_SECOND = 20.0;
    private static final double COST_PER_BLOCK = 50.0;

    private SpellParam<Number> number;
    private SpellParam<Number> time;
    private SpellParam<Vector3> direction;

    public PieceTrickParade(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_TIME, true)
                .mul(POTENCY_PER_SECOND)
                .add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER, true).abs().min(MAX_DISTANCE).mul(POTENCY_PER_BLOCK))
                .add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_TIME, true)
                .mul(COST_PER_SECOND)
                .add(new StatLabel(SpellParam.GENERIC_NAME_NUMBER, true).abs().min(MAX_DISTANCE).mul(COST_PER_BLOCK))
                .add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.BLUE, false, true));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.PURPLE, false, true));
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Number numberVal = getParamEvaluation(number);
        double value = numberVal == null ? 0.0 : numberVal.doubleValue();
        double blocks = Math.abs(distance(value));
        int timeVal = CombatTrickStats.positiveInteger(getParamEvaluation(time), x, y);
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + (double) timeVal * POTENCY_PER_SECOND
                + blocks * POTENCY_PER_BLOCK));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(costFor(timeVal, blocks)));
    }

    /**
     * Cost: 400 + 20 x Time + 50 x |Number| ({@code blocks} as {@link #distance} gives it, at most 16; the stat drops the
     * fraction of the total). Also what the core counts as its placement cost for Trick: Extend SpellGram.
     */
    public static double costFor(int seconds, double blocks) {
        return COST_BASE + (double) seconds * COST_PER_SECOND + Math.abs(blocks) * COST_PER_BLOCK;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Number numberVal = getParamValue(context, number);
        double value = numberVal == null ? 0.0 : numberVal.doubleValue();
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        double blocks = distance(value);
        int seconds = Math.max(1, getParamValue(context, time).intValue());
        Vec3 fixedDirection = fixedDirection(getParamValue(context, direction));
        Parades.conjure(level, context.caster, blocks, seconds, (float) costFor(seconds, blocks), fixedDirection);
        return null;
    }

    /**
     * The way the image stays when Direction is given, as a unit vector (up and down included), so the image keeps to that
     * side of the caster however they turn. Null when Direction is not given or is zero (or not a number): then the image
     * turns with the caster's view.
     */
    @javax.annotation.Nullable
    public static Vec3 fixedDirection(@javax.annotation.Nullable Vector3 direction) {
        if (direction == null) {
            return null;
        }
        Vec3 way = new Vec3(direction.x, direction.y, direction.z);
        double length = way.length();
        if (!(length > 1.0E-6) || !Double.isFinite(length)) {
            return null;
        }
        return way.scale(1.0 / length);
    }

    /**
     * The distance in blocks, fractions kept: anything beyond {@link #MAX_DISTANCE} is taken as {@link #MAX_DISTANCE},
     * keeping its sign. At 0 the image stands where the caster stands.
     */
    public static double distance(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        return Math.max(-MAX_DISTANCE, Math.min(MAX_DISTANCE, value));
    }
}
