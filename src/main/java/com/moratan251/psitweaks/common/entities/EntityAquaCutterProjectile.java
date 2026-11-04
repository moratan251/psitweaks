package com.moratan251.psitweaks.common.entities;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The water blade of Trick: Aqua Cutter. It flies like a thrown item (with gravity) and is drawn as a crescent of
 * water; the hit itself is handled by {@code AquaCutterProjectileHandler}.
 */
public class EntityAquaCutterProjectile extends ThrowableItemProjectile {
    /** The blue of the water. The caster's CAD colour is mixed into it by {@link #tint}. */
    public static final int WATER_COLOR = 0x3DA5F0;
    /** How much of the caster's CAD colour {@link #tint} mixes into the water's blue. */
    private static final float CAD_ACCENT = 0.3F;
    /** Drops are shed only once the blade is this many ticks out, so they do not fall in front of the caster's eyes. */
    private static final int DROPS_FROM_TICK = 2;

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(EntityAquaCutterProjectile.class, EntityDataSerializers.INT);

    public EntityAquaCutterProjectile(EntityType<? extends EntityAquaCutterProjectile> type, Level level) {
        super(type, level);
    }

    public EntityAquaCutterProjectile(Level level, LivingEntity owner) {
        super(PsitweaksEntities.AQUA_CUTTER_PROJECTILE.get(), owner, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, CombatSpellHelper.DEFAULT_SPELL_COLOR);
    }

    /** The caster's CAD colour, which tints the glow of the water. */
    public int getColor() {
        return entityData.get(COLOR);
    }

    public void setColor(int color) {
        entityData.set(COLOR, color & 0xFFFFFF);
    }

    /** The water's blue with a little of {@code casterColor} mixed in: the glow of the blade and of its splash. */
    public static int tint(int casterColor) {
        int r = (int) Mth.lerp(CAD_ACCENT, WATER_COLOR >> 16 & 0xFF, casterColor >> 16 & 0xFF);
        int g = (int) Mth.lerp(CAD_ACCENT, WATER_COLOR >> 8 & 0xFF, casterColor >> 8 & 0xFF);
        int b = (int) Mth.lerp(CAD_ACCENT, WATER_COLOR & 0xFF, casterColor & 0xFF);
        return r << 16 | g << 8 | b;
    }

    @Override
    protected Item getDefaultItem() {
        return PsitweaksItems.AQUA_CUTTER_PROJECTILE.get();
    }

    /** The blade and its wake reach well past the small hitbox; keep them drawn while the hitbox is off screen. */
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(2.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && !isRemoved() && tickCount >= DROPS_FROM_TICK) {
            shedDrops();
        }
    }

    /** Drops shaken off the blade fall along the stretch it has just flown (client only). */
    private void shedDrops() {
        Vec3 motion = getDeltaMovement();
        double centerY = getY() + getBbHeight() * 0.5;
        for (int i = 0; i < 2; i++) {
            double back = random.nextDouble();
            level().addParticle(ParticleTypes.FALLING_WATER,
                    getX() - motion.x * back + random.nextGaussian() * 0.06,
                    centerY - motion.y * back + (random.nextDouble() - 0.5) * 0.7,
                    getZ() - motion.z * back + random.nextGaussian() * 0.06,
                    0.0, 0.0, 0.0);
        }
        if (random.nextBoolean()) {
            double back = 0.2 + random.nextDouble() * 0.5;
            level().addParticle(ParticleTypes.SPLASH,
                    getX() - motion.x * back, centerY - motion.y * back, getZ() - motion.z * back,
                    0.0, 0.0, 0.0);
        }
    }

    /** 術者側（術者・術者のペット・味方）と、プレイヤーに安全のときのプレイヤーはすり抜ける。 */
    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && AttackRules.canProjectileHarm(this, target,
                AttackRules.Options.of(getPersistentData().getBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS)));
    }
}
