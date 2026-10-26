package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.spells.combat.AttackCaster;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Base of the short-lived entities the combat tricks spawn (shockwaves, blades, meteors, gravity wells).
 * They have no collision, no gravity, are never saved, and remember who cast them ({@link AttackCaster}). A subclass
 * picks its targets with {@link #canAffect} (or {@link AttackRules}) and hurts them only through {@link #harm}.
 */
public abstract class CombatSpellEntity extends Entity {
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(CombatSpellEntity.class, EntityDataSerializers.INT);

    /** The caster as it was when cast; also finds casters that are not in the world (fake players). */
    private final AttackCaster caster = AttackCaster.byReference();
    private float damage;

    protected CombatSpellEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    /** Sets who cast it and how ({@link AttackRules.Options#from}), the damage per hit and the colour of its glow. */
    public void configure(LivingEntity caster, AttackRules.Options options, float damage, int color) {
        this.caster.set(caster, options);
        this.damage = damage;
        entityData.set(COLOR, color & 0xFFFFFF);
    }

    /** @deprecated use {@link #configure(LivingEntity, AttackRules.Options, float, int)}; kept for the existing GameTests. */
    @Deprecated
    public void configure(LivingEntity caster, boolean safeToPlayers, float damage, int color) {
        configure(caster, AttackRules.Options.of(safeToPlayers), damage, color);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLOR, CombatSpellHelper.DEFAULT_SPELL_COLOR);
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    @Nullable
    protected LivingEntity getCaster() {
        return caster.get(level());
    }

    protected AttackRules.Options options() {
        return caster.options();
    }

    /** Whether an attack of {@code caster} (this entity's, looked up once by the caller) may hit the entity. */
    protected boolean canAffect(@Nullable LivingEntity caster, @Nullable Entity target) {
        return AttackRules.canHarm(caster, target, options());
    }

    /** Hurts the entity as an attack of {@code caster} ({@link AttackRules#harm}). */
    protected boolean harm(@Nullable LivingEntity caster, Entity target, DamageSource source, float amount) {
        return AttackRules.harm(caster, target, options(), source, amount);
    }

    protected float getDamage() {
        return damage;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0 * 128.0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        caster.load(tag, "Caster");
        damage = tag.getFloat("Damage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        caster.save(tag, "Caster");
        tag.putFloat("Damage", damage);
    }
}
