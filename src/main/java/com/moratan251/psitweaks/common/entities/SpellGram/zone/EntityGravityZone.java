package com.moratan251.psitweaks.common.entities.SpellGram.zone;

import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.common.core.handler.AdditiveMotionHandler;

/**
 * 作動式: 加重領域（仮）の範囲。中の生物に、指定の向きへ毎 tick 運動を足し続ける（Psi の運動追加と同じ仕組み）。
 * 足す量は 威力 × {@link #ACCELERATION_PER_POWER}（ブロック/tick²、仮）で、事象干渉力では変わらない。
 * 上向きに押している間は落下の距離を数えない（エレベーターのように使っても落下ダメージを受けない）。
 */
public class EntityGravityZone extends SpellGramZone {
    /** 威力 1 あたりの毎 tick の加速（仮）。重力は 0.08 なので、威力 4 で上向きに釣り合う。 */
    public static final double ACCELERATION_PER_POWER = 0.02;
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
    protected void affect(LivingEntity target) {
        Vec3 push = direction.scale(power * ACCELERATION_PER_POWER);
        if (push.lengthSqr() < 1.0E-12) {
            return;
        }
        AdditiveMotionHandler.addMotion(target, push.x, push.y, push.z);
        if (push.y > 0.0) {
            target.resetFallDistance();
        }
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
