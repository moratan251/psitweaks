package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 作動式: 遮音領域（仮、Silence Zone、2026-11-11 ユーザー）の範囲。範囲の中で鳴る音は聞こえず（クライアントで鳴らさない、
 * {@code SilenceZoneSounds}）、振動は範囲を通れない（羊毛と同じく、範囲の中で起きた振動も、範囲の中へ向かう振動も、範囲を
 * 横切る振動も届かない。{@code VibrationListenerSilenceZoneMixin}）。スカルクセンサー・調律されたスカルクセンサー・
 * スカルクシュリーカー・ウォーデンが気づかなくなる。範囲の中のスカルクシュリーカーは、踏んでも叫ばない
 * （{@code SculkShriekerSilenceZoneMixin}）。
 * <p>
 * 術者自身の音も消す。中のエンティティへの直接の効き目はない。
 */
public class EntitySilenceZone extends SpellGramZone {
    /** 辺の色: 灰色がかった青。 */
    public static final int OUTLINE_COLOR = 0x8494B8;
    /** 振動の道筋を確かめる間隔（ブロック）。 */
    private static final double PATH_STEP = 0.25;
    /** クライアントで、音の位置のまわりから領域を探す広さ（領域の中心から端まではこれより短い）。 */
    private static final double CLIENT_SEARCH_REACH = 32.0;
    private static final int MAX_MOTES_PER_TICK = 2;

    public EntitySilenceZone(EntityType<? extends EntitySilenceZone> type, Level level) {
        super(type, level);
    }

    public EntitySilenceZone(Level level) {
        this(PsitweaksEntities.SILENCE_ZONE.get(), level);
    }

    /**
     * 点 {@code point} が、効いている遮音領域の中か。サーバーは台帳から、クライアントは点のまわりのエンティティから探す
     * （クライアントは領域干渉に止められているかを知らないので、範囲の中かだけで決める）。
     */
    public static boolean silences(Level level, Vec3 point) {
        BlockPos pos = BlockPos.containing(point);
        for (EntitySilenceZone zone : zonesAround(level, point)) {
            if (!zone.isEnded() && zone.isInsideAndActive(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 振動が {@code from} から {@code to} へ届く道筋が、効いている遮音領域を通るか（サーバー側）。両端も含む: 範囲の中で起きた
     * 振動も、範囲の中の聞き手へ向かう振動も、範囲を横切る振動も遮る。
     */
    public static boolean blocksPath(Level level, Vec3 from, Vec3 to) {
        if (level.isClientSide) {
            return false;
        }
        List<EntitySilenceZone> zones = SpellGramIndex.all(level, EntitySilenceZone.class);
        if (zones.isEmpty()) {
            return false;
        }
        int steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) / PATH_STEP));
        for (EntitySilenceZone zone : zones) {
            if (zone.isEnded()) {
                continue;
            }
            AABB box = zone.getArea().bounds();
            if (!box.contains(from) && !box.contains(to) && box.clip(from, to).isEmpty()) {
                continue;
            }
            for (int i = 0; i <= steps; i++) {
                if (zone.isInsideAndActive(BlockPos.containing(from.lerp(to, i / (double) steps)))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<EntitySilenceZone> zonesAround(Level level, Vec3 point) {
        if (!level.isClientSide) {
            return SpellGramIndex.all(level, EntitySilenceZone.class);
        }
        return level.getEntitiesOfClass(EntitySilenceZone.class, new AABB(point, point).inflate(CLIENT_SEARCH_REACH));
    }

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    /** 中のエンティティへの直接の効き目はない（止めるのは音と振動）。 */
    @Override
    protected boolean affects(Entity entity) {
        return false;
    }

    @Override
    protected void affect(Entity target) {
    }

    /** 中にゆっくり落ちる灰色の粒（音のない空気）。 */
    @Override
    protected void spawnClientParticles() {
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int motes = (int) Math.min(MAX_MOTES_PER_TICK, Math.max(1, volume / 300.0));
        for (int i = 0; i < motes; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
