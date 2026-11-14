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
     * ボイラーバルブなど）に、合計 {@code total} の熱を均等に分けて送る（2026-11-14 ユーザー: 熱は領域ごとの総量を受け手で分け合う。
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
     * 作動式: 凍結領域の冷却: 範囲の中の Mekanism の熱を持つブロックを、周囲の温度に向けて温度差の {@code fraction} だけ
     * 冷やす（周囲の温度より下には冷やさない、同じマルチブロックには 1 回だけ、{@code mayUse} が偽のブロックは冷やさない）。
     * 取り除く合計は {@code maxTotal} まで（超えたら、どの受け手も同じ割合で減らす。2026-11-14 ユーザー）。
     * 取り除いた熱の合計を返す。Mekanism がなければ 0。
     */
    public static double drainZoneHeat(ServerLevel level, List<BlockEntity> blockEntities, Map<BlockEntity, Integer> sideCache,
                                       double fraction, double maxTotal, Predicate<BlockPos> mayUse) {
        return MekanismCompat.isMekanismLoaded()
                ? MekanismGameplayIntegration.drainZoneHeat(level, blockEntities, sideCache, fraction, maxTotal, mayUse)
                : 0.0;
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
