package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.entities.SpellGram.EntityParadeCore;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIntegrity;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramLimits;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Trick: Parade, after the Kudou family's secret magic: an image a few blocks in front of or behind the caster carries
 * the caster's "coordinates", so enemies go for it instead. While it stands, mobs that go for the caster target it,
 * melee and ranged alike, bosses included; attacks that still reach the caster miss by chance, and other players'
 * spells that name the caster directly act on the image and achieve nothing; Operator: Entity Position reports where
 * the image is, and
 * other players no longer see the caster at all. The caster and their team mates are unaffected.
 * The image's time and integrity are a Resident SpellGram's ({@link EntityParadeCore}), so it can be extended,
 * recalled and dispelled like one, but other players' Interference Zones do not stop it.
 * An autonomous caster has no body to disguise, so the trick does nothing there.
 */
public final class Parades {
    public static final String ERROR_DISGUISED = "psitweaks.spellerror.parade_disguised";
    /** Mobs this close to the image or its owner are drawn to the image instead of the owner. */
    public static final double LURE_RANGE = 32.0;
    /**
     * The core's integrity per second of Time at an Interference of 1, the same as Zone Magic's: integrity is the time
     * left, and each attack ({@link EntityParadeCore#ATTACK_WEAR}) takes two seconds of it. Interference raises the
     * integrity but not the time: it wears away over Time like other Resident SpellGrams.
     */
    public static final float INTEGRITY_PER_SECOND = 1.0F;
    /**
     * The chance that an attack on the caster misses: the base, plus this much per block between the caster and the
     * image (the farther the false coordinates, the wider attacks go), up to the cap.
     */
    public static final double BASE_EVASION = 0.60;
    public static final double EVASION_PER_BLOCK = 0.05;
    public static final double MAX_EVASION = 0.95;

    private Parades() {
    }

    /**
     * Projects the caster's image {@code distance} blocks in front of them (behind if negative), replacing an earlier
     * one, and turns nearby mobs onto it. Its core, a Resident SpellGram, lasts {@code seconds} with
     * {@link #integrityFor} integrity, and counts {@code placementCost} as what it cost for Trick: Extend SpellGram.
     * Null for an autonomous caster, where the trick does nothing.
     */
    @Nullable
    public static EntityParadeDecoy conjure(ServerLevel level, Player caster, double distance, int seconds, float placementCost) {
        return conjure(level, caster, distance, seconds, placementCost, null);
    }

    /**
     * As above; a {@code fixedDirection} (a unit vector) keeps the image {@code distance} blocks along it instead of
     * turning with the caster's view ({@link EntityParadeDecoy#isDirectionFixed}).
     */
    public static EntityParadeDecoy conjure(ServerLevel level, Player caster, double distance, int seconds, float placementCost,
                                            @Nullable Vec3 fixedDirection) {
        if (caster instanceof AutonomousCasterPlayer) {
            return null;
        }
        EntityParadeDecoy old = activeDecoy(caster);
        if (old != null) {
            old.vanish(false);
        }
        SpellGramLimits.makeRoom(level, caster);
        EntityParadeDecoy decoy = new EntityParadeDecoy(level, caster.position().add(fixedDirection != null
                ? fixedDirection.scale(distance) : horizontalOffset(caster.getYRot(), distance)));
        decoy.configure(caster, distance, CombatSpellHelper.spellColor(caster), fixedDirection);
        EntityParadeCore core = new EntityParadeCore(level);
        core.setCaster(caster);
        core.setMaxIntegrity(integrityFor(caster, seconds));
        core.setLifetimeTicks(Math.max(1, seconds) * 20);
        core.setPlacementCost(placementCost);
        core.bind(decoy);
        decoy.bindCore(core);
        level.addFreshEntity(decoy);
        level.addFreshEntity(core);
        caster.setData(PsitweaksAttachments.PARADE_DECOY, decoy.getId());

        lureFrom(level, caster, decoy);

        Vec3 center = decoy.getBoundingBox().getCenter();
        level.sendParticles(ParticleTypes.ENCHANT, center.x, center.y, center.z, 40, 0.3, 0.8, 0.3, 1.0);
        level.sendParticles(ParticleTypes.ENCHANT, caster.getX(), caster.getY() + 1.0, caster.getZ(), 20, 0.3, 0.6, 0.3, 0.6);
        CombatEffects.sound(level, center, SoundEvents.ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.2F);
        return decoy;
    }

    /** {@code distance} blocks along the horizontal direction of {@code yaw}, as {@link Entity#getLookAngle} turns it. */
    public static Vec3 horizontalOffset(float yaw, double distance) {
        float radians = yaw * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(radians) * distance, 0.0, Mth.cos(radians) * distance);
    }

    /**
     * The core's integrity when cast: {@link #INTEGRITY_PER_SECOND} x Time x the caster's Interference (and the
     * config's multiplier).
     */
    public static float integrityFor(@Nullable LivingEntity caster, int seconds) {
        return SpellGramIntegrity.initial(INTEGRITY_PER_SECOND, caster, Math.max(1, seconds));
    }

    /** The chance that an attack on the caster misses while their image stands {@code distance} blocks away. */
    public static double evasionChance(double distance) {
        return Math.min(MAX_EVASION, BASE_EVASION + EVASION_PER_BLOCK * Math.abs(distance));
    }

    /**
     * Whether an attack on {@code target} misses because of their image: it must come from someone (a creature, a
     * player, a projectile, a spell), not from the terrain, a fall or the void, and {@code roll} (0..1) must fall under
     * the image's {@link #evasionChance}. Server side.
     */
    public static boolean evades(LivingEntity target, DamageSource source, double roll) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || (source.getEntity() == null && source.getDirectEntity() == null)) {
            return false;
        }
        EntityParadeDecoy decoy = activeDecoy(target);
        return decoy != null && roll < decoy.getEvasionChance();
    }

    /**
     * Turns the mobs near the image or its owner that are after the owner onto the image. Done when it appears and
     * then every so often, for mobs that pick their target without going through the target change event (a warden's
     * anger, the Wither's side heads, for example). The Ender Dragon's attacks check for the image themselves.
     */
    public static void lureFrom(ServerLevel level, Player owner, EntityParadeDecoy decoy) {
        AABB around = decoy.getBoundingBox();
        if (owner.level() == level) {
            around = around.minmax(owner.getBoundingBox());
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, around.inflate(LURE_RANGE), other -> other != decoy)) {
            if (mob.getTarget() == owner) {
                mob.setTarget(decoy);
            }
            // Brain-driven mobs (piglins, wardens) keep their target in memory; asking for an unregistered memory throws.
            if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                    && mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == owner) {
                mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, decoy);
            }
            if (mob instanceof WitherBoss wither) {
                for (int head = 0; head < 3; head++) {
                    if (wither.getAlternativeTarget(head) == owner.getId()) {
                        wither.setAlternativeTarget(head, decoy.getId());
                    }
                }
            }
        }
    }

    /** The image standing in for this entity right now, if any. Server side. */
    @Nullable
    public static EntityParadeDecoy activeDecoy(@Nullable Entity entity) {
        if (!(entity instanceof Player player)) {
            return null;
        }
        Integer id = player.getExistingDataOrNull(PsitweaksAttachments.PARADE_DECOY);
        if (id == null) {
            return null;
        }
        if (player.level().getEntity(id) instanceof EntityParadeDecoy decoy && decoy.isAlive() && decoy.isOwnedBy(player)) {
            return decoy;
        }
        player.removeData(PsitweaksAttachments.PARADE_DECOY);
        return null;
    }

    /**
     * The image that {@code target} hides behind as far as {@code caster} can tell: from anyone but the target
     * itself and its team mates, whatever the game mode or PvP setting.
     */
    @Nullable
    public static EntityParadeDecoy decoyFacing(@Nullable Entity target, @Nullable LivingEntity caster) {
        if (target == null || caster == null || target == caster || target.getUUID().equals(caster.getUUID())) {
            return null;
        }
        EntityParadeDecoy decoy = activeDecoy(target);
        if (decoy == null || caster.isAlliedTo(target)) {
            return null;
        }
        return decoy;
    }

    /**
     * The image a mob should go for instead of {@code newTarget}, or null to leave the target alone: the target's image,
     * when the mob is within {@link #LURE_RANGE} of it or of the target. Bosses are fooled like any other mob; the Ender
     * Dragon, which circles the whole island and strikes from afar, at any distance.
     */
    @Nullable
    public static EntityParadeDecoy lureTarget(LivingEntity attacker, @Nullable LivingEntity newTarget) {
        if (!(attacker instanceof Mob mob)) {
            return null;
        }
        EntityParadeDecoy decoy = activeDecoy(newTarget);
        if (decoy == null || decoy == mob || decoy.level() != mob.level()) {
            return null;
        }
        double range = LURE_RANGE * LURE_RANGE;
        if (!(mob instanceof EnderDragon) && mob.distanceToSqr(decoy) > range && mob.distanceToSqr(newTarget) > range) {
            return null;
        }
        return decoy;
    }

    /** {@code target}'s image if {@code attacker} should go for it instead ({@link #lureTarget}), else {@code target}. */
    @Nullable
    public static LivingEntity lureOrKeep(LivingEntity attacker, @Nullable LivingEntity target) {
        EntityParadeDecoy decoy = lureTarget(attacker, target);
        return decoy != null ? decoy : target;
    }
}
