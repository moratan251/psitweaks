package com.moratan251.psitweaks.common.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class MekanismGameplayCompat {
    private MekanismGameplayCompat() {
    }

    public static SoundEvent laserSound() {
        return MekanismCompat.isMekanismLoaded()
                ? MekanismGameplayIntegration.laserSound()
                : SoundEvents.BEACON_ACTIVATE;
    }

    public static ResourceKey<DamageType> laserDamageType() {
        return MekanismCompat.isMekanismLoaded()
                ? MekanismGameplayIntegration.laserDamageType()
                : DamageTypes.MAGIC;
    }

    /**
     * 作動式: 加熱領域の熱: 範囲の中の Mekanism の熱を受け取るブロック（抵抗型発熱器、加温蒸発濃縮バルブ、熱発電機、
     * ボイラーバルブなど）に、合計 {@code total} の熱を均等に分けて送る（熱は領域ごとの総量を受け手で分け合う。
     * 受け取りきれなかった分は、まだ入る受け手に 1 回だけ分け直す）。温度が {@code maxTemperature} 以上のものには送らず、
     * それを越えない分だけ送る（核分裂炉を損傷させないため）。同じマルチブロックには 1 回だけ。{@code mayUse} が偽の
     * ブロック（保護された場所）には送らない。送った熱の合計を返す。Mekanism がなければ何もしない。{@code sideCache} は、熱の窓口が
     * どの面にあるかの覚え書き（領域が機械の一覧と一緒に持ち、一覧を作り直すときに空にする）。
     */
    public static double supplyZoneHeat(ServerLevel level, List<BlockEntity> blockEntities, Map<BlockEntity, Integer> sideCache,
                                        double total, double maxTemperature, Predicate<BlockPos> mayUse) {
        return MekanismCompat.isMekanismLoaded()
                ? MekanismGameplayIntegration.supplyZoneHeat(level, blockEntities, sideCache, total, maxTemperature, mayUse)
                : 0.0;
    }

    /**
     * 作動式: 凍結領域の冷却: 範囲の中の Mekanism の熱を持つブロックを、下限（周囲の温度 − {@code belowAmbient}）に向けて
     * 温度差の {@code fraction} だけ冷やす（下限より下には冷やさない、同じマルチブロックには 1 回だけ、{@code mayUse} が偽の
     * ブロックは冷やさない）。
     * 取り除く合計は {@code total} まで（均等に分け、取り除ききれなかった分を 1 回だけ分け直す）。
     * 取り除いた熱の合計と、そのうち周囲の温度より上にあった分（加熱領域へ運べる熱）を返す。Mekanism がなければ両方 0。
     */
    public static ZoneHeatDrain drainZoneHeat(ServerLevel level, List<BlockEntity> blockEntities, Map<BlockEntity, Integer> sideCache,
                                              double fraction, double total, double belowAmbient, Predicate<BlockPos> mayUse) {
        return MekanismCompat.isMekanismLoaded()
                ? MekanismGameplayIntegration.drainZoneHeat(level, blockEntities, sideCache, fraction, total, belowAmbient, mayUse)
                : ZoneHeatDrain.NONE;
    }

    /**
     * 凍結領域の冷却の結果（J）: 取り除いた熱 {@code removed} と、そのうち周囲の温度より上にあった分 {@code carried}（ブロック
     * ごとに min(奪った熱, (温度 − 周囲の温度) × 熱容量) の合計）。運べる熱は、触れ合っている加熱領域へ渡す（2026-11-16
     * ユーザー、案 3「運ぶだけ」）。
     * <p>
     * {@code carriedBySource} は運べる熱の熱源ごとの内訳（キーは熱源を表す中身の見えないもの、同じ熱源なら同じもの。組み上がった
     * マルチブロックは全体で 1 つ）。熱を奪った熱源は、運べる熱が 0 でも載る。同じ熱源を冷やす自分の凍結領域をまとめるのに使う
     * （15 節の B）。
     */
    public record ZoneHeatDrain(double removed, double carried, Map<Object, Double> carriedBySource) {
        public static final ZoneHeatDrain NONE = new ZoneHeatDrain(0.0, 0.0, Map.of());
    }

    /**
     * Mekanism の {@code /mek build} と同じく、{@code start} を角に 4 × 18 × 4 の加温蒸発濃縮プラント（外壁・中は空気・コントローラー）を
     * 組む。Mekanism がなければ何もしない。GameTest 用。
     */
    public static void buildEvaporationPlant(ServerLevel level, BlockPos start) {
        if (MekanismCompat.isMekanismLoaded()) {
            MekanismGameplayIntegration.buildEvaporationPlant(level, start);
        }
    }

    /** ブロック {@code pos} が、組み上がった Mekanism のマルチブロックの一部か。Mekanism がなければ偽。GameTest 用。 */
    public static boolean isFormedMultiblockAt(ServerLevel level, BlockPos pos) {
        return MekanismCompat.isMekanismLoaded() && MekanismGameplayIntegration.isFormedMultiblockAt(level, pos);
    }

    /** Mekanism の熱を持つブロック {@code pos} の温度（K）。Mekanism がないか、熱を持たないブロックなら NaN。GameTest 用。 */
    public static double temperatureAt(ServerLevel level, BlockPos pos) {
        return MekanismCompat.isMekanismLoaded() ? MekanismGameplayIntegration.temperatureAt(level, pos) : Double.NaN;
    }

    /** {@code blockEntity} が Mekanism Generators の風力発電機か。Mekanism Generators がなければ偽。 */
    public static boolean isWindGenerator(@Nullable BlockEntity blockEntity) {
        return blockEntity != null && MekanismCompat.isGeneratorsLoaded()
                && MekanismGeneratorsGameplayIntegration.isWindGenerator(blockEntity);
    }

    /** Mekanism Generators の風力発電機の最小の発電量（J/t、設定 generationMin）。ないときは NaN。GameTest 用。 */
    public static double windGenerationMinimum() {
        return MekanismCompat.isGeneratorsLoaded() ? MekanismGeneratorsGameplayIntegration.windGenerationMinimum() : Double.NaN;
    }

    /** 風力発電機の今の発電の倍率（0 なら止まっている）。Mekanism Generators がないか、風力発電機でなければ NaN。GameTest 用。 */
    public static double windMultiplierOf(@Nullable BlockEntity blockEntity) {
        return blockEntity != null && MekanismCompat.isGeneratorsLoaded()
                ? MekanismGeneratorsGameplayIntegration.windMultiplierOf(blockEntity)
                : Double.NaN;
    }

    /** {@code caster} is awarded the hidden advancement if this heat lights a Fusion Reactor. */
    public static void supplyLaserEnergy(BlockEntity blockEntity, long energy, @Nullable ServerPlayer caster) {
        if (MekanismCompat.isMekanismLoaded()) {
            if (caster != null && MekanismCompat.isGeneratorsLoaded()) {
                MekanismGeneratorsGameplayIntegration.watchFusionIgnition(blockEntity, caster);
            }
            MekanismGameplayIntegration.supplyLaserEnergy(blockEntity, energy);
        }
    }
}
