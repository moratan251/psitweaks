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
     * ボイラーバルブなど）に、1 つあたり {@code heat} を送る。温度が {@code maxTemperature} 以上のものには送らず、
     * それを越えない分だけ送る（核分裂炉を損傷させないため）。同じマルチブロックには 1 回だけ。{@code mayUse} が偽の
     * ブロック（保護された場所）には送らない。送った数を返す。Mekanism がなければ何もしない。
     */
    public static int supplyZoneHeat(ServerLevel level, List<BlockEntity> blockEntities, double heat, double maxTemperature,
                                     Predicate<BlockPos> mayUse) {
        return MekanismCompat.isMekanismLoaded()
                ? MekanismGameplayIntegration.supplyZoneHeat(level, blockEntities, heat, maxTemperature, mayUse)
                : 0;
    }

    /** Mekanism の熱を持つブロック {@code pos} の温度（K）。Mekanism がないか、熱を持たないブロックなら NaN。GameTest 用。 */
    public static double temperatureAt(ServerLevel level, BlockPos pos) {
        return MekanismCompat.isMekanismLoaded() ? MekanismGameplayIntegration.temperatureAt(level, pos) : Double.NaN;
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
