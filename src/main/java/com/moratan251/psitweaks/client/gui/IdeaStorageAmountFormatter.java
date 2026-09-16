package com.moratan251.psitweaks.client.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** イデアストレージの生量をGUI上の個/B/FE表記へ変換する。 */
public final class IdeaStorageAmountFormatter {
    public static final int ITEM_SCALE = 0;
    public static final int BUCKET_SCALE = 3;
    private static final BigDecimal ABBREVIATION_THRESHOLD = BigDecimal.valueOf(100_000L);
    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1_000L);
    private static final List<String> SUFFIXES = List.of("k", "M", "B", "T", "Qa", "Qi");

    private IdeaStorageAmountFormatter() {
    }

    public static BigDecimal asDisplayAmount(long rawAmount, int decimalScale) {
        if (decimalScale < 0) {
            throw new IllegalArgumentException("decimalScale must not be negative");
        }
        return BigDecimal.valueOf(rawAmount, decimalScale);
    }

    public static String formatExact(long rawAmount, int decimalScale) {
        return plain(asDisplayAmount(rawAmount, decimalScale));
    }

    public static String formatGrid(long rawAmount, int decimalScale) {
        BigDecimal amount = asDisplayAmount(rawAmount, decimalScale);
        if (amount.abs().compareTo(ABBREVIATION_THRESHOLD) < 0) {
            return plain(amount);
        }
        BigDecimal scaled = amount.movePointLeft(3);
        int suffix = 0;
        while (scaled.abs().compareTo(THOUSAND) >= 0 && suffix + 1 < SUFFIXES.size()) {
            scaled = scaled.movePointLeft(3);
            suffix++;
        }
        return compact(scaled) + SUFFIXES.get(suffix);
    }

    private static String compact(BigDecimal amount) {
        return plain(amount.setScale(1, RoundingMode.DOWN));
    }

    private static String plain(BigDecimal amount) {
        BigDecimal normalized = amount.stripTrailingZeros();
        return normalized.signum() == 0 ? "0" : normalized.toPlainString();
    }
}
