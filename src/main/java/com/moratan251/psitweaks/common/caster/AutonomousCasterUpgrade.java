package com.moratan251.psitweaks.common.caster;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.cad.EnumCADStat;

/**
 * 自律詠唱機のアップグレードの種類。1種類で1つの要素だけを上げ、下げる要素はない。同じ種類は1台に1つまで入れられる。
 * 上がり幅は {@link AutonomousCasterSettings} のコンフィグ値。
 */
public enum AutonomousCasterUpgrade {
    /** 演算規模アップグレード。 */
    POTENCY(EnumCADStat.POTENCY),
    /** 演算効率アップグレード。 */
    EFFICIENCY(EnumCADStat.EFFICIENCY),
    /** オーバークロックアップグレード（複雑性）。 */
    COMPLEXITY(EnumCADStat.COMPLEXITY),
    /** マルチステップアップグレード（作動量）。 */
    PROJECTION(EnumCADStat.PROJECTION),
    /** メモリアップグレード（ベクトル記憶の数）。 */
    MEMORY(EnumCADStat.SAVED_VECTORS),
    /** サイオン容量アップグレード（内部 Psi の容量）。 */
    PSI_CAPACITY(null),
    /** サイオン変換速度アップグレード（1 tick に回復する Psi）。 */
    CONVERSION_SPEED(null),
    /** サイオン変換効率アップグレード（Psi 1 あたりのエコー消費を減らす）。 */
    CONVERSION_EFFICIENCY(null),
    /** 事象干渉力アップグレード（機械の詠唱主体の事象干渉力）。 */
    INTERFERENCE(null);

    private static final String ITEM_PREFIX = "autonomous_caster_upgrade_";

    @Nullable
    private final EnumCADStat cadStat;

    AutonomousCasterUpgrade(@Nullable EnumCADStat cadStat) {
        this.cadStat = cadStat;
    }

    /** 小文字の名前。アイテム ID・翻訳キー・コンフィグの名前に使う。 */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String itemId() {
        return ITEM_PREFIX + id();
    }

    /** 上げる内蔵 CAD の性能値。Psi の回復や事象干渉力を上げる種類は null。 */
    @Nullable
    public EnumCADStat cadStat() {
        return cadStat;
    }
}
