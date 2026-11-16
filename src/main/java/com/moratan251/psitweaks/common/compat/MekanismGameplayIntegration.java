package com.moratan251.psitweaks.common.compat;

import mekanism.api.heat.HeatAPI;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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

    static double supplyZoneHeat(ServerLevel level, List<BlockEntity> blockEntities, Map<BlockEntity, Integer> sideCache,
                                 double total, double maxTemperature, Predicate<BlockPos> mayUse) {
        // 同じマルチブロック（ボイラー・加温蒸発濃縮・核分裂炉など）は、どのブロックから受け取っても 1 つとして 1 回だけ。
        Set<Object> heated = Collections.newSetFromMap(new IdentityHashMap<>());
        List<IHeatHandler> handlers = new ArrayList<>();
        List<Double> rooms = new ArrayList<>();
        for (BlockEntity blockEntity : blockEntities) {
            IHeatHandler handler = heatHandlerOf(level, blockEntity, sideCache);
            if (handler == null) {
                continue;
            }
            if (!heated.add(heatKeyOf(blockEntity, handler)) || !mayUse.test(blockEntity.getBlockPos())) {
                continue;
            }
            double temperature = handler.getTotalTemperature();
            if (temperature >= maxTemperature) {
                continue;
            }
            // 上限の温度を越えない分だけ送る（熱 = 温度差 × 熱容量）。
            double room = (maxTemperature - temperature) * handler.getTotalHeatCapacity();
            if (room > 0.0) {
                handlers.add(handler);
                rooms.add(room);
            }
        }
        if (handlers.isEmpty() || total <= 0.0) {
            return 0.0;
        }
        // 総量を受け手で均等に分け、上限の温度で受け取りきれなかった分を、まだ入る受け手に分け直す（1 回だけ）。
        double share = total / handlers.size();
        double given = 0.0;
        List<Integer> open = new ArrayList<>();
        for (int i = 0; i < handlers.size(); i++) {
            double amount = Math.min(share, rooms.get(i));
            handlers.get(i).handleHeat(amount);
            given += amount;
            rooms.set(i, rooms.get(i) - amount);
            if (rooms.get(i) > 1.0E-9) {
                open.add(i);
            }
        }
        double leftover = total - given;
        if (leftover > 1.0E-9 && !open.isEmpty()) {
            double extra = leftover / open.size();
            for (int i : open) {
                double amount = Math.min(extra, rooms.get(i));
                handlers.get(i).handleHeat(amount);
                given += amount;
            }
        }
        return given;
    }

    /**
     * 作動式: 凍結領域の冷却（加熱の熱量と対の「冷却量」）: 冷却量 {@code total} を、下限の温度 {@code floor} より熱い
     * ブロックで均等に分けて取り除く。1 つから取り除くのは下限との温度差の {@code fraction} まで（温度差 × 割合 × 熱容量）で、
     * 下限より下には冷やさない。取り除ききれなかった分は、まだ取り除けるブロックに分け直す（1 回だけ）。同じマルチブロックには
     * 1 回だけ。取り除いた熱の合計を返す。
     */
    static MekanismGameplayCompat.ZoneHeatDrain drainZoneHeat(ServerLevel level, List<BlockEntity> blockEntities,
                                                              Map<BlockEntity, Integer> sideCache, double fraction, double total,
                                                              double floor, Predicate<BlockPos> mayUse) {
        Set<Object> cooled = Collections.newSetFromMap(new IdentityHashMap<>());
        List<IHeatHandler> handlers = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        // そのブロックが周囲の温度より上に持っていた熱（運べる熱の上限、2026-11-16 ユーザー、案 3）。
        List<Double> aboveAmbient = new ArrayList<>();
        // 熱源（組み上がったマルチブロックはマルチブロック全体、ほかはブロックエンティティ、15 節の B）。
        List<Object> sources = new ArrayList<>();
        for (BlockEntity blockEntity : blockEntities) {
            IHeatHandler handler = heatHandlerOf(level, blockEntity, sideCache);
            if (handler == null) {
                continue;
            }
            if (!cooled.add(heatKeyOf(blockEntity, handler)) || !mayUse.test(blockEntity.getBlockPos())) {
                continue;
            }
            // 冷やせる下限は周囲の温度によらない決まった温度。周囲の温度は、運べる熱（周囲より上にあった分）を求めるのに使う。
            double ambient = HeatAPI.getAmbientTemp(level, blockEntity.getBlockPos());
            double excess = handler.getTotalTemperature() - Math.max(0.0, floor);
            if (excess <= 0.01) {
                continue;
            }
            // handleHeat は負の値をそのまま受け取り下限を見ないので、取り除くのは必ず下限との差まで（割合は 1 以下）。
            double amount = excess * Math.min(1.0, fraction) * handler.getTotalHeatCapacity();
            if (amount > 0.0) {
                handlers.add(handler);
                amounts.add(amount);
                aboveAmbient.add(Math.max(0.0, handler.getTotalTemperature() - ambient) * handler.getTotalHeatCapacity());
                sources.add(heatSourceOf(blockEntity));
            }
        }
        if (handlers.isEmpty() || total <= 0.0) {
            return MekanismGameplayCompat.ZoneHeatDrain.NONE;
        }
        // 冷却量を均等に分け、温度差の割合で取り除ききれなかった分を、まだ取り除けるブロックに分け直す（1 回だけ）。
        double share = total / handlers.size();
        double removed = 0.0;
        double[] taken = new double[handlers.size()];
        List<Integer> open = new ArrayList<>();
        for (int i = 0; i < handlers.size(); i++) {
            double amount = Math.min(share, amounts.get(i));
            handlers.get(i).handleHeat(-amount);
            removed += amount;
            taken[i] += amount;
            amounts.set(i, amounts.get(i) - amount);
            if (amounts.get(i) > 1.0E-9) {
                open.add(i);
            }
        }
        double leftover = total - removed;
        if (leftover > 1.0E-9 && !open.isEmpty()) {
            double extra = leftover / open.size();
            for (int i : open) {
                double amount = Math.min(extra, amounts.get(i));
                handlers.get(i).handleHeat(-amount);
                removed += amount;
                taken[i] += amount;
            }
        }
        // 運べる熱: ブロックごとに min(奪った熱, (温度 − 周囲の温度) × 熱容量)。周囲より下まで冷やして奪った分は捨てる。
        // 熱を奪った熱源は、運べる熱が 0 でも載せる（その熱源を冷やしている、という印になる）。
        double carried = 0.0;
        Map<Object, Double> bySource = new IdentityHashMap<>();
        for (int i = 0; i < handlers.size(); i++) {
            if (taken[i] <= 0.0) {
                continue;
            }
            double amount = Math.min(taken[i], aboveAmbient.get(i));
            carried += amount;
            bySource.merge(sources.get(i), amount, Double::sum);
        }
        return new MekanismGameplayCompat.ZoneHeatDrain(removed, carried, bySource);
    }

    /**
     * 熱の受け渡しで「同じ熱源」とみなす単位（15 節の B）: 組み上がったマルチブロック（核分裂炉など）はマルチブロック全体、
     * それ以外はそのブロックエンティティ。別々の凍結領域が同じ熱源を冷やしているかを見分けるのに使う。
     */
    private static Object heatSourceOf(BlockEntity blockEntity) {
        if (blockEntity instanceof IMultiblock<?> multiblock) {
            MultiblockData data = multiblock.getMultiblock();
            if (data != null && data.isFormed()) {
                return data;
            }
        }
        return blockEntity;
    }

    /** 熱を送る・取り除く相手の重複よけのキー: 組み上がったマルチブロックはマルチブロック全体、それ以外は熱の窓口。 */
    private static Object heatKeyOf(BlockEntity blockEntity, IHeatHandler handler) {
        if (blockEntity instanceof IMultiblock<?> multiblock) {
            MultiblockData data = multiblock.getMultiblock();
            if (data != null && data.isFormed()) {
                return data;
            }
        }
        return handler;
    }

    static void buildEvaporationPlant(ServerLevel level, BlockPos start) {
        new mekanism.common.command.builders.Builders.EvaporationBuilder().build(level, start, false);
    }

    static boolean isFormedMultiblockAt(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof IMultiblock<?> multiblock && multiblock.getMultiblock() != null
                && multiblock.getMultiblock().isFormed();
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

    /** 覚え書きの値: 6 は面のない窓口、7 は窓口なし（0〜5 は Direction の番号）。 */
    private static final int NULL_SIDE = 6;
    private static final int NO_HANDLER = 7;

    /**
     * {@link #heatHandlerOf(ServerLevel, BlockEntity)} と同じ窓口を、どの面で見つかったかを {@code sideCache} に覚えて引く。
     * 毎 tick、熱を持たないブロックエンティティにまで 7 回ずつ問い合わせないように。覚えた面で
     * 見つからなくなったら（ブロックが変わったら）探し直す。壊れたブロックエンティティは null。
     */
    @Nullable
    private static IHeatHandler heatHandlerOf(ServerLevel level, BlockEntity blockEntity, Map<BlockEntity, Integer> sideCache) {
        if (blockEntity.isRemoved()) {
            return null;
        }
        Integer remembered = sideCache.get(blockEntity);
        if (remembered != null) {
            if (remembered == NO_HANDLER) {
                return null;
            }
            Direction side = remembered == NULL_SIDE ? null : Direction.from3DDataValue(remembered);
            IHeatHandler handler = level.getCapability(Capabilities.HEAT, blockEntity.getBlockPos(), blockEntity.getBlockState(),
                    blockEntity, side);
            if (handler != null) {
                return handler;
            }
        }
        BlockPos pos = blockEntity.getBlockPos();
        for (Direction side : Direction.values()) {
            IHeatHandler handler = level.getCapability(Capabilities.HEAT, pos, blockEntity.getBlockState(), blockEntity, side);
            if (handler != null) {
                sideCache.put(blockEntity, side.get3DDataValue());
                return handler;
            }
        }
        IHeatHandler handler = level.getCapability(Capabilities.HEAT, pos, blockEntity.getBlockState(), blockEntity, null);
        sideCache.put(blockEntity, handler != null ? NULL_SIDE : NO_HANDLER);
        return handler;
    }
}
