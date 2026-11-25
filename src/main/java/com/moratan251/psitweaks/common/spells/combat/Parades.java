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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

/**
 * Trick: Parade, after the Kudou family's secret magic: an image a few blocks in front of or behind the caster carries
 * the caster's "coordinates", so enemies go for it instead. While it stands, mobs that go for the caster target it,
 * melee and ranged alike, attacks that still reach the caster miss by chance, and other players' spells that name the
 * caster directly act on the image and achieve nothing; Operator: Entity Position reports where the image is, and
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
     * The core's integrity per Power at an Interference of 1: two attacks' worth ({@link EntityParadeCore#ATTACK_WEAR})
     * when just cast. It wears away over Time like other Resident SpellGrams.
     */
    public static final float INTEGRITY_PER_POWER = 4.0F;
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
     * one, and turns nearby mobs onto it. Its core, a Resident SpellGram, lasts {@code lifetimeTicks} with
     * {@link #integrityFor} integrity, and counts {@code placementCost} as what it cost for Trick: Extend SpellGram.
     * Null for an autonomous caster, where the trick does nothing.
     */
    @Nullable
    public static EntityParadeDecoy conjure(ServerLevel level, Player caster, double distance, int power, int lifetimeTicks,
                                            float placementCost) {
        if (caster instanceof AutonomousCasterPlayer) {
            return null;
        }
        EntityParadeDecoy old = activeDecoy(caster);
        if (old != null) {
            old.vanish(false);
        }
        SpellGramLimits.makeRoom(level, caster);
        EntityParadeDecoy decoy = new EntityParadeDecoy(level, imagePosition(caster, distance));
        decoy.configure(caster, distance, CombatSpellHelper.spellColor(caster));
        EntityParadeCore core = new EntityParadeCore(level);
        core.setCaster(caster);
        core.setMaxIntegrity(integrityFor(caster, power));
        core.setLifetimeTicks(lifetimeTicks);
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

    /**
     * Where the image of {@code owner} stands: {@code distance} blocks along the way the owner looks, on the horizontal
     * plane (behind for a negative distance), at the height of the owner's feet.
     */
    public static Vec3 imagePosition(Player owner, double distance) {
        return owner.position().add(horizontalOffset(owner.getYRot(), distance));
    }

    /** {@code distance} blocks along the horizontal direction of {@code yaw}, as {@link Entity#getLookAngle} turns it. */
    public static Vec3 horizontalOffset(float yaw, double distance) {
        float radians = yaw * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(radians) * distance, 0.0, Mth.cos(radians) * distance);
    }

    /** The core's integrity when cast: {@link #INTEGRITY_PER_POWER} x Power x the caster's Interference (and the config's multiplier). */
    public static float integrityFor(@Nullable LivingEntity caster, int power) {
        return SpellGramIntegrity.initial(INTEGRITY_PER_POWER, caster, Math.max(1, power));
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
     * anger, for example).
     */
    public static void lureFrom(ServerLevel level, Player owner, EntityParadeDecoy decoy) {
        AABB around = decoy.getBoundingBox();
        if (owner.level() == level) {
            around = around.minmax(owner.getBoundingBox());
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, around.inflate(LURE_RANGE), Parades::canBeLured)) {
            if (mob.getTarget() == owner) {
                mob.setTarget(decoy);
            }
            // Brain-driven mobs (piglins, wardens) keep their target in memory; asking for an unregistered memory throws.
            if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                    && mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == owner) {
                mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, decoy);
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

    /** Whether the mob may be turned onto an image; bosses keep their own targeting. */
    public static boolean canBeLured(Mob mob) {
        return !mob.getType().is(Tags.EntityTypes.BOSSES);
    }

    /** The image a mob should go for instead of {@code newTarget}, or null to leave the target alone. */
    @Nullable
    public static EntityParadeDecoy lureTarget(LivingEntity attacker, @Nullable LivingEntity newTarget) {
        if (!(attacker instanceof Mob mob) || !canBeLured(mob)) {
            return null;
        }
        EntityParadeDecoy decoy = activeDecoy(newTarget);
        double range = LURE_RANGE * LURE_RANGE;
        if (decoy == null || decoy.level() != mob.level()
                || (mob.distanceToSqr(decoy) > range && mob.distanceToSqr(newTarget) > range)) {
            return null;
        }
        return decoy;
    }
}
