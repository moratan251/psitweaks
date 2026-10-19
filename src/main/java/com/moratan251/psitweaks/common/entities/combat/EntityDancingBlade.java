package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.PsitweaksEntities;
import com.moratan251.psitweaks.common.registries.PsitweaksDamageTypes;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.SwordStrikes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * One blade of Trick: Dancing Blades. The blades circle their caster, dart at hostile creatures nearby (spreading
 * over several targets), cut, and come back, a few times each, until they fade.
 */
public class EntityDancingBlade extends CombatSpellEntity {
    public static final int LIFETIME = 300;
    public static final int STRIKES = 3;
    private static final double ORBIT_RADIUS = 1.6;
    private static final double SEEK_RANGE = 16.0;
    private static final double STRIKE_SPEED = 1.3;
    private static final double RETURN_SPEED = 1.1;
    private static final int STRIKE_TIMEOUT = 30;
    private static final int REST_TICKS = 6;
    private static final int SPAWN_TICKS = 6;
    /** Blades never stray this far from their caster: they vanish when the caster is gone. */
    private static final double OWNER_SEARCH_RANGE = 64.0;

    public static final byte ORBIT = 0;
    public static final byte STRIKE = 1;
    public static final byte RETURN = 2;

    private static final EntityDataAccessor<Byte> STATE = SynchedEntityData.defineId(EntityDancingBlade.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(EntityDancingBlade.class, EntityDataSerializers.INT);
    /** The sword from the Blade Pouch this blade is: what it looks like and how hard it cuts. */
    private static final EntityDataAccessor<ItemStack> SWORD = SynchedEntityData.defineId(EntityDancingBlade.class, EntityDataSerializers.ITEM_STACK);

    private int index;
    private int count = 1;
    private int launchDelay;
    private int strikesLeft = STRIKES;
    /** Whether the blade goes after any creature, not only hostile ones (server side; blades are never saved). */
    private boolean indiscriminate;
    private int stateTicks;
    private int restTicks;
    @Nullable
    private UUID targetUuid;
    /** Client only: the direction the blade pointed last frame, kept while it barely moves. */
    public Vec3 renderFacing = new Vec3(0.0, 0.0, 1.0);

    public EntityDancingBlade(EntityType<?> type, Level level) {
        super(type, level);
    }

    public EntityDancingBlade(Level level, LivingEntity caster, int index, int count) {
        this(PsitweaksEntities.DANCING_BLADE.get(), level);
        this.index = index;
        this.count = Math.max(1, count);
        this.launchDelay = 10 + index * 3;
        entityData.set(OWNER_ID, caster.getId());
        Vec3 start = CombatSpellHelper.center(caster);
        setPos(start.x, start.y, start.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, ORBIT);
        builder.define(OWNER_ID, -1);
        builder.define(SWORD, ItemStack.EMPTY);
    }

    public ItemStack getSword() {
        return entityData.get(SWORD);
    }

    public void setSword(ItemStack sword) {
        entityData.set(SWORD, sword.copyWithCount(1));
    }

    public void setIndiscriminate(boolean indiscriminate) {
        this.indiscriminate = indiscriminate;
    }

    public byte getState() {
        return entityData.get(STATE);
    }

    public int getOwnerId() {
        return entityData.get(OWNER_ID);
    }

    /** The blades {@code owner} has out (server side). */
    public static List<EntityDancingBlade> bladesOf(ServerLevel level, LivingEntity owner) {
        int ownerId = owner.getId();
        return level.getEntitiesOfClass(EntityDancingBlade.class, owner.getBoundingBox().inflate(OWNER_SEARCH_RANGE),
                blade -> blade.isAlive() && blade.getOwnerId() == ownerId);
    }

    /** Whether {@code owner} has blades out, which keeps their Blade Pouch locked (server side). */
    public static boolean hasBladesOut(ServerLevel level, LivingEntity owner) {
        return !bladesOf(level, owner).isEmpty();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            if (getState() == STRIKE) {
                level().addParticle(ParticleTypes.ENCHANTED_HIT, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
            }
            return;
        }
        LivingEntity caster = getCaster();
        if (caster == null || !caster.isAlive() || caster.level() != level || tickCount > LIFETIME
                || (strikesLeft <= 0 && getState() == ORBIT && restTicks <= 0)) {
            vanish(level);
            return;
        }
        stateTicks++;
        if (restTicks > 0) {
            restTicks--;
        }
        switch (getState()) {
            case STRIKE -> tickStrike(level, caster);
            case RETURN -> tickReturn(caster);
            default -> tickOrbit(level, caster);
        }
    }

    private Vec3 orbitSlot(LivingEntity caster) {
        double angle = Mth.TWO_PI * index / count + tickCount * 0.2;
        double grow = Math.min(1.0, (double) tickCount / SPAWN_TICKS);
        Vec3 center = CombatSpellHelper.center(caster).add(0.0, 0.2, 0.0);
        return center.add(Math.cos(angle) * ORBIT_RADIUS * grow, Math.sin(angle * 3.0) * 0.25, Math.sin(angle) * ORBIT_RADIUS * grow);
    }

    private void tickOrbit(ServerLevel level, LivingEntity caster) {
        Vec3 slot = orbitSlot(caster);
        setPos(slot.x, slot.y, slot.z);
        if (tickCount < launchDelay || restTicks > 0 || strikesLeft <= 0) {
            return;
        }
        LivingEntity target = findTarget(level, caster);
        if (target != null) {
            targetUuid = target.getUUID();
            setState(STRIKE);
            CombatEffects.sound(level, position(), SoundEvents.TRIDENT_THROW, 0.5F, 1.6F);
        }
    }

    private void tickStrike(ServerLevel level, LivingEntity caster) {
        Entity entity = targetUuid != null ? level.getEntity(targetUuid) : null;
        if (!(entity instanceof LivingEntity target) || !target.isAlive() || stateTicks > STRIKE_TIMEOUT
                || target.distanceToSqr(caster) > (SEEK_RANGE + 8.0) * (SEEK_RANGE + 8.0)) {
            setState(RETURN);
            return;
        }
        Vec3 aim = CombatSpellHelper.center(target);
        Vec3 offset = aim.subtract(position());
        double distance = offset.length();
        double reach = STRIKE_SPEED + target.getBbWidth() * 0.5;
        if (distance <= reach) {
            setPos(aim.x, aim.y, aim.z);
            strike(level, caster, target, offset.lengthSqr() > 1.0E-6 ? offset.normalize() : Vec3.ZERO);
            return;
        }
        Vec3 next = position().add(offset.scale(STRIKE_SPEED / distance));
        setPos(next.x, next.y, next.z);
    }

    private void strike(ServerLevel level, LivingEntity caster, LivingEntity target, Vec3 direction) {
        // A swing as the caster with the sword in hand: the caster is the direct attacker (Fire Aspect and the like only
        // act on direct hits) and the sword is the weapon; a copy takes whatever wear the hit causes.
        ItemStack sword = SwordStrikes.swingCopy(getSword());
        DamageSource source = SwordStrikes.weaponSource(level, PsitweaksDamageTypes.DANCING_BLADES, caster, sword);
        // A swing of the sword, scaled like every attack spell (global and per-spell multipliers, Phenomenon Interference).
        float damage = CombatSpellHelper.scaleDamage(SwordStrikes.meleeDamage(level, sword, target, source),
                PsitweaksConfig.COMMON.dancingBladesDamageMultiplier.get(), caster);
        if (CombatSpellHelper.canHarm(caster, target, isSafeToPlayers()) && target.hurt(source, damage)) {
            CombatSpellHelper.push(target, new Vec3(direction.x, 0.0, direction.z).scale(0.3).add(0.0, 0.08, 0.0));
            SwordStrikes.afterHit(level, caster, sword, target, source, direction);
        }
        Vec3 center = CombatSpellHelper.center(target);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 8, 0.3, 0.3, 0.3, 0.2);
        CombatEffects.sound(level, center, SoundEvents.PLAYER_ATTACK_SWEEP, 0.6F, 1.5F);
        strikesLeft--;
        setState(RETURN);
    }

    private void tickReturn(LivingEntity caster) {
        Vec3 slot = orbitSlot(caster);
        Vec3 offset = slot.subtract(position());
        double distance = offset.length();
        if (distance <= RETURN_SPEED || stateTicks > STRIKE_TIMEOUT) {
            setPos(slot.x, slot.y, slot.z);
            restTicks = strikesLeft > 0 ? REST_TICKS : 10;
            setState(ORBIT);
            return;
        }
        Vec3 next = position().add(offset.scale(RETURN_SPEED / distance));
        setPos(next.x, next.y, next.z);
    }

    /**
     * The nearest visible hostile creature (any creature when indiscriminate), preferring ones fewer sibling blades are
     * already after.
     */
    @Nullable
    private LivingEntity findTarget(ServerLevel level, LivingEntity caster) {
        AABB area = caster.getBoundingBox().inflate(SEEK_RANGE);
        Map<UUID, Integer> claims = new HashMap<>();
        for (EntityDancingBlade sibling : level.getEntitiesOfClass(EntityDancingBlade.class, area.inflate(8.0),
                blade -> blade != this && blade.getOwnerId() == getOwnerId() && blade.targetUuid != null && blade.getState() == STRIKE)) {
            claims.merge(sibling.targetUuid, 1, Integer::sum);
        }
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        Vec3 from = position();
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!CombatSpellHelper.isAutoTarget(caster, candidate, isSafeToPlayers(), indiscriminate)) {
                continue;
            }
            Vec3 center = CombatSpellHelper.center(candidate);
            double distance = center.distanceTo(CombatSpellHelper.center(caster));
            if (distance > SEEK_RANGE || !CombatSpellHelper.canSee(level, from, center, this)) {
                continue;
            }
            double score = claims.getOrDefault(candidate.getUUID(), 0) * 64.0 + distance;
            if (score < bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private void setState(byte state) {
        entityData.set(STATE, state);
        stateTicks = 0;
    }

    private void vanish(ServerLevel level) {
        level.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 4, 0.1, 0.1, 0.1, 0.02);
        discard();
    }
}
