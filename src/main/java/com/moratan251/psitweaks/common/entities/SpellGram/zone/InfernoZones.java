package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.handler.PsitweaksAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 隠し要素: 氷炎地獄（インフェルノ、2026-11-11 ユーザー）。同じ術者の加熱領域と凍結領域が、同じ大きさで面どうしぴったり
 * 接し、1 つの直方体を半分ずつ分け合っている（二分している）と起きる。どちらかが他者の領域干渉で止められていれば起きない。
 * <p>
 * 起きている間、加熱側は「灼熱」、凍結側は「極寒」になり、それぞれの効き目はそのままで（2026-11-15 ユーザー、案 A:
 * 灼熱側の熱の総量は 2 つの領域のコストの合計で決まり、極寒側が Mekanism の機械から奪った熱は灼熱側の Mekanism の機械へ
 * 移る。前の「熱・冷却 4 倍」は外した）、中の生物に
 * {@link #INTERVAL} tick ごとにダメージを与える（攻撃術式の共通ルール {@link AttackRules}: 術者・術者のペット・同じ
 * チームには当てず、事象干渉力・全体倍率・設定 {@code inferno.damageMultiplier} で増減）。どちらかの領域が消えると終わる。
 * 本には書かない。初めて起こしたプレイヤー（自律詠唱機ならその持ち主）に隠し進捗を与える。
 */
public final class InfernoZones {
    /** ダメージの間隔（tick）。 */
    public static final int INTERVAL = 10;
    /**
     * 両側の威力のうち小さいほう 1 あたりの、1 回のダメージ（倍率を掛ける前、仮）。威力 10 で 4。片方から減らした運動エネルギーを
     * もう片方へ逃がすので、強さは弱い側でそろう（2026-11-11 ユーザー）。
     */
    public static final float DAMAGE_PER_POWER = 0.4F;
    /** 加熱領域が組み合わせを確かめる間隔より少し長く、氷炎地獄を続けておく長さ（tick）。 */
    private static final int LINK_TICKS = INTERVAL * 2 + 5;
    private static final int BURST_PARTICLES = 60;

    private InfernoZones() {
    }

    /**
     * 範囲 {@code a} と {@code b} が、同じ大きさで面どうしぴったり接し、1 つの直方体を二分しているか: 2 つの軸では
     * 範囲が同じで、残りの 1 軸で隣り合い、3 軸とも長さが同じ。
     */
    public static boolean splitsOneBox(ZoneArea a, ZoneArea b) {
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        AABB p = a.bounds();
        AABB q = b.bounds();
        if (p.getXsize() != q.getXsize() || p.getYsize() != q.getYsize() || p.getZsize() != q.getZsize()) {
            return false;
        }
        boolean sameX = p.minX == q.minX && p.maxX == q.maxX;
        boolean sameY = p.minY == q.minY && p.maxY == q.maxY;
        boolean sameZ = p.minZ == q.minZ && p.maxZ == q.maxZ;
        boolean touchX = p.maxX == q.minX || q.maxX == p.minX;
        boolean touchY = p.maxY == q.minY || q.maxY == p.minY;
        boolean touchZ = p.maxZ == q.minZ || q.maxZ == p.minZ;
        return (touchX && sameY && sameZ) || (touchY && sameX && sameZ) || (touchZ && sameX && sameY);
    }

    /**
     * 加熱領域が {@link #INTERVAL} tick ごとに呼ぶ: 同じ術者の凍結領域で二分しているものがあれば、両方の氷炎地獄を続ける。
     * 起きた瞬間なら音と演出を出し、隠し進捗を与える。
     */
    static void link(ServerLevel level, EntityHeatZone heat) {
        UUID caster = heat.getCasterUuid();
        if (caster == null || heat.isEnded() || !heat.isActiveAt(heat.getArea().center())) {
            return;
        }
        long now = level.getGameTime();
        for (SpellGramObject object : SpellGramIndex.ofCaster(level, caster)) {
            if (object instanceof EntityFreezeZone freeze && !freeze.isEnded()
                    && splitsOneBox(heat.getArea(), freeze.getArea())
                    && freeze.isActiveAt(freeze.getArea().center())) {
                boolean starting = !heat.isInfernoAt(now) || !freeze.isInfernoAt(now);
                float power = Math.min(heat.getPower(), freeze.getPower());
                heat.extendInferno(now + LINK_TICKS, power, freeze);
                freeze.extendInferno(now + LINK_TICKS, power, heat);
                if (starting) {
                    ignite(level, heat, freeze);
                }
                return;
            }
        }
    }

    /** 相殺・接触の音を鳴らす間隔（tick）。 */
    private static final int HINT_SOUND_INTERVAL = 40;

    /**
     * 箱 {@code a} と {@code b} が面で触れ合っているときの、触れ合っている面（厚さ 0 の箱）。重なっている・離れている・
     * 辺や角だけで接しているときは null。
     */
    @org.jetbrains.annotations.Nullable
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

    /**
     * 加熱領域が {@link #INTERVAL} tick ごとに呼ぶ: 凍結領域との関わりを見せる（2026-11-11 ユーザー）。
     * <ul>
     * <li>重なっている（術者は問わない）: 加熱と凍結が打ち消し合っているので、重なったところから湯気が立ち、ジュッと鳴る。</li>
     * <li>同じ術者の凍結領域と面で触れ合っているが、二分にはなっていない（大きさ違い・ずれ）: 触れ合う面で炎と雪がぶつかり、
     * 共鳴する音が鳴る（氷炎地獄のヒント）。</li>
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
            if (face != null && sameCaster && !splitsOneBox(heat.getArea(), freeze.getArea())) {
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

    /** 氷炎地獄の中の生物にダメージを与える（加熱側・凍結側の両方から、{@link #INTERVAL} tick ごとに呼ぶ）。 */
    static void harm(ServerLevel level, SpellGramZone zone, ResourceKey<DamageType> type, Consumer<LivingEntity> afterHit) {
        ZoneAttacks.harm(level, zone, type, DAMAGE_PER_POWER * zone.getInfernoPower(),
                PsitweaksConfig.COMMON.infernoDamageMultiplier.get(),
                pos -> true, afterHit);
    }

    /** 起きた瞬間: 灼熱側から炎、極寒側から雪が噴き出し、音が鳴る。術者（自律詠唱機なら持ち主）に隠し進捗。 */
    private static void ignite(ServerLevel level, EntityHeatZone heat, EntityFreezeZone freeze) {
        burst(level, heat.getArea(), ParticleTypes.FLAME);
        burst(level, freeze.getArea(), ParticleTypes.SNOWFLAKE);
        Vec3 hot = Vec3.atCenterOf(heat.getArea().center());
        Vec3 cold = Vec3.atCenterOf(freeze.getArea().center());
        level.playSound(null, hot.x, hot.y, hot.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.6F);
        level.playSound(null, cold.x, cold.y, cold.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.5F);
        award(level, heat.getCaster(), PsitweaksAdvancements.INFERNO);
    }

    /**
     * 術者 {@code caster} に進捗 {@code id} を与える。自律詠唱機は持ち主の UUID を名乗るので、その UUID で本物のプレイヤー
     * （ログインしていれば）に与える。
     */
    static void award(ServerLevel level, @org.jetbrains.annotations.Nullable LivingEntity caster, net.minecraft.resources.ResourceLocation id) {
        ServerPlayer player = caster instanceof AutonomousCasterPlayer machine
                ? level.getServer().getPlayerList().getPlayer(machine.getUUID())
                : caster instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        if (player != null) {
            PsitweaksAdvancements.award(player, id);
        }
    }

    private static void burst(ServerLevel level, ZoneArea area, ParticleOptions particle) {
        AABB box = area.bounds();
        for (int i = 0; i < BURST_PARTICLES; i++) {
            double x = box.minX + level.random.nextDouble() * box.getXsize();
            double y = box.minY + level.random.nextDouble() * box.getYsize();
            double z = box.minZ + level.random.nextDouble() * box.getZsize();
            level.sendParticles(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.08);
        }
    }
}
