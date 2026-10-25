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
