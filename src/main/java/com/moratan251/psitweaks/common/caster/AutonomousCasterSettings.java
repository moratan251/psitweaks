package com.moratan251.psitweaks.common.caster;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * 自律詠唱機のコンフィグ値。
 * BlockEntity の生成はコンフィグ読み込み前にも起こり得るため、未読み込み時は既定値を返す。
 */
public final class AutonomousCasterSettings {
    /** 仕様上の連射上限。コンフィグでもこれより短くはできない。 */
    public static final int ABSOLUTE_MIN_CAST_INTERVAL = 4;
    public static final int MAX_CAST_INTERVAL = 72_000;

    private AutonomousCasterSettings() {
    }

    public static int psiCapacity() {
        return Math.max(1, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterPsiCapacity.get(), 10_000));
    }

    public static int echoPerOperation() {
        return Math.max(1, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterEchoPerOperation.get(), 250));
    }

    public static int psiPerOperation() {
        return Math.max(1, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterPsiPerOperation.get(), 25));
    }

    /** 回復で Psi 1 を作るのに使う気化サイオニックエコー（mB）。アップグレードの分は含まない。 */
    public static double echoPerPsi() {
        return (double) echoPerOperation() / psiPerOperation();
    }

    /** アップグレード1つで上がる内蔵 CAD の性能値。CAD の性能値を上げない種類は 0。 */
    public static int cadStatBonus(AutonomousCasterUpgrade upgrade) {
        return switch (upgrade) {
            case POTENCY -> Math.max(0, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradePotency.get(), 500));
            case EFFICIENCY -> Math.max(0, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradeEfficiency.get(), 50));
            case COMPLEXITY -> Math.max(0, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradeComplexity.get(), 15));
            case PROJECTION -> Math.max(0, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradeProjection.get(), 3));
            case MEMORY -> Math.max(0, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradeMemory.get(), 7));
            default -> 0;
        };
    }

    /** サイオン容量アップグレードで増える内部 Psi の容量。 */
    public static int upgradePsiCapacity() {
        return Math.max(0, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradePsiCapacity.get(), 5_000));
    }

    /** サイオン変換速度アップグレードで増える、1 tick に回復する Psi。 */
    public static int upgradeConversionSpeed() {
        return Math.max(0, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradeConversionSpeed.get(), 10));
    }

    /** サイオン変換効率アップグレードで減る、Psi 1 あたりのエコー消費（mB）。 */
    public static double upgradeConversionEfficiency() {
        return Math.max(0.0D, safeDouble(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradeConversionEfficiency.get(), 3.0D));
    }

    /** 事象干渉力アップグレードで詠唱主体の事象干渉力に足す値（0.3 で +30%）。 */
    public static double upgradeInterference() {
        return Math.max(0.0D, safeDouble(() -> PsitweaksConfig.COMMON.autonomousCasterUpgradeInterference.get(), 0.3D));
    }

    public static long echoTankCapacity() {
        return Math.max(1L, safeLong(() -> PsitweaksConfig.COMMON.autonomousCasterEchoTankCapacity.get(), 8_000L));
    }

    public static int minCastInterval() {
        return Math.max(ABSOLUTE_MIN_CAST_INTERVAL,
                safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterMinCastInterval.get(), ABSOLUTE_MIN_CAST_INTERVAL));
    }

    public static int defaultCastInterval() {
        return clampCastInterval(safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterDefaultCastInterval.get(), 10));
    }

    public static float castVolume() {
        double volume = safeDouble(() -> PsitweaksConfig.COMMON.autonomousCasterCastVolume.get(), 0.0D);
        return (float) Math.max(0.0D, Math.min(1.0D, volume));
    }

    public static int logCapacity() {
        return Math.max(1, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterLogCapacity.get(), 100));
    }

    public static int maxLogMessageLength() {
        return Math.max(16, safeInt(() -> PsitweaksConfig.COMMON.autonomousCasterMaxLogMessageLength.get(), 2_048));
    }

    public static int clampCastInterval(int interval) {
        return Math.max(minCastInterval(), Math.min(MAX_CAST_INTERVAL, interval));
    }

    private static int safeInt(IntSupplier supplier, int fallback) {
        try {
            return supplier.getAsInt();
        } catch (IllegalStateException | NullPointerException e) {
            return fallback;
        }
    }

    private static long safeLong(LongSupplier supplier, long fallback) {
        try {
            return supplier.getAsLong();
        } catch (IllegalStateException | NullPointerException e) {
            return fallback;
        }
    }

    private static double safeDouble(DoubleSupplier supplier, double fallback) {
        try {
            return supplier.getAsDouble();
        } catch (IllegalStateException | NullPointerException e) {
            return fallback;
        }
    }
}
