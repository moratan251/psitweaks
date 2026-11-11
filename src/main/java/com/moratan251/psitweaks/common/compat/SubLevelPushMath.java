package com.moratan251.psitweaks.common.compat;

/**
 * 作動式: 加重領域が物理で動く構造物（Sable のサブレベル、Create Aeronautics の機体など）を押す力積の計算（2026-11-11 の設計 C）。
 * Minecraft や Sable のクラスに頼らない計算だけを置く（JUnit で確かめるため）。
 */
public final class SubLevelPushMath {
    /** 1 ゲーム tick の長さ（秒）。 */
    public static final double SECONDS_PER_TICK = 0.05;
    /** Sable の既定の重力の大きさ（m/s²）。ディメンションの重力が取れないときに使う。 */
    public static final double DEFAULT_GRAVITY = 11.0;

    private SubLevelPushMath() {
    }

    /**
     * 1 tick に与える力積の大きさ（N·s）: 質量 × 加速度 × 1 tick。加速度は その場の重力の大きさ × {@code gravityFraction}
     * （威力 × 0.25 × 設定の倍率。威力 4 で重力と釣り合う）。重さによらず同じ加速になる。
     */
    public static double impulsePerTick(double mass, double gravityFraction, double gravity) {
        if (!Double.isFinite(mass) || mass <= 0.0 || gravityFraction <= 0.0) {
            return 0.0;
        }
        double g = Double.isFinite(gravity) && gravity > 0.0 ? gravity : DEFAULT_GRAVITY;
        return mass * gravityFraction * g * SECONDS_PER_TICK;
    }
}
