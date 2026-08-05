package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EntityTunnelerArrow extends AbstractArrow {
    /** 発射からこのtick数までは無重力で直進し、その後は通常の重力に従う */
    private static final int NO_GRAVITY_TICKS = 100;

    /** 同一mobに再度ヒットできるようになるまでのクールタイム(tick) */
    private static final int REHIT_COOLDOWN_TICKS = 20;

    /** エンティティIDごとの最終ヒットtick。貫通後に同じmobへ毎tick再ヒットするのを防ぎ、クールタイム後に再ヒットを許可する */
    private final Map<Integer, Integer> lastHitTicks = new HashMap<>();

    /** デスポーン判定が有効になる発射からの経過tick数(10秒) */
    private static final int DESPAWN_TICKS = 200;

    /** デスポーンに必要な射手からの距離の二乗(64ブロック) */
    private static final double DESPAWN_DISTANCE_SQR = 64.0 * 64.0;

    /** ダメージの下限値。速度が低下してもこの値を下回らない */
    private static final double MIN_DAMAGE = 36.0;

    /** 飛翔モード: 通常 */
    public static final int MODE_NORMAL = 0;
    /** 飛翔モード: 低速（非常に低速・無期限の無重力・20秒で距離に関係なくデスポーン） */
    public static final int MODE_SLOW = 1;
    /** 飛翔モード: 無慣性（速度一定・Psiの運動追加などで慣性なしに即転換）。現在未使用 */
    public static final int MODE_INERTIALESS = 2;
    /** 飛翔モード: ハイブリッド（1回の射撃で中央=通常・左右=低速の3本を扇状に発射。矢自体にこのモードは設定されない） */
    public static final int MODE_HYBRID = 3;

    private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(EntityTunnelerArrow.class, EntityDataSerializers.BYTE);

    /** 低速モードの発射速度倍率(1/15) */
    private static final float SLOW_SPEED_FACTOR = 0.01F;

    /** 低速モードのデスポーンtick数(60秒)。距離条件は適用しない */
    private static final int SLOW_DESPAWN_TICKS = 1200;

    /** 無慣性モードの発射速度倍率 */
    private static final float INERTIALESS_SPEED_FACTOR = 0.15F;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MODE, (byte) MODE_NORMAL);
    }

    public int getMode() {
        return this.entityData.get(DATA_MODE);
    }

    public void setMode(int mode) {
        this.entityData.set(DATA_MODE, (byte) mode);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        if (this.getMode() == MODE_SLOW) {
            velocity *= SLOW_SPEED_FACTOR;
        } else if (this.getMode() == MODE_INERTIALESS) {
            velocity *= INERTIALESS_SPEED_FACTOR;
        }
        super.shoot(x, y, z, velocity, inaccuracy);
    }

    @Override
    public void shootFromRotation(Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
        if (this.getMode() != MODE_SLOW) {
            super.shootFromRotation(shooter, x, y, z, velocity, inaccuracy);
            return;
        }
        // 低速モードは射手の移動速度に引きずられないよう、慣性の加算（super内の shooter.getKnownMovement() 加算）を行わない
        float yRot = y * (float) (Math.PI / 180.0);
        float xRot = x * (float) (Math.PI / 180.0);
        float f = -Mth.sin(yRot) * Mth.cos(xRot);
        float f1 = -Mth.sin((x + z) * (float) (Math.PI / 180.0));
        float f2 = Mth.cos(yRot) * Mth.cos(xRot);
        this.shoot(f, f1, f2, velocity, inaccuracy);
    }

    @Override
    public void push(double x, double y, double z) {
        if (this.getMode() == MODE_INERTIALESS) {
            // 慣性の影響を受けず、速度は現在のまま方向のみ付与された運動の方向へ切り替える
            double length = Math.sqrt(x * x + y * y + z * z);
            double speed = this.getDeltaMovement().length();
            if (length > 1.0E-6 && speed > 1.0E-6) {
                double scale = speed / length;
                this.setDeltaMovement(x * scale, y * scale, z * scale);
            }
            return;
        }
        super.push(x, y, z);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putByte("ArrowMode", (byte) this.getMode());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ArrowMode")) {
            this.setMode(compound.getByte("ArrowMode"));
        }
    }

    public EntityTunnelerArrow(EntityType<? extends EntityTunnelerArrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public EntityTunnelerArrow(Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(PsitweaksEntities.TUNNELER_ARROW.get(), owner, level, pickupItemStack, firedFromWeapon);
        this.setNoGravity(true);
    }

    public EntityTunnelerArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(PsitweaksEntities.TUNNELER_ARROW.get(), x, y, z, level, pickupItemStack, firedFromWeapon);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        // 無慣性モード: tick前の速度を記録し、重力や抵抗による速度変化をtick後に打ち消して速さを一定に保つ
        double inertialessSpeed = -1.0;
        if (this.getMode() == MODE_INERTIALESS) {
            inertialessSpeed = this.getDeltaMovement().length();
        }

        Vec3 start = this.position();
        super.tick();
        // AbstractArrow.tick は onHitBlock とは別に、矢の中心がブロックの衝突判定内にあると
        // 直接 inGround を立ててそのtickの移動をスキップする（貫通させても次tickに内部で停止する）。
        // トンネラーは常に貫通させるため、inGround を解除してスキップされた移動を手動で適用する
        if (this.inGround) {
            this.inGround = false;
            Vec3 delta = this.getDeltaMovement();
            this.setPos(this.getX() + delta.x, this.getY() + delta.y, this.getZ() + delta.z);
        }

        if (inertialessSpeed > 1.0E-6) {
            Vec3 delta = this.getDeltaMovement();
            double length = delta.length();
            if (length > 1.0E-6 && Math.abs(length - inertialessSpeed) > 1.0E-6) {
                this.setDeltaMovement(delta.scale(inertialessSpeed / length));
            }
        }

        // 低速モードは無期限で無重力のまま（5秒経っても落下しない）
        if (this.isNoGravity() && this.getMode() != MODE_SLOW && this.tickCount >= NO_GRAVITY_TICKS) {
            this.setNoGravity(false);
        }

        // ブロックを貫通するため消えない代わりに、発射から一定時間経過し射手から十分離れた矢はデスポーンさせる。
        // 低速モードは60秒経過で距離に関係なくデスポーンする
        if (!this.level().isClientSide) {
            boolean despawn;
            if (this.getMode() == MODE_SLOW) {
                despawn = this.tickCount >= SLOW_DESPAWN_TICKS;
            } else if (this.tickCount >= DESPAWN_TICKS) {
                Entity owner = this.getOwner();
                despawn = owner == null || this.distanceToSqr(owner) >= DESPAWN_DISTANCE_SQR;
            } else {
                despawn = false;
            }
            if (despawn) {
                this.discard();
                return;
            }
        }

        // バニラの矢は1tickに1体しかヒットしないため、高速時は列になったmobを取りこぼす。
        // ヒット済みのmobは canHitEntity のクールタイム判定で除外されるので、
        // 同じ区間を見つからなくなるまで再スキャンして残りのmobにもヒットさせる。
        if (!this.level().isClientSide && !this.isRemoved() && !this.inGround) {
            Vec3 end = this.position();
            while (!this.isRemoved()) {
                EntityHitResult hit = this.findHitEntity(start, end);
                if (hit == null) {
                    break;
                }
                this.onHitEntity(hit);
            }

            // 非常に低速の場合、矢がmobの当たり判定（inflate 0.3）の内部に入り込み、
            // 内部からのレイキャスト（AABB.clip）はヒットを返さないため接触判定で補完する
            if (!this.isRemoved()) {
                EntityHitResult overlap = this.findOverlappingEntity(end);
                if (overlap != null) {
                    this.onHitEntity(overlap);
                }
            }
        }
    }

    /** 矢の現在位置が当たり判定内にあるエンティティを1体返す。バニラのレイキャストが拾えない低速・接触時用 */
    @Nullable
    private EntityHitResult findOverlappingEntity(Vec3 pos) {
        AABB search = this.getBoundingBox().inflate(1.0);
        for (Entity entity : this.level().getEntities(this, search, this::canHitEntity)) {
            if (entity.getBoundingBox().inflate(0.3).contains(pos)) {
                return new EntityHitResult(entity);
            }
        }
        return null;
    }

    /** 最後にヒットしてから {@link #REHIT_COOLDOWN_TICKS} 経っていなければ true */
    private boolean isOnRehitCooldown(Entity target) {
        Integer lastHit = this.lastHitTicks.get(target.getId());
        return lastHit != null && this.tickCount - lastHit < REHIT_COOLDOWN_TICKS;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        // 射手自身には絶対に命中しない。再ヒットクールタイム中のmobにも当たらない
        return super.canHitEntity(target) && target != this.getOwner() && !this.isOnRehitCooldown(target);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // トンネラーは常にブロックを貫通し、突き刺さらない。
        // バニラのtick処理はヒットの有無にかかわらず位置を進めるため、何もしなければそのまま通過する
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(PsitweaksItems.TUNNELER.get());
    }

    /**
     * バニラの {@link AbstractArrow#onHitEntity} と同じ処理を、ダメージタイプだけ
     * {@code psitweaks:tunneler}（防具・エンチャント・エフェクト・盾・無敵時間貫通）に差し替えて行う。
     * mobに命中しても消失せず貫通する。同一mobへの再ヒットは {@link #REHIT_COOLDOWN_TICKS} のクールタイム後に許可する。
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity == this.getOwner() || this.isOnRehitCooldown(entity)) {
            return;
        }
        this.lastHitTicks.put(entity.getId(), this.tickCount);

        float velocity = (float) this.getDeltaMovement().length();
        double baseDamage = this.getBaseDamage();
        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().source(PsitweaksDamageTypes.TUNNELER, this, owner != null ? owner : this);
        if (this.getWeaponItem() != null && this.level() instanceof ServerLevel serverLevel) {
            baseDamage = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damageSource, (float) baseDamage);
        }

        int damage = Mth.ceil(Mth.clamp(velocity * baseDamage, MIN_DAMAGE, 2.147483647E9));
        if (this.isCritArrow()) {
            long bonus = (long) this.random.nextInt(damage / 2 + 2);
            damage = (int) Math.min(bonus + (long) damage, 2147483647L);
        }

        if (owner instanceof LivingEntity livingOwner) {
            livingOwner.setLastHurtMob(entity);
        }

        boolean isEnderman = entity.getType() == EntityType.ENDERMAN;
        int remainingFireTicks = entity.getRemainingFireTicks();
        if (this.isOnFire() && !isEnderman) {
            entity.igniteForSeconds(5.0F);
        }

        if (entity.hurt(damageSource, (float) damage)) {
            if (isEnderman) {
                return;
            }

            if (entity instanceof LivingEntity livingEntity) {
                this.doKnockback(livingEntity, damageSource);
                if (this.level() instanceof ServerLevel serverLevel) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, livingEntity, damageSource, this.getWeaponItem());
                }

                this.doPostHurtEffects(livingEntity);
                if (livingEntity != owner && livingEntity instanceof Player && owner instanceof ServerPlayer serverOwner && !this.isSilent()) {
                    serverOwner.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }
            }

            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        } else {
            entity.setRemainingFireTicks(remainingFireTicks);
            this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            }
        }
    }
}
