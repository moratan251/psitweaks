package com.moratan251.psitweaks.common.compat;

import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 作動式: 除染と汚染領域から Mekanism の放射線を扱う。Mekanism のクラスを読み込むので、Mekanism があるときだけ呼ぶこと
 * （{@link #isRadiationEnabled} だけはいつ呼んでもよい）。線源の強さは Sv/h、生き物の被ばく量は Sv。
 */
public final class ZoneRadiationCompat {
    private ZoneRadiationCompat() {
    }

    /** 範囲の中の線源: 位置（線源の中心のブロック）と強さ（Sv/h）。 */
    public record Source(BlockPos pos, double magnitude) {
    }

    /** Mekanism が入っていて、設定で放射線が有効か。 */
    public static boolean isRadiationEnabled() {
        return MekanismCompat.isMekanismLoaded() && IRadiationManager.INSTANCE.isRadiationEnabled();
    }

    /** これより弱い線源は Mekanism が消す（Sv/h）。 */
    public static double minMagnitude() {
        return IRadiationManager.INSTANCE.minRadiationMagnitude();
    }

    /**
     * 中心のブロックの真ん中が {@code center} から {@code radius} 以内にある線源。中心のまわりの箱にかかるチャンクから集める
     * （チャンクが多すぎるときは、全部の線源の一覧から探す）。
     */
    public static List<Source> sourcesWithin(ServerLevel level, Vec3 center, double radius) {
        AABB around = new AABB(center, center).inflate(radius);
        double radiusSqr = radius * radius;
        List<Source> found = new ArrayList<>();
        int minChunkX = (int) Math.floor(around.minX) >> 4;
        int maxChunkX = (int) Math.floor(around.maxX) >> 4;
        int minChunkZ = (int) Math.floor(around.minZ) >> 4;
        int maxChunkZ = (int) Math.floor(around.maxZ) >> 4;
        long chunks = (long) (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
        if (chunks > MAX_CHUNK_LOOKUPS) {
            for (IRadiationSource source : IRadiationManager.INSTANCE.getRadiationSources().values()) {
                if (source.getPos().dimension().equals(level.dimension())) {
                    addIfWithin(found, source, center, radiusSqr);
                }
            }
            return found;
        }
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                for (IRadiationSource source : IRadiationManager.INSTANCE.getRadiationSources(level, chunkX, chunkZ)) {
                    addIfWithin(found, source, center, radiusSqr);
                }
            }
        }
        return found;
    }

    /** チャンクごとに線源を引く上限（半径 32 なら 5 × 5 か 6 × 6 チャンク）。 */
    private static final long MAX_CHUNK_LOOKUPS = 400;

    private static void addIfWithin(List<Source> found, IRadiationSource source, Vec3 center, double radiusSqr) {
        BlockPos pos = source.getPosition();
        if (Vec3.atCenterOf(pos).distanceToSqr(center) <= radiusSqr) {
            found.add(new Source(pos.immutable(), source.getMagnitude()));
        }
    }

    /** 位置 {@code pos} の線源の強さ（Sv/h、なければ 0）。 */
    public static double sourceAt(ServerLevel level, BlockPos pos) {
        for (IRadiationSource source : IRadiationManager.INSTANCE.getRadiationSources(level, pos.getX() >> 4, pos.getZ() >> 4)) {
            if (source.getPosition().equals(pos)) {
                return source.getMagnitude();
            }
        }
        return 0.0;
    }

    /**
     * 位置 {@code pos} の線源（あるものだけ）を {@code amount}（Sv/h）弱める。Mekanism の線源の強さを直接減らす
     * （{@code IRadiationManager#radiate} に負の量を渡す。同じ位置の線源に足し込まれ、見えている放射線の量も送り直される）。
     */
    public static void weakenSource(ServerLevel level, BlockPos pos, double amount) {
        IRadiationManager.INSTANCE.radiate(level, pos, -amount);
    }

    /** 位置 {@code pos} の線源を消す。 */
    public static void removeSource(ServerLevel level, BlockPos pos) {
        IRadiationManager.INSTANCE.removeRadiationSource(level, pos);
    }

    /** 位置 {@code pos} に強さ {@code magnitude}（Sv/h）の線源を足す（同じ位置にあれば強める）。 */
    public static void addSource(ServerLevel level, BlockPos pos, double magnitude) {
        IRadiationManager.INSTANCE.radiate(level, pos, magnitude);
    }

    /** 生き物 {@code entity} の被ばく量（Sv、放射線を受けない相手は 0）。 */
    public static double exposure(LivingEntity entity) {
        IRadiationEntity radiation = Capabilities.RADIATION_ENTITY.getCapability(entity, null);
        return radiation == null ? 0.0 : radiation.getRadiation();
    }

    /** 生き物 {@code entity} の被ばく量を {@code sieverts}（Sv）にする。放射線を受けない相手には何もしない。 */
    public static void setExposure(LivingEntity entity, double sieverts) {
        IRadiationEntity radiation = Capabilities.RADIATION_ENTITY.getCapability(entity, null);
        if (radiation != null) {
            radiation.set(Math.max(0.0, sieverts));
        }
    }

    /**
     * 生き物 {@code entity} を {@code sieverts}（Sv）だけ被ばくさせる。防護（MekaSuit などの防具、作動式: 放射線フィルタ）の
     * 割り引きは Mekanism 側で効く。
     */
    public static void radiate(LivingEntity entity, double sieverts) {
        IRadiationManager.INSTANCE.radiate(entity, sieverts);
    }

    /** Mekanism の放射線の粒（クライアント側の演出用）。 */
    public static net.minecraft.core.particles.ParticleOptions radiationParticle() {
        return mekanism.common.registries.MekanismParticleTypes.RADIATION.get();
    }
}
