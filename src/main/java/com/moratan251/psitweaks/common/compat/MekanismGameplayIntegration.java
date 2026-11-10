package com.moratan251.psitweaks.common.compat;

import mekanism.api.heat.IHeatHandler;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

final class MekanismGameplayIntegration {
    private MekanismGameplayIntegration() {
    }

    static SoundEvent laserSound() {
        return MekanismSounds.LASER.get();
    }

    static ResourceKey<DamageType> laserDamageType() {
        return MekanismDamageTypes.LASER.key();
    }

    static void supplyLaserEnergy(BlockEntity blockEntity, long energy) {
        if (blockEntity instanceof ILaserReceptor receptor) {
            receptor.receiveLaserEnergy(energy);
        }
    }

    static int supplyZoneHeat(ServerLevel level, List<BlockEntity> blockEntities, double heat, double maxTemperature,
                              Predicate<BlockPos> mayUse) {
        // 同じマルチブロック（ボイラー・加温蒸発濃縮・核分裂炉など）は、どのブロックから受け取っても 1 つとして 1 回だけ。
        Set<Object> heated = Collections.newSetFromMap(new IdentityHashMap<>());
        int count = 0;
        for (BlockEntity blockEntity : blockEntities) {
            IHeatHandler handler = heatHandlerOf(level, blockEntity);
            if (handler == null) {
                continue;
            }
            Object key = handler;
            if (blockEntity instanceof IMultiblock<?> multiblock) {
                MultiblockData data = multiblock.getMultiblock();
                if (data != null && data.isFormed()) {
                    key = data;
                }
            }
            if (!heated.add(key) || !mayUse.test(blockEntity.getBlockPos())) {
                continue;
            }
            double temperature = handler.getTotalTemperature();
            if (temperature >= maxTemperature) {
                continue;
            }
            // 上限の温度を越えない分だけ送る（熱 = 温度差 × 熱容量）。
            double room = (maxTemperature - temperature) * handler.getTotalHeatCapacity();
            double amount = Math.min(heat, room);
            if (amount > 0.0) {
                handler.handleHeat(amount);
                count++;
            }
        }
        return count;
    }

    static double temperatureAt(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        IHeatHandler handler = blockEntity == null ? null : heatHandlerOf(level, blockEntity);
        return handler == null ? Double.NaN : handler.getTotalTemperature();
    }

    /**
     * 熱を受け取れる面の熱の扱い。面のない問い合わせ（null）は Mekanism では読むだけの窓口で熱を受け取らないので、
     * 面を先に探し、どの面にもなければ null の窓口（温度を読むだけ）を返す。
     */
    @Nullable
    private static IHeatHandler heatHandlerOf(ServerLevel level, BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        for (Direction side : Direction.values()) {
            IHeatHandler handler = level.getCapability(Capabilities.HEAT, pos, blockEntity.getBlockState(), blockEntity, side);
            if (handler != null) {
                return handler;
            }
        }
        return level.getCapability(Capabilities.HEAT, pos, blockEntity.getBlockState(), blockEntity, null);
    }
}
