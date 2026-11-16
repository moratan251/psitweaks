package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 加熱領域と凍結領域の関わり（2026-11-16 ユーザー、案 3「運ぶだけ」）。
 * <ul>
 * <li>重なると、重なったところで打ち消し合う（{@link SpellGramZone#cancelledBy}）。湯気が立つ。</li>
 * <li>同じ術者の凍結領域と加熱領域が面で触れ合うか重なっている（2026-11-16 ユーザー: 重なりでも。重なったところは打ち消し合う
 * まま）と、凍結領域が Mekanism のブロックから奪った熱のうち、周囲の温度より上にあった分を、つながっている加熱領域へ運ぶ
 * （{@link #heatContacts}）。いくつもつながっていれば、つながりの広さ（{@link #linkArea}）に比例して分け、減らさずに渡す。
 * 触れ合う面では炎と雪がぶつかり、共鳴する音が鳴る。重なりでは湯気に共鳴する音が加わる。</li>
 * <li>熱は熱源ごとにまとめる（2026-11-16 ユーザー、15 節の B）: ある熱源を冷やしている自分の凍結領域のうち 1 つでも加熱領域に
 * 触れていれば、その熱源から自分の凍結領域が奪った熱（周囲より上の分）は全部渡る（{@link #route}）。分け方は、その熱源を
 * 冷やしていて加熱領域に触れている凍結領域の、触れ合う面の広さの合計に比例。他人の凍結領域が奪った分は運ばない。</li>
 * </ul>
 * 熱を作るのは加熱領域だけで、凍結領域は運ぶだけ。冷やし方は変えず、奪った熱の行き先だけを決める。Mekanism がなければ
 * 運ぶ熱はない。
 */
public final class ZoneHeatExchange {
    /** 触れている加熱領域を探し直す間隔と、演出の間隔（tick）。 */
    public static final int INTERVAL = 10;
    /** 他人の領域との相殺の音を鳴らす間隔（tick）。 */
    private static final int HINT_SOUND_INTERVAL = 40;
    /**
     * 受け渡しの演出の粒を減らさない受け取りの量（J/t、仮、2026-11-16 ユーザー、16 節）。これより少なければ量に合わせて減らす
     * （{@link #MIN_HINT_SCALE} まで）。
     */
    public static final double FULL_HINT_HEAT_PER_TICK = 600.0;
    /** 受け渡しの演出の粒を減らすときの下限の割合。 */
    private static final double MIN_HINT_SCALE = 0.1;

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

    /** 箱 {@code a} と {@code b} が触れ合う面の広さ（ブロックの面の数）。面で触れ合っていなければ 0（重なっていても 0）。 */
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
     * 箱 {@code a} と {@code b} のつながりの広さ（熱を分ける割合に使う）: 面で触れ合っていれば触れ合う面の広さ、重なっていれば
     * 重なりの箱のいちばん広い面の広さ（重なりを薄くしていくと、触れ合う面の広さにそのままつながる）。辺や角だけで接している・
     * 離れているときは 0。
     */
    public static double linkArea(AABB a, AABB b) {
        AABB overlap = ZoneAttacks.overlap(a, b);
        if (overlap != null && overlap.getXsize() > 0.0 && overlap.getYsize() > 0.0 && overlap.getZsize() > 0.0) {
            double x = overlap.getXsize();
            double y = overlap.getYsize();
            double z = overlap.getZsize();
            return Math.max(x * y, Math.max(y * z, x * z));
        }
        return contactArea(a, b);
    }

    /**
     * 凍結領域 {@code freeze} と面で触れ合っているか重なっている、同じ術者の加熱領域と、つながりの広さ（{@link #linkArea}）。
     * つながっていなければ空。凍結領域が {@link #INTERVAL} tick ごとに呼んで覚えておく（サーバー側だけ）。
     */
    static Map<EntityHeatZone, Double> heatContacts(ServerLevel level, EntityFreezeZone freeze) {
        UUID caster = freeze.getCasterUuid();
        if (caster == null || freeze.isEnded() || freeze.getArea().isEmpty()) {
            return Map.of();
        }
        AABB cold = freeze.getArea().bounds();
        Map<EntityHeatZone, Double> areas = new LinkedHashMap<>();
        for (SpellGramObject object : SpellGramIndex.ofCaster(level, caster)) {
            if (object instanceof EntityHeatZone heat && !heat.isEnded() && !heat.getArea().isEmpty()) {
                double area = linkArea(heat.getArea().bounds(), cold);
                if (area > 0.0) {
                    areas.put(heat, area);
                }
            }
        }
        return areas.isEmpty() ? Map.of() : areas;
    }

    /**
     * 凍結領域 {@code freeze} が熱源 {@code source} から運ぶ熱の渡し先と割合（合計 1、15 節の B）: 同じ術者の凍結領域のうち、
     * その熱源を冷やしているもの（{@code freeze} 自身を含む）がつながっている加熱領域ごとに、つながりの広さを足し合わせて割合に
     * する。どれも加熱領域につながっていなければ空（その熱源の熱は捨てる）。他人の凍結領域は数えない。
     */
    static Map<EntityHeatZone, Double> route(ServerLevel level, EntityFreezeZone freeze, Object source) {
        UUID caster = freeze.getCasterUuid();
        if (caster == null) {
            return Map.of();
        }
        Map<EntityHeatZone, Double> weights = new LinkedHashMap<>();
        double sum = 0.0;
        for (SpellGramObject object : SpellGramIndex.ofCaster(level, caster)) {
            if (object instanceof EntityFreezeZone other && !other.isEnded()
                    && (other == freeze || other.coolsSource(source))) {
                for (Map.Entry<EntityHeatZone, Double> contact : other.heatContacts().entrySet()) {
                    if (!contact.getKey().isEnded() && contact.getValue() > 0.0) {
                        weights.merge(contact.getKey(), contact.getValue(), Double::sum);
                        sum += contact.getValue();
                    }
                }
            }
        }
        if (weights.isEmpty()) {
            return Map.of();
        }
        for (Map.Entry<EntityHeatZone, Double> entry : weights.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
        return weights;
    }

    /**
     * 加熱領域が {@link #INTERVAL} tick ごとに呼ぶ: 凍結領域との関わりを見せる（2026-11-16 ユーザー、16 節: 演出と音を、実際に
     * 熱を運んでいるときに合わせる）。
     * <ul>
     * <li>運んでいる（この加熱領域が直前の間隔に熱を受け取った）: つながっている自分の凍結領域すべての、触れ合う面（重なりなら
     * 重なり）に炎と雪と火花。受け取りが {@link #FULL_HINT_HEAT_PER_TICK} 以上で粒を減らさず、それより少なければ量に合わせて
     * 減らす（最低 1 割）。共鳴音は運び始めたときに 1 回だけ。</li>
     * <li>つながっているが運ぶ熱がない: 火花を少しだけ、音はなし。Mekanism がなければ受け渡しの演出は出さない。</li>
     * <li>自分の凍結領域と重なっている: 湯気（打ち消しの印）。湯気の音は重なりができたときに 1 回だけ。</li>
     * <li>他人の凍結領域と重なっている: 湯気と、{@link #HINT_SOUND_INTERVAL} tick ごとの湯気の音（今までどおり）。</li>
     * </ul>
     */
    static void showHints(ServerLevel level, EntityHeatZone heat) {
        if (heat.isEnded() || heat.getArea().isEmpty()) {
            return;
        }
        AABB hot = heat.getArea().bounds();
        boolean sound = heat.tickCount % HINT_SOUND_INTERVAL == 0;
        boolean exchange = MekanismCompat.isMekanismLoaded();
        double received = heat.receivedHeatPerTick();
        boolean carrying = exchange && received > 0.0;
        boolean startCarrying = carrying && !heat.showsCarryingHint();
        heat.setCarryingHint(carrying);
        double scale = Math.max(MIN_HINT_SCALE, Math.min(1.0, received / FULL_HINT_HEAT_PER_TICK));
        Vec3 resonanceAt = null;
        Set<Integer> ownOverlaps = new HashSet<>();
        for (EntityFreezeZone freeze : SpellGramIndex.all(level, EntityFreezeZone.class)) {
            if (freeze.isEnded() || freeze.getArea().isEmpty()) {
                continue;
            }
            AABB cold = freeze.getArea().bounds();
            boolean sameCaster = heat.getCasterUuid() != null && heat.getCasterUuid().equals(freeze.getCasterUuid());
            AABB overlap = ZoneAttacks.overlap(hot, cold);
            AABB link;
            if (overlap != null && overlap.getSize() > 0.0) {
                if (!sameCaster) {
                    steam(level, overlap, sound);
                    continue;
                }
                // 自分の領域どうしの重なり: 湯気の音は重なりができたときに 1 回だけ。
                ownOverlaps.add(freeze.getId());
                steam(level, overlap, !heat.steamedOverlaps().contains(freeze.getId()));
                link = overlap;
            } else {
                link = sameCaster ? contactFace(hot, cold) : null;
            }
            if (link == null || !exchange) {
                continue;
            }
            if (carrying) {
                clash(level, link, hot, cold, scale);
                if (resonanceAt == null) {
                    resonanceAt = link.getCenter();
                }
            } else {
                sparks(level, link);
            }
        }
        heat.steamedOverlaps().retainAll(ownOverlaps);
        heat.steamedOverlaps().addAll(ownOverlaps);
        if (startCarrying) {
            Vec3 at = resonanceAt != null ? resonanceAt : hot.getCenter();
            level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.6F);
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

    /** つながっているが運ぶ熱がない: 触れ合う面（重なり）に火花を少しだけ。 */
    private static void sparks(ServerLevel level, AABB face) {
        Vec3 center = face.getCenter();
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 2,
                face.getXsize() * 0.3, face.getYsize() * 0.3, face.getZsize() * 0.3, 0.02);
    }

    /**
     * 運んでいる: 触れ合う面（重なり）の熱い側に炎、冷たい側に雪、面に火花。粒の数は {@code scale}（受け取りの量に合わせた割合）を
     * 掛ける。
     */
    private static void clash(ServerLevel level, AABB face, AABB hot, AABB cold, double scale) {
        Vec3 center = face.getCenter();
        // 面から少しだけ、それぞれの領域の側へずらす。
        Vec3 toHot = hot.getCenter().subtract(center).normalize().scale(0.25);
        Vec3 toCold = cold.getCenter().subtract(center).normalize().scale(0.25);
        double dx = face.getXsize() * 0.3;
        double dy = face.getYsize() * 0.3;
        double dz = face.getZsize() * 0.3;
        int full = (int) Math.max(3, Math.min(16, (face.getXsize() + face.getYsize() + face.getZsize())));
        int count = Math.max(1, (int) Math.round(full * scale));
        level.sendParticles(ParticleTypes.FLAME, center.x + toHot.x, center.y + toHot.y, center.z + toHot.z, count,
                dx, dy, dz, 0.01);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x + toCold.x, center.y + toCold.y, center.z + toCold.z, count,
                dx, dy, dz, 0.01);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, Math.max(1, count / 3),
                dx, dy, dz, 0.05);
    }
}
