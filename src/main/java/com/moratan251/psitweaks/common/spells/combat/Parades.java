package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

/**
 * Trick: Parade, after the Kudou family's secret magic: a lookalike carries the caster's "coordinates", so enemies go
 * for it instead. While it stands, hostile mobs nearby target it rather than the caster, and other players' spells
 * that name the caster directly act on the dummy and achieve nothing; Operator: Entity Position reports where the
 * lookalike is, and other players no longer see the caster at all. The caster and their team mates are unaffected.
 */
public final class Parades {
    public static final String ERROR_DISGUISED = "psitweaks.spellerror.parade_disguised";
    /** Mobs this close to the lookalike are drawn to it instead of the caster. */
    public static final double LURE_RANGE = 32.0;

    private Parades() {
    }

    /** Conjures the caster's lookalike at {@code position}, replacing an earlier one, and turns nearby mobs onto it. */
    public static EntityParadeDecoy conjure(ServerLevel level, Player caster, Vec3 position, int hits, int lifetimeTicks) {
        EntityParadeDecoy old = activeDecoy(caster);
        if (old != null) {
            old.vanish(false);
        }
        EntityParadeDecoy decoy = new EntityParadeDecoy(level, position);
        decoy.configure(caster, hits, lifetimeTicks, CombatSpellHelper.spellColor(caster));
        level.addFreshEntity(decoy);
        caster.setData(PsitweaksAttachments.PARADE_DECOY, decoy.getId());

        lureFrom(level, caster, decoy);

        Vec3 center = decoy.getBoundingBox().getCenter();
        level.sendParticles(ParticleTypes.ENCHANT, center.x, center.y, center.z, 40, 0.3, 0.8, 0.3, 1.0);
        level.sendParticles(ParticleTypes.ENCHANT, caster.getX(), caster.getY() + 1.0, caster.getZ(), 20, 0.3, 0.6, 0.3, 0.6);
        CombatEffects.sound(level, center, SoundEvents.ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.2F);
        return decoy;
    }

    /**
     * Turns the mobs near the lookalike that are after its owner onto the lookalike. Done when it appears and then
     * every so often, for mobs that pick their target without going through the target change event (a warden's
     * anger, for example).
     */
    public static void lureFrom(ServerLevel level, Player owner, EntityParadeDecoy decoy) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, decoy.getBoundingBox().inflate(LURE_RANGE), Parades::canBeLured)) {
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

    /** The lookalike standing in for this entity right now, if any. Server side. */
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
     * The lookalike that {@code target} hides behind as far as {@code caster} can tell: from anyone but the target
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

    /** Whether the mob may be turned onto a lookalike; bosses keep their own targeting. */
    public static boolean canBeLured(Mob mob) {
        return !mob.getType().is(Tags.EntityTypes.BOSSES);
    }

    /** The lookalike a mob should go for instead of {@code newTarget}, or null to leave the target alone. */
    @Nullable
    public static EntityParadeDecoy lureTarget(LivingEntity attacker, @Nullable LivingEntity newTarget) {
        if (!(attacker instanceof Mob mob) || !canBeLured(mob)) {
            return null;
        }
        EntityParadeDecoy decoy = activeDecoy(newTarget);
        if (decoy == null || decoy.level() != mob.level() || mob.distanceToSqr(decoy) > LURE_RANGE * LURE_RANGE) {
            return null;
        }
        return decoy;
    }
}
