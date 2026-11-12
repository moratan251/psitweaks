package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.compat.SablePhysicsCompat;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.common.core.handler.AdditiveMotionHandler;

/**
 * 作動式: 加重領域（仮）の範囲。中のエンティティ全般（生き物、落ちているアイテム、経験値、矢などの飛び道具、
 * トロッコやボート）に、指定の向きへ毎 tick 運動を足し続ける（Psi の運動追加と同じ仕組み）。スペクテイターと、物理で
 * 動かないもの（{@code noPhysics} の光線など）と魔法式オブジェクトは動かさない。
 * 足す量は 威力 × {@link #ACCELERATION_PER_POWER}（ブロック/tick²、仮）で、事象干渉力では変わらない。
 * 上向きに押している間は落下の距離を数えない（エレベーターのように使っても落下ダメージを受けない）。
 * <p>
 * Sable で物理で動く構造物（Simulated Contraption、Create Aeronautics の機体など）も、重心が範囲の中にある間、生物と同じ基準
 * （威力 4 で重力と釣り合う）で押す（2026-11-11 の設計 C、{@link SablePhysicsCompat#accelerateSubLevels}）。重さによらず同じ
 * 加速で、重心をまっすぐ押すので傾かない。設定 {@code gravity_zone.contraptionMultiplier}（0 で無効）で強さを変えられる。
 */
public class EntityGravityZone extends SpellGramZone {
    /** 威力 1 あたりの毎 tick の加速（仮）。重力は 0.08 なので、威力 4 で上向きに釣り合う。 */
    public static final double ACCELERATION_PER_POWER = 0.02;
    /** 物理で動く構造物（Sable のサブレベル）への加速: 威力 1 あたり、その場の重力の何倍か（仮）。威力 4 で釣り合う。 */
    public static final double SUB_LEVEL_GRAVITY_FRACTION_PER_POWER = 0.25;
    private static final int MAX_STREAKS_PER_TICK = 4;

    private Vec3 direction = Vec3.ZERO;
    private float power;

    public EntityGravityZone(EntityType<? extends EntityGravityZone> type, Level level) {
        super(type, level);
    }

    public EntityGravityZone(Level level) {
        this(PsitweaksEntities.GRAVITY_ZONE.get(), level);
    }

    /** 置く前に呼ぶ。{@code direction} は向きだけを使う。 */
    public void configurePush(Vec3 direction, float power) {
        this.direction = direction.lengthSqr() > 1.0E-12 ? direction.normalize() : Vec3.ZERO;
        this.power = Math.max(0.0F, power);
    }

    /** 辺の色: 白。 */
    public static final int OUTLINE_COLOR = 0xF0F0FF;

    @Override
    public int getOutlineColor() {
        return OUTLINE_COLOR;
    }

    public Vec3 getPushDirection() {
        return direction;
    }

    public float getPower() {
        return power;
    }

    @Override
    protected boolean affects(Entity entity) {
        return entity.isAlive() && !entity.isSpectator() && !entity.noPhysics;
    }

    /** 重なった加重領域の押す力（向き × 威力）を足したものの大きさの上限（威力、2026-11-12 ユーザー）。 */
    public static final float MAX_COMBINED_POWER = 10.0F;

    /** 加重領域は重なると押す向きを足し合わせる（{@link #combinedPushAt}）。 */
    @Override
    protected boolean stacksWithSameType() {
        return true;
    }

    /**
     * ブロック {@code pos} で、重なった加重領域（術者は問わない）の「向き × 威力」を足したもの。大きさは威力
     * {@link #MAX_COMBINED_POWER} で頭打ち。重なったうちエンティティの番号の一番小さい領域だけが代表として押し、ほかは null
     * （同じ物を二度押さない）。この領域が効いていないブロックも null。
     */
    @org.jetbrains.annotations.Nullable
    public Vec3 combinedPushAt(BlockPos pos) {
        if (!isInsideAndActive(pos)) {
            return null;
        }
        Vec3 sum = Vec3.ZERO;
        for (EntityGravityZone zone : com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex.all(level(), EntityGravityZone.class)) {
            if (zone.isEnded() || !zone.isInsideAndActive(pos)) {
                continue;
            }
            if (zone.getId() < getId()) {
                return null;
            }
            sum = sum.add(zone.unitDirection().scale(zone.power));
        }
        return capCombined(sum);
    }

    /** 足した押す力の大きさを威力 {@link #MAX_COMBINED_POWER} で頭打ちにする。 */
    public static Vec3 capCombined(Vec3 sum) {
        double length = sum.length();
        return length > MAX_COMBINED_POWER ? sum.scale(MAX_COMBINED_POWER / length) : sum;
    }

    private Vec3 unitDirection() {
        return direction.lengthSqr() < 1.0E-12 ? Vec3.ZERO : direction.normalize();
    }

    @Override
    protected void affect(Entity target) {
        Vec3 combined = combinedPushAt(BlockPos.containing(target.getBoundingBox().getCenter()));
        if (combined == null) {
            return;
        }
        Vec3 push = combined.scale(ACCELERATION_PER_POWER);
        if (push.lengthSqr() < 1.0E-12) {
            return;
        }
        AdditiveMotionHandler.addMotion(target, push.x, push.y, push.z);
        if (push.y > 0.0) {
            target.resetFallDistance();
        }
    }

    @Override
    protected void onSpellGramTickServer() {
        super.onSpellGramTickServer();
        if (isEnded() || power <= 0.0F || direction.lengthSqr() < 1.0E-12
                || !(level() instanceof ServerLevel level) || !SablePhysicsCompat.isLoaded()) {
            return;
        }
        double multiplier = PsitweaksConfig.COMMON.gravityZoneContraptionMultiplier.get();
        if (multiplier <= 0.0) {
            return;
        }
        // 機体も、重心のところで重なった加重領域の押す力を足して（威力 10 で頭打ち）、代表の領域だけが押す。
        SablePhysicsCompat.accelerateSubLevels(level, getOutline(), pos -> {
            Vec3 combined = combinedPushAt(pos);
            return combined == null ? null : combined.scale(SUB_LEVEL_GRAVITY_FRACTION_PER_POWER * multiplier);
        });
    }

    @Override
    protected void spawnClientParticles() {
        if (direction.lengthSqr() < 1.0E-12) {
            return;
        }
        AABB box = getOutline();
        double volume = box.getXsize() * box.getYsize() * box.getZsize();
        int streaks = (int) Math.min(MAX_STREAKS_PER_TICK, Math.max(1, volume / 200.0));
        Vec3 velocity = direction.scale(0.25);
        for (int i = 0; i < streaks; i++) {
            double x = box.minX + random.nextDouble() * box.getXsize();
            double y = box.minY + random.nextDouble() * box.getYsize();
            double z = box.minZ + random.nextDouble() * box.getZsize();
            if (getArea().contains(BlockPos.containing(x, y, z))) {
                level().addParticle(ParticleTypes.CLOUD, x, y, z, velocity.x, velocity.y, velocity.z);
            }
        }
    }

    @Override
    protected void writeZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeDouble(direction.x);
        buffer.writeDouble(direction.y);
        buffer.writeDouble(direction.z);
        buffer.writeFloat(power);
    }

    @Override
    protected void readZoneSpawnData(RegistryFriendlyByteBuf buffer) {
        direction = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        power = buffer.readFloat();
    }
}
