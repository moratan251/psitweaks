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
 * 起きている間、加熱側は「灼熱」、凍結側は「極寒」になり、それぞれの効き目はそのままで、中の生物に
 * {@link #INTERVAL} tick ごとにダメージを与える（攻撃術式の共通ルール {@link AttackRules}: 術者・術者のペット・同じ
 * チームには当てず、事象干渉力・全体倍率・設定 {@code inferno.damageMultiplier} で増減）。どちらかの領域が消えると終わる。
 * 本には書かない。初めて起こしたプレイヤー（自律詠唱機ならその持ち主）に隠し進捗を与える。
 */
public final class InfernoZones {
    /** ダメージの間隔（tick）。 */
    public static final int INTERVAL = 10;
    /** 1 回のダメージ（倍率を掛ける前、仮）。 */
    public static final float BASE_DAMAGE = 4.0F;
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
                heat.extendInferno(now + LINK_TICKS);
                freeze.extendInferno(now + LINK_TICKS);
                if (starting) {
                    ignite(level, heat, freeze);
                }
                return;
            }
        }
    }

    /** 氷炎地獄の中の生物にダメージを与える（加熱側・凍結側の両方から、{@link #INTERVAL} tick ごとに呼ぶ）。 */
    static void harm(ServerLevel level, SpellGramZone zone, ResourceKey<DamageType> type, Consumer<LivingEntity> afterHit) {
        ZoneAttacks.harm(level, zone, type, BASE_DAMAGE, PsitweaksConfig.COMMON.infernoDamageMultiplier.get(),
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
        // 自律詠唱機は持ち主の UUID を名乗るので、その UUID で本物のプレイヤー（ログインしていれば）に与える。
        LivingEntity caster = heat.getCaster();
        ServerPlayer player = caster instanceof AutonomousCasterPlayer machine
                ? level.getServer().getPlayerList().getPlayer(machine.getUUID())
                : caster instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        if (player != null) {
            PsitweaksAdvancements.award(player, PsitweaksAdvancements.INFERNO);
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
