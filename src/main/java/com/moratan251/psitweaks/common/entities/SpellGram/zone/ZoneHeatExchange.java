package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 加熱領域と凍結領域の関わり（2026-11-16 ユーザー、案 3「運ぶだけ」）。
 * <ul>
 * <li>重なると、重なったところで打ち消し合う（{@link SpellGramZone#cancelledBy}）。湯気が立つ。</li>
 * <li>同じ術者の凍結領域と加熱領域が面で触れ合うと、凍結領域が Mekanism のブロックから奪った熱のうち、周囲の温度より上に
 * あった分を、触れている加熱領域へ運ぶ（{@link #heatPartners}）。いくつも触れていれば、触れ合う面の広さに比例して分け、
 * 減らさずに渡す。触れ合う面で炎と雪がぶつかり、共鳴する音が鳴る。</li>
 * </ul>
 * 熱を作るのは加熱領域だけで、凍結領域は運ぶだけ。Mekanism がなければ運ぶ熱はない。
 */
public final class ZoneHeatExchange {
    /** 触れている加熱領域を探し直す間隔と、演出の間隔（tick）。 */
    public static final int INTERVAL = 10;
    /** 相殺・接触の音を鳴らす間隔（tick）。 */
    private static final int HINT_SOUND_INTERVAL = 40;

    private ZoneHeatExchange() {
    }

    /**
     * 箱 {@code a} と {@code b} が面で触れ合っているときの、触れ合っている面（厚さ 0 の箱）。重なっている・離れている・
     * 辺や角だけで接しているときは null。
     */
    @Nullable
    public static AABB contactFace(AABB a, AABB b) {
        double minX = Math.max(a.minX, b.minX);
        double maxX = Math.min(a.maxX, b.maxX);
        double minY = Math.max(a.minY, b.minY);
        double maxY = Math.min(a.maxY, b.maxY);
        double minZ = Math.max(a.minZ, b.minZ);
        double maxZ = Math.min(a.maxZ, b.maxZ);
        // ちょうど 1 つの軸で厚さ 0（面）、残り 2 つの軸で広がりがあるときだけ。
        int flat = (minX == maxX ? 1 : 0) + (minY == maxY ? 1 : 0) + (minZ == maxZ ? 1 : 0);
        if (flat != 1 || minX > maxX || minY > maxY || minZ > maxZ) {
            return null;
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /** 箱 {@code a} と {@code b} が触れ合う面の広さ（ブロックの面の数）。面で触れ合っていなければ 0。 */
    public static double contactArea(AABB a, AABB b) {
        AABB face = contactFace(a, b);
        if (face == null) {
            return 0.0;
        }
        // 厚さ 0 の軸を除いた 2 つの軸の長さの積。
        double x = face.getXsize();
        double y = face.getYsize();
        double z = face.getZsize();
        return x == 0.0 ? y * z : y == 0.0 ? x * z : x * y;
    }

    /**
     * 凍結領域 {@code freeze} が熱を運ぶ先: 同じ術者の、面で触れ合っている加熱領域と、触れ合う面の広さの割合（合計 1）。
     * 触れていなければ空。凍結領域が {@link #INTERVAL} tick ごとに呼んで覚えておく（サーバー側だけ）。
     */
    static Map<EntityHeatZone, Double> heatPartners(ServerLevel level, EntityFreezeZone freeze) {
        UUID caster = freeze.getCasterUuid();
        if (caster == null || freeze.isEnded() || freeze.getArea().isEmpty()) {
            return Map.of();
        }
        AABB cold = freeze.getArea().bounds();
        Map<EntityHeatZone, Double> areas = new LinkedHashMap<>();
        double sum = 0.0;
        for (SpellGramObject object : SpellGramIndex.ofCaster(level, caster)) {
            if (object instanceof EntityHeatZone heat && !heat.isEnded() && !heat.getArea().isEmpty()) {
                double area = contactArea(heat.getArea().bounds(), cold);
                if (area > 0.0) {
                    areas.put(heat, area);
                    sum += area;
                }
            }
        }
        if (areas.isEmpty()) {
            return Map.of();
        }
        for (Map.Entry<EntityHeatZone, Double> entry : areas.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
        return areas;
    }

    /**
     * 加熱領域が {@link #INTERVAL} tick ごとに呼ぶ: 凍結領域との関わりを見せる。
     * <ul>
     * <li>重なっている（術者は問わない）: 加熱と凍結が打ち消し合っているので、重なったところから湯気が立ち、ジュッと鳴る。</li>
     * <li>同じ術者の凍結領域と面で触れ合っている（大きさ・ずれは問わない）: 熱を受け渡しているので、触れ合う面で炎と雪が
     * ぶつかり、共鳴する音が鳴る。</li>
     * </ul>
     */
    static void showHints(ServerLevel level, EntityHeatZone heat) {
        if (heat.isEnded() || heat.getArea().isEmpty()) {
            return;
        }
        AABB hot = heat.getArea().bounds();
        boolean sound = heat.tickCount % HINT_SOUND_INTERVAL == 0;
        for (EntityFreezeZone freeze : SpellGramIndex.all(level, EntityFreezeZone.class)) {
            if (freeze.isEnded() || freeze.getArea().isEmpty()) {
                continue;
            }
            AABB cold = freeze.getArea().bounds();
            AABB overlap = ZoneAttacks.overlap(hot, cold);
            if (overlap != null && overlap.getSize() > 0.0) {
                steam(level, overlap, sound);
                continue;
            }
            boolean sameCaster = heat.getCasterUuid() != null && heat.getCasterUuid().equals(freeze.getCasterUuid());
            AABB face = contactFace(hot, cold);
            if (face != null && sameCaster) {
                clash(level, face, hot, cold, sound);
            }
        }
    }

    /** 相殺: 重なったところから湯気が立つ。 */
    private static void steam(ServerLevel level, AABB overlap, boolean sound) {
        Vec3 center = overlap.getCenter();
        int count = (int) Math.max(4, Math.min(30, overlap.getSize() * 2.0));
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, count,
                overlap.getXsize() * 0.3, overlap.getYsize() * 0.3, overlap.getZsize() * 0.3, 0.02);
        level.sendParticles(ParticleTypes.WHITE_SMOKE, center.x, center.y, center.z, Math.max(2, count / 3),
                overlap.getXsize() * 0.3, overlap.getYsize() * 0.3, overlap.getZsize() * 0.3, 0.01);
        if (sound) {
            level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7F, 0.8F);
        }
    }

    /** 接触: 触れ合う面の熱い側に炎、冷たい側に雪、面に火花。 */
    private static void clash(ServerLevel level, AABB face, AABB hot, AABB cold, boolean sound) {
        Vec3 center = face.getCenter();
        // 面から少しだけ、それぞれの領域の側へずらす。
        Vec3 toHot = hot.getCenter().subtract(center).normalize().scale(0.25);
        Vec3 toCold = cold.getCenter().subtract(center).normalize().scale(0.25);
        double dx = face.getXsize() * 0.3;
        double dy = face.getYsize() * 0.3;
        double dz = face.getZsize() * 0.3;
        int count = (int) Math.max(3, Math.min(16, (face.getXsize() + face.getYsize() + face.getZsize())));
        level.sendParticles(ParticleTypes.FLAME, center.x + toHot.x, center.y + toHot.y, center.z + toHot.z, count,
                dx, dy, dz, 0.01);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x + toCold.x, center.y + toCold.y, center.z + toCold.z, count,
                dx, dy, dz, 0.01);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, Math.max(2, count / 3),
                dx, dy, dz, 0.05);
        if (sound) {
            level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
                    1.0F, 0.6F);
        }
    }
}
