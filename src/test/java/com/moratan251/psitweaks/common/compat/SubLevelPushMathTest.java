package com.moratan251.psitweaks.common.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubLevelPushMathTest {
    @Test
    void powerFourBalancesGravityForAnyMass() {
        // 威力 4 × 0.25 = 重力 1 倍。質量 100、重力 11 なら 1 tick に 100 × 11 × 0.05 = 55。
        assertEquals(55.0, SubLevelPushMath.impulsePerTick(100.0, 4 * 0.25, 11.0), 1.0E-9);
        // 重さに比例する（同じ加速になる）。
        assertEquals(550.0, SubLevelPushMath.impulsePerTick(1000.0, 4 * 0.25, 11.0), 1.0E-9);
        // 威力 10 は重力の 2.5 倍。
        assertEquals(137.5, SubLevelPushMath.impulsePerTick(100.0, 10 * 0.25, 11.0), 1.0E-9);
    }

    @Test
    void fallsBackToTheDefaultGravityAndIgnoresBadInput() {
        assertEquals(55.0, SubLevelPushMath.impulsePerTick(100.0, 1.0, Double.NaN), 1.0E-9);
        assertEquals(55.0, SubLevelPushMath.impulsePerTick(100.0, 1.0, 0.0), 1.0E-9);
        assertEquals(0.0, SubLevelPushMath.impulsePerTick(0.0, 1.0, 11.0), 1.0E-9);
        assertEquals(0.0, SubLevelPushMath.impulsePerTick(100.0, 0.0, 11.0), 1.0E-9);
    }
}
