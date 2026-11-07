package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import java.lang.ref.WeakReference;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;

/**
 * Who cast an attack trick's entity (beam, circle, blade...) and with which {@link AttackRules.Options}: the entity
 * keeps one of these instead of its own caster UUID and Safe to Players flag. The caster is stored by
 * {@link CasterIds#idOf} and found again with {@link CasterIds#findLiving}, so an autonomous caster is never mistaken
 * for its owner. {@link #canAffect} is {@link AttackRules#canHarm} for this caster.
 */
public final class AttackCaster {
    private static final String SIDE_SUFFIX = "Side";
    private static final String TEAM_SUFFIX = "Team";

    private final boolean keepsReference;
    @Nullable
    private UUID id;
    @Nullable
    private WeakReference<LivingEntity> reference;
    /**
     * The caster's side as it was when cast, for when the caster cannot be found (logged out, machine removed): its
     * UUID (an autonomous caster's is its owner's) and its scoreboard team.
     */
    @Nullable
    private UUID sideUuid;
    @Nullable
    private String teamName;
    private AttackRules.Options options = AttackRules.Options.DEFAULT;

    private AttackCaster(boolean keepsReference) {
        this.keepsReference = keepsReference;
    }

    /** Finds the caster again by its stored id only, so a caster that is gone (or a removed machine) is null. */
    public static AttackCaster byId() {
        return new AttackCaster(false);
    }

    /**
     * Keeps the caster as it was when cast while it is still in the entity's level, and finds it by id after that.
     * This also follows casters that are not in the world and not registered (other mods' fake players).
     */
    public static AttackCaster byReference() {
        return new AttackCaster(true);
    }

    public void set(@Nullable LivingEntity caster) {
        this.id = caster == null ? null : CasterIds.idOf(caster);
        this.reference = keepsReference && caster != null ? new WeakReference<>(caster) : null;
        this.sideUuid = caster == null ? null : caster.getUUID();
        this.teamName = caster == null || caster.getTeam() == null ? null : caster.getTeam().getName();
    }

    public void set(@Nullable LivingEntity caster, AttackRules.Options options) {
        set(caster);
        setOptions(options);
    }

    public void setOptions(AttackRules.Options options) {
        this.options = options;
    }

    public AttackRules.Options options() {
        return options;
    }

    /** The stored caster id ({@link CasterIds#idOf}). */
    @Nullable
    public UUID id() {
        return id;
    }

    /** The caster, seen from an entity in {@code level}. Null when it cannot be found. */
    @Nullable
    public LivingEntity get(Level level) {
        LivingEntity cached = reference != null ? reference.get() : null;
        if (cached != null && !cached.isRemoved() && cached.level() == level) {
            return cached;
        }
        return CasterIds.findLiving(level, id);
    }

    /** Whether an attack of this caster may hit the entity ({@link AttackRules#canHarm}). */
    public boolean canAffect(Level level, @Nullable Entity target) {
        return canHarm(get(level), target, options);
    }

    /**
     * {@link AttackRules#canHarm} for the caster that was found ({@code found}, from {@link #get}). When the caster is
     * gone, its side as it was when cast is still spared: the same UUID, its pets, its team and its team mates' pets.
     */
    public boolean canHarm(@Nullable LivingEntity found, @Nullable Entity target, AttackRules.Options options) {
        if (found == null && isOnTheGoneCastersSide(target)) {
            return false;
        }
        return AttackRules.canHarm(found, target, options);
    }

    private boolean isOnTheGoneCastersSide(@Nullable Entity target) {
        if (target == null) {
            return false;
        }
        if (sideUuid != null && (target.getUUID().equals(sideUuid)
                || target instanceof OwnableEntity ownable && sideUuid.equals(ownable.getOwnerUUID()))) {
            return true;
        }
        if (teamName == null) {
            return false;
        }
        if (target.getTeam() != null && teamName.equals(target.getTeam().getName())) {
            return true;
        }
        return target instanceof OwnableEntity ownable && ownable.getOwner() != null && ownable.getOwner().getTeam() != null
                && teamName.equals(ownable.getOwner().getTeam().getName());
    }

    /** Reads the caster id under {@code idKey} and the Safe to Players flag. */
    public void load(CompoundTag tag, String idKey) {
        loadId(tag, idKey);
        options = AttackRules.Options.of(tag.getBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS));
    }

    /** Writes the caster id under {@code idKey} and the Safe to Players flag. */
    public void save(CompoundTag tag, String idKey) {
        saveId(tag, idKey);
        tag.putBoolean(SpellSafetyUtils.NBT_SAFE_TO_PLAYERS, options.safeToPlayers());
    }

    /** Reads only the caster id, for an entity that keeps its options elsewhere. */
    public void loadId(CompoundTag tag, String idKey) {
        id = tag.hasUUID(idKey) ? tag.getUUID(idKey) : null;
        reference = null;
        sideUuid = tag.hasUUID(idKey + SIDE_SUFFIX) ? tag.getUUID(idKey + SIDE_SUFFIX) : null;
        teamName = tag.contains(idKey + TEAM_SUFFIX) ? tag.getString(idKey + TEAM_SUFFIX) : null;
    }

    /** Writes only the caster id, for an entity that keeps its options elsewhere. */
    public void saveId(CompoundTag tag, String idKey) {
        if (id != null) {
            tag.putUUID(idKey, id);
        }
        if (sideUuid != null) {
            tag.putUUID(idKey + SIDE_SUFFIX, sideUuid);
        }
        if (teamName != null) {
            tag.putString(idKey + TEAM_SUFFIX, teamName);
        }
    }
}
