package com.moratan251.psitweaks.common.effects;

final class FlightPsiUpkeep {
    private static final int[] PSI_COST_BY_AMPLIFIER = {1000, 750, 500, 250};
    private static final int PSI_COST_LEVEL_FIVE_OR_HIGHER = 5;

    private FlightPsiUpkeep() {
    }

    static int getPsiCostForAmplifier(int amplifier) {
        if (amplifier >= PSI_COST_BY_AMPLIFIER.length) {
            return PSI_COST_LEVEL_FIVE_OR_HIGHER;
        }
        return PSI_COST_BY_AMPLIFIER[Math.max(0, amplifier)];
    }

    static int getAdjustedPsiCostForAmplifier(
            int amplifier,
            int cadEfficiency,
            double bulletCostModifier
    ) {
        int normalizedEfficiency = cadEfficiency > 0 ? cadEfficiency : 100;
        double normalizedCostModifier = Double.isFinite(bulletCostModifier)
                && bulletCostModifier >= 0.0
                ? bulletCostModifier
                : 1.0;
        double adjustedCost = getPsiCostForAmplifier(amplifier)
                * 100.0
                / normalizedEfficiency
                * normalizedCostModifier;
        if (adjustedCost >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) adjustedCost;
    }
}
