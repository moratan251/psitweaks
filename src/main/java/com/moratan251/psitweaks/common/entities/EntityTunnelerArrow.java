package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class EntityTunnelerArrow extends AbstractArrow {
    private static final int NO_GRAVITY_TICKS = 100;
    private static final int REHIT_COOLDOWN_TICKS = 20;
    private static final int DESPAWN_TICKS = 200;
    private static final double DESPAWN_DISTANCE_SQR = 64.0D * 64.0D;
    private static final float SLOW_SPEED_FACTOR = 0.01F;
    private static final int SLOW_DESPAWN_TICKS = 1200;
    private static final float INERTIALESS_SPEED_FACTOR = 0.15F;

    public static final int MODE_NORMAL = 0;
    public static final int MODE_SLOW = 1;
    public static final int MODE_INERTIALESS = 2;
    public static final int MODE_HYBRID = 3;

    private static final EntityDataAccessor<Byte> DATA_MODE =
            SynchedEntityData.defineId(EntityTunnelerArrow.class, EntityDataSerializers.BYTE);

    private final Map<Integer, Integer> lastHitTicks = new HashMap<>();

    public EntityTunnelerArrow(EntityType<? extends EntityTunnelerArrow> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
    }

    public EntityTunnelerArrow(Level level, LivingEntity owner) {
        super(PsitweaksEntities.TUNNELER_ARROW.get(), owner, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_MODE, (byte) MODE_NORMAL);
    }

    public int getMode() {
        return entityData.get(DATA_MODE);
    }

    public void setMode(int mode) {
        entityData.set(DATA_MODE, (byte) Mth.clamp(mode, MODE_NORMAL, MODE_HYBRID));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        if (getMode() == MODE_SLOW) {
            velocity *= SLOW_SPEED_FACTOR;
        } else if (getMode() == MODE_INERTIALESS) {
            velocity *= INERTIALESS_SPEED_FACTOR;
        }
        super.shoot(x, y, z, velocity, inaccuracy);
    }

    @Override
    public void shootFromRotation(Entity shooter, float xRotation, float yRotation, float rotationOffset,
                                  float velocity, float inaccuracy) {
        if (getMode() != MODE_NORMAL && getMode() != MODE_SLOW) {
            super.shootFromRotation(shooter, xRotation, yRotation, rotationOffset, velocity, inaccuracy);
            return;
        }

        float yaw = yRotation * Mth.DEG_TO_RAD;
        float pitch = xRotation * Mth.DEG_TO_RAD;
        float x = -Mth.sin(yaw) * Mth.cos(pitch);
        float y = -Mth.sin((xRotation + rotationOffset) * Mth.DEG_TO_RAD);
        float z = Mth.cos(yaw) * Mth.cos(pitch);
        shoot(x, y, z, velocity, inaccuracy);
    }

    @Override
    public void push(double x, double y, double z) {
        if (getMode() == MODE_INERTIALESS) {
            double newLength = Math.sqrt(x * x + y * y + z * z);
            double speed = getDeltaMovement().length();
            if (newLength > 1.0E-6D && speed > 1.0E-6D) {
                double scale = speed / newLength;
                setDeltaMovement(x * scale, y * scale, z * scale);
            }
            return;
        }
        super.push(x, y, z);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putByte("ArrowMode", (byte) getMode());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ArrowMode")) {
            setMode(compound.getByte("ArrowMode"));
        }
    }

    @Override
    public void tick() {
        double inertialessSpeed = getMode() == MODE_INERTIALESS ? getDeltaMovement().length() : -1.0D;
        Vec3 start = position();
        super.tick();

        if (inGround) {
            inGround = false;
            Vec3 movement = getDeltaMovement();
            setPos(getX() + movement.x, getY() + movement.y, getZ() + movement.z);
        }

        if (inertialessSpeed > 1.0E-6D) {
            Vec3 movement = getDeltaMovement();
            double length = movement.length();
            if (length > 1.0E-6D && Math.abs(length - inertialessSpeed) > 1.0E-6D) {
                setDeltaMovement(movement.scale(inertialessSpeed / length));
            }
        }

        if (isNoGravity() && getMode() != MODE_SLOW && tickCount >= NO_GRAVITY_TICKS) {
            setNoGravity(false);
        }

        if (!level().isClientSide) {
            boolean despawn;
            if (getMode() == MODE_SLOW) {
                despawn = tickCount >= SLOW_DESPAWN_TICKS;
            } else if (tickCount >= DESPAWN_TICKS) {
                Entity owner = getOwner();
                despawn = owner == null || distanceToSqr(owner) >= DESPAWN_DISTANCE_SQR;
            } else {
                despawn = false;
            }
            if (despawn) {
                discard();
                return;
            }
        }

        if (!level().isClientSide && !isRemoved() && !inGround) {
            Vec3 end = position();
            while (!isRemoved()) {
                EntityHitResult hit = findHitEntity(start, end);
                if (hit == null) {
                    break;
                }
                onHitEntity(hit);
            }

            if (!isRemoved()) {
                EntityHitResult overlap = findOverlappingEntity(end);
                if (overlap != null) {
                    onHitEntity(overlap);
                }
            }
        }
    }

    @Nullable
    private EntityHitResult findOverlappingEntity(Vec3 position) {
        AABB search = getBoundingBox().inflate(1.0D);
        for (Entity entity : level().getEntities(this, search, this::canHitEntity)) {
            if (entity.getBoundingBox().inflate(0.3D).contains(position)) {
                return new EntityHitResult(entity);
            }
        }
        return null;
    }

    private boolean isOnRehitCooldown(Entity target) {
        Integer lastHit = lastHitTicks.get(target.getId());
        return lastHit != null && tickCount - lastHit < REHIT_COOLDOWN_TICKS;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != getOwner() && !isOnRehitCooldown(target);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Tunnelers deliberately pass through blocks.
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(PsitweaksItems.TUNNELER.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (target == getOwner() || isOnRehitCooldown(target)) {
            return;
        }
        lastHitTicks.put(target.getId(), tickCount);

        double minimumDamage = PsitweaksConfig.COMMON.tunnelerMinimumDamage.get();
        int damage = Mth.ceil(Mth.clamp(getDeltaMovement().length() * getBaseDamage(), minimumDamage,
                Integer.MAX_VALUE));
        if (isCritArrow()) {
            long bonus = random.nextInt(damage / 2 + 2);
            damage = (int) Math.min(bonus + damage, Integer.MAX_VALUE);
        }

        Entity owner = getOwner();
        Holder<DamageType> damageType = level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(PsitweaksDamageTypes.TUNNELER);
        DamageSource source = new DamageSource(damageType, this, owner != null ? owner : this);
        if (owner instanceof LivingEntity livingOwner) {
            livingOwner.setLastHurtMob(target);
        }

        boolean enderman = target.getType() == EntityType.ENDERMAN;
        int remainingFireTicks = target.getRemainingFireTicks();
        if (isOnFire() && !enderman) {
            target.setSecondsOnFire(5);
        }

        if (target.hurt(source, damage)) {
            if (enderman) {
                return;
            }
            if (target instanceof LivingEntity livingTarget) {
                if (getKnockback() > 0) {
                    double resistance = Math.max(0.0D,
                            1.0D - livingTarget.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    Vec3 knockback = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize()
                            .scale(getKnockback() * 0.6D * resistance);
                    if (knockback.lengthSqr() > 0.0D) {
                        livingTarget.push(knockback.x, 0.1D, knockback.z);
                    }
                }
                if (!level().isClientSide && owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(livingTarget, livingOwner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, livingTarget);
                }
                doPostHurtEffects(livingTarget);
                if (livingTarget != owner && livingTarget instanceof Player
                        && owner instanceof ServerPlayer serverOwner && !isSilent()) {
                    serverOwner.connection.send(new ClientboundGameEventPacket(
                            ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }
            }
            playSound(getHitGroundSoundEvent(), 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
        } else {
            target.setRemainingFireTicks(remainingFireTicks);
            setDeltaMovement(getDeltaMovement().scale(-0.1D));
            setYRot(getYRot() + 180.0F);
            yRotO += 180.0F;
            if (!level().isClientSide && getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (pickup == Pickup.ALLOWED) {
                    spawnAtLocation(getPickupItem(), 0.1F);
                }
                discard();
            }
        }
    }
}
