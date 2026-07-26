package com.moratan251.psitweaks.common.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EffectFlightTest {
    @Test
    void mapsEffectAmplifierToTenTickPsiCost() {
        assertEquals(1000, FlightPsiUpkeep.getPsiCostForAmplifier(0));
        assertEquals(750, FlightPsiUpkeep.getPsiCostForAmplifier(1));
        assertEquals(500, FlightPsiUpkeep.getPsiCostForAmplifier(2));
        assertEquals(250, FlightPsiUpkeep.getPsiCostForAmplifier(3));
        assertEquals(5, FlightPsiUpkeep.getPsiCostForAmplifier(4));
        assertEquals(5, FlightPsiUpkeep.getPsiCostForAmplifier(9));
    }

    @Test
    void appliesCadEfficiencyToPsiCost() {
        assertEquals(666, FlightPsiUpkeep.getAdjustedPsiCostForAmplifier(0, 150, 1.0));
    }

    @Test
    void appliesBulletCostModifierToPsiCost() {
        assertEquals(500, FlightPsiUpkeep.getAdjustedPsiCostForAmplifier(0, 100, 0.5));
    }

    @Test
    void combinesCadAndBulletAdjustments() {
        assertEquals(333, FlightPsiUpkeep.getAdjustedPsiCostForAmplifier(0, 150, 0.5));
    }

    @Test
    void usesDefaultMultiplierForInvalidOrUnrecordedValues() {
        assertEquals(1000, FlightPsiUpkeep.getAdjustedPsiCostForAmplifier(0, 0, 1.0));
        assertEquals(1000, FlightPsiUpkeep.getAdjustedPsiCostForAmplifier(0, -1, Double.NaN));
    }

    @Test
    void followsZeroBulletCostModifier() {
        assertEquals(0, FlightPsiUpkeep.getAdjustedPsiCostForAmplifier(0, 100, 0.0));
    }
}
