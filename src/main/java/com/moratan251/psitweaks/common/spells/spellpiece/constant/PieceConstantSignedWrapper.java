package com.moratan251.psitweaks.common.spells.spellpiece.constant;

import com.moratan251.psitweaks.mixin.SpellParamAccessor;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.IGenericRedirector;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellGrid;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * Constant: Signed Wrapper. Like Psi's Constant: Wrapper, it counts as its Constant when the spell is compiled and outputs
 * Target when it runs, kept within the Constant's absolute value; unlike it, Target keeps its own sign (a Constant of 5
 * gives -5 to 5).
 * <p>
 * Many tricks check a constant for its sign only when the spell is compiled, so a value of the other sign must not reach
 * them. Once every reachable piece has passed normal compilation, {@link #validateCompiled(CompiledSpell)} checks each
 * reachable piece that reads this piece through a constant parameter (directly or through connectors) by compiling it on
 * trial with the Constant made positive and then negative ({@link #takesEitherSign}): it may read this piece only if both
 * compile with the same stats. When Constant is 0, the trial probes +1 and -1 so inputs that allow 0 but reject negative
 * constants are still rejected. Constant pieces never may read it, since they would pass the value on to inputs this check
 * does not see. Parameters that take any value (operators, Trick: Debug) may read it like any other number.
 */
public class PieceConstantSignedWrapper extends SpellPiece {
    public static final String ERROR_TARGET = "psitweaks.spellerror.signed_wrapper_target";

    private SpellParam<Number> target;
    private SpellParam<Number> constant;
    private boolean evaluating;
    /** While a reader is compiled on trial: the sign the Constant is given (1 or -1). */
    @Nullable
    private Double trialSign;

    public PieceConstantSignedWrapper(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1.0));
    }

    @Override
    public void initParams() {
        addParam(target = new ParamNumber(SpellParam.GENERIC_NAME_TARGET, SpellParam.RED, false, false));
        addParam(constant = new ParamNumber("psi.spellparam.constant", SpellParam.GREEN, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
    }

    /**
     * Checks every reachable {@link PieceConstantSignedWrapper} in {@code compiled} in deterministic grid order.
     * Must run after all reachable pieces have passed normal parameter and metadata validation.
     */
    public static void validateCompiled(CompiledSpell compiled) throws SpellCompilationException {
        SpellGrid grid = compiled.sourceSpell.grid;
        Set<SpellPiece> compiledPieces = compiled.actionMap.keySet();
        for (int gx = 0; gx < SpellGrid.GRID_SIZE; gx++) {
            for (int gy = 0; gy < SpellGrid.GRID_SIZE; gy++) {
                SpellPiece piece = grid.gridData[gx][gy];
                if (piece instanceof PieceConstantSignedWrapper wrapper && compiledPieces.contains(piece)) {
                    wrapper.checkReaders(grid, compiledPieces);
                }
            }
        }
    }

    /** Fails if a reachable piece reads this one through a constant parameter but cannot take its value with either sign. */
    private void checkReaders(SpellGrid grid, Set<SpellPiece> compiledPieces) throws SpellCompilationException {
        for (int gx = 0; gx < SpellGrid.GRID_SIZE; gx++) {
            for (int gy = 0; gy < SpellGrid.GRID_SIZE; gy++) {
                SpellPiece reader = grid.gridData[gx][gy];
                // Connectors only pass the value on; whoever reads through them is checked instead.
                if (reader == null || reader == this || reader instanceof IGenericRedirector
                        || !compiledPieces.contains(reader) || !readsAsConstant(grid, reader)) {
                    continue;
                }
                if (reader.getPieceType() == EnumPieceType.CONSTANT || !takesEitherSign(reader)) {
                    throw new SpellCompilationException(ERROR_TARGET, x, y);
                }
            }
        }
    }

    /** Whether {@code reader} reads this piece through an enabled parameter that accepts it and requires a constant. */
    private boolean readsAsConstant(SpellGrid grid, SpellPiece reader) {
        for (Map.Entry<SpellParam<?>, SpellParam.Side> entry : reader.paramSides.entrySet()) {
            SpellParam<?> param = entry.getKey();
            SpellParam.Side side = entry.getValue();
            if (side.isEnabled() && ((SpellParamAccessor) param).psitweaks$requiresConstant()
                    && param.canAccept(this) && readsThis(grid, reader, side)) {
                return true;
            }
        }
        return false;
    }

    private boolean readsThis(SpellGrid grid, SpellPiece reader, SpellParam.Side side) {
        try {
            return grid.getPieceAtSideWithRedirections(reader.x, reader.y, side) == this;
        } catch (SpellCompilationException e) {
            return false;
        }
    }

    /**
     * Compiles {@code reader} on trial with the Constant made positive and then negative: true if both compile and give
     * the same stats.
     */
    private boolean takesEitherSign(SpellPiece reader) {
        SpellMetadata positive = new SpellMetadata();
        SpellMetadata negative = new SpellMetadata();
        try {
            trialSign = 1.0;
            reader.addToMetadata(positive);
            trialSign = -1.0;
            reader.addToMetadata(negative);
        } catch (SpellCompilationException | RuntimeException e) {
            return false;
        } finally {
            trialSign = null;
        }
        if (positive.errorsSuppressed != negative.errorsSuppressed) {
            return false;
        }
        for (EnumSpellStat stat : EnumSpellStat.values()) {
            if (positive.getStat(stat) != negative.getStat(stat)
                    || positive.getStatMultiplier(stat) != negative.getStatMultiplier(stat)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double targetVal = getNotNullParamValue(context, target).doubleValue();
        double limit = Math.abs(getNotNullParamValue(context, constant).doubleValue());
        if (limit == 0.0) {
            return 0.0;
        }
        double clamped = Math.max(-limit, Math.min(limit, targetVal));
        return clamped == 0.0 ? 0.0 : clamped;
    }

    @Override
    public Object evaluate() throws SpellCompilationException {
        if (evaluating) {
            return 0.0;
        }
        evaluating = true;
        try {
            Object value = getParamEvaluation(constant);
            if (trialSign != null && value instanceof Number number) {
                double magnitude = Math.abs(number.doubleValue());
                double probe = magnitude == 0.0 ? 1.0 : magnitude;
                return Math.copySign(probe, trialSign);
            }
            return value;
        } finally {
            evaluating = false;
        }
    }

    @Override
    public EnumPieceType getPieceType() {
        return EnumPieceType.CONSTANT;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
