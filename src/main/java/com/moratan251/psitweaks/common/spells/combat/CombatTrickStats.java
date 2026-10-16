package com.moratan251.psitweaks.common.spells.combat;

import vazkii.psi.api.spell.SpellCompilationException;

/** Compile-time checks shared by the combat tricks. */
public final class CombatTrickStats {
    private CombatTrickStats() {
    }

    /** Rounds a stat down and rejects values that do not fit Psi's integer stats. */
    public static int stat(double value) throws SpellCompilationException {
        if (Double.isNaN(value) || value >= Integer.MAX_VALUE || value < 0.0) {
            throw new SpellCompilationException(SpellCompilationException.STAT_OVERFLOW);
        }
        return (int) value;
    }

    /** The value of a required constant number parameter; it must be positive. */
    public static double positive(Double value, int x, int y) throws SpellCompilationException {
        if (value == null || Double.isNaN(value) || value <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        return value;
    }

    /** The value of a required constant number parameter; it must be a positive whole number. */
    public static int positiveInteger(Double value, int x, int y) throws SpellCompilationException {
        if (value == null || Double.isNaN(value) || value <= 0.0 || value != Math.floor(value) || value > Integer.MAX_VALUE) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);
        }
        return value.intValue();
    }
}
