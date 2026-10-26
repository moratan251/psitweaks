package com.moratan251.psitweaks.common.entities.combat;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import java.lang.ref.WeakReference;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Base of the short-lived entities the combat tricks spawn (shockwaves, blades, meteors, gravity wells).
 * They have no collision, no gravity, are never saved, and remember who cast them.
 */
public abstract class CombatSpellEntity extends Entity {
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(CombatSpellEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID casterUuid;
    /** The caster as it was when cast; also finds casters that are not in the world (fake players). */
    @Nullable
    private WeakReference<LivingEntity> casterRef;
    private boolean safeToPlayers;
    private float damage;

    protected CombatSpellEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    /** Sets who cast it, the damage per hit and the colour of its glow. */
    public void configure(LivingEntity caster, boolean safeToPlayers, float damage, int color) {
        this.casterUuid = CasterIds.idOf(caster);
        this.casterRef = new WeakReference<>(caster);
        this.safeToPlayers = safeToPlayers;
        this.damage = damage;
        entityData.set(COLOR, color & 0xFFFFFF);
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
        LivingEntity cached = casterRef != null ? casterRef.get() : null;
        if (cached != null && !cached.isRemoved() && cached.level() == level()) {
            return cached;
        }
        return CombatSpellHelper.findLiving(level(), casterUuid);
    }

    protected boolean isSafeToPlayers() {
        return safeToPlayers;
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
        if (tag.hasUUID("Caster")) {
            casterUuid = tag.getUUID("Caster");
        }
        safeToPlayers = tag.getBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS);
        damage = tag.getFloat("Damage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (casterUuid != null) {
            tag.putUUID("Caster", casterUuid);
        }
        tag.putBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS, safeToPlayers);
        tag.putFloat("Damage", damage);
    }
}
