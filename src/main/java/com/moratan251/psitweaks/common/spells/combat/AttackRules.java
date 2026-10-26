package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import vazkii.psi.api.capability.PsiCapabilities;
import vazkii.psi.api.spell.ISpellImmune;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * The one rule every attack trick follows: who it may hit ({@link #canHarm}, also for picking targets), how hard
 * ({@link #scale}) and how ({@link #harm}). An attack trick never calls {@code hurt} itself; it deals damage through
 * {@link #harm} or {@link #harmByProjectile}, which apply the rule first. See {@code docs/1.21.1/development-guidelines.md}
 * 「攻撃術式の命中判定」.
 */
public final class AttackRules {
    private AttackRules() {
    }

    /**
     * How an attack was cast. {@code safeToPlayers} is the Safe to Players option: on while the server's PvP is off
     * ({@link SpellSafetyUtils#hasSafeToPlayers}), and then no player is hit.
     */
    public record Options(boolean safeToPlayers) {
        public static final Options DEFAULT = new Options(false);

        public static Options from(SpellContext context) {
            return of(SpellSafetyUtils.hasSafeToPlayers(context));
        }

        public static Options of(boolean safeToPlayers) {
            return safeToPlayers ? new Options(true) : DEFAULT;
        }
    }

    // ---- who may be hit ----

    /**
     * Whether an attack of this caster may hit the entity, close to Ars Nouveau's rule: spectators, armor stands, the
     * caster, the caster's pets and the caster's allies (the same scoreboard team whatever its friendly fire, and the
     * team mates' pets) are never hit. Players are never hit with the Safe to Players option (PvP off); otherwise the
     * player decides like a vanilla hit ({@link Player#canHarmPlayer}: PvP and the player's team). An autonomous caster
     * has its machine owner's UUID and team, so it spares the owner, the owner's pets and the owner's team mates too.
     */
    public static boolean canHarm(@Nullable LivingEntity caster, @Nullable Entity entity, Options options) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isSpectator()) {
            return false;
        }
        if (living instanceof ArmorStand) {
            return false;
        }
        if (caster != null && (isCasterOrTheirPet(caster, living) || isAlly(caster, living))) {
            return false;
        }
        if (living instanceof Player player) {
            if (options.safeToPlayers() || player.isCreative()) {
                return false;
            }
            if (caster instanceof Player casterPlayer) {
                return player.canHarmPlayer(casterPlayer);
            }
            // A caster that is gone (logged out, unloaded) only hits players where PvP is on.
            return player.getServer() != null && player.getServer().isPvpAllowed();
        }
        return true;
    }

    /**
     * For an attack trick's projectile ({@code canHitEntity}): creatures are hit by {@link #canHarm} as the caster who
     * shot it, so the caster's side lets it pass instead of only the moment after the shot; other entities as vanilla.
     * The client keeps vanilla's rule, as it does not know the Safe to Players option.
     */
    public static boolean canProjectileHarm(Projectile projectile, Entity target, Options options) {
        if (projectile.level().isClientSide || !(target instanceof LivingEntity)) {
            return true;
        }
        return canHarm(projectile.getOwner() instanceof LivingEntity owner ? owner : null, target, options);
    }

    /**
     * Whether the entity is on the caster's side: the same scoreboard team (whatever its friendly fire), or allied to
     * the caster by its own rule (a tamed pet whose owner is the caster or on the caster's team, another mod's summon).
     */
    public static boolean isAlly(LivingEntity caster, Entity entity) {
        return caster.isAlliedTo(entity) || entity.isAlliedTo(caster);
    }

    /**
     * Whether the entity is the caster or one of the caster's pets, which a caster's own attacks never hit. Matched by
     * UUID: an autonomous caster has its machine owner's UUID, so the owner and the owner's pets count too.
     */
    public static boolean isCasterOrTheirPet(@Nullable LivingEntity caster, @Nullable Entity entity) {
        if (caster == null || entity == null) {
            return false;
        }
        if (entity == caster || entity.getUUID().equals(caster.getUUID())) {
            return true;
        }
        return entity instanceof OwnableEntity ownable && caster.getUUID().equals(ownable.getOwnerUUID());
    }

    // ---- picking targets ----

    /**
     * Targets that auto-aimed attacks (chain jumps, dancing blades) may pick by themselves: monsters, mobs that
     * target or are angry at the caster, and anything that recently hurt the caster. Entities that opt out of
     * spells are skipped ({@link #isSpellImmune}); bosses are fair game.
     */
    public static boolean isHostileTarget(@Nullable LivingEntity caster, @Nullable Entity entity, Options options) {
        if (!canHarm(caster, entity, options) || isSpellImmune(entity)) {
            return false;
        }
        if (entity instanceof Enemy) {
            return true;
        }
        if (caster == null) {
            return false;
        }
        if (entity instanceof Mob mob && mob.getTarget() == caster) {
            return true;
        }
        if (entity instanceof NeutralMob neutral && caster.getUUID().equals(neutral.getPersistentAngerTarget())) {
            return true;
        }
        return caster.getLastHurtByMob() == entity;
    }

    /**
     * What an auto-targeting trick in indiscriminate mode may pick: any creature it may harm ({@link #canHarm}, so
     * never the caster, allies, the caster's pets, armour stands, creative players, or players spared by Safe to
     * Players) that is not immune to spells. Villagers and wandering traders are fair game.
     */
    public static boolean isIndiscriminateTarget(@Nullable LivingEntity caster, @Nullable Entity entity, Options options) {
        return canHarm(caster, entity, options) && !isSpellImmune(entity);
    }

    /** {@link #isIndiscriminateTarget} when {@code indiscriminate}, else {@link #isHostileTarget}. */
    public static boolean isAutoTarget(@Nullable LivingEntity caster, @Nullable Entity entity, Options options,
                                       boolean indiscriminate) {
        return indiscriminate ? isIndiscriminateTarget(caster, entity, options) : isHostileTarget(caster, entity, options);
    }

    /** Whether an optional Indiscriminate input was given a non-zero number (an unconnected input reads as null). */
    public static boolean isIndiscriminate(@Nullable Number value) {
        return value != null && value.doubleValue() != 0.0;
    }

    /**
     * Whether the target has already fallen (health 0, playing its death animation) or is gone. Psi's
     * {@code verifyEntity} reports such a target as immune and stops the spell, which only gets in the way when a
     * spell keeps aiming at a creature it has just killed, so the targeted combat tricks skip it instead.
     */
    public static boolean hasFallen(@Nullable Entity entity) {
        return entity != null && !entity.isAlive();
    }

    /**
     * Skips a fallen target (see {@link #hasFallen}): the spell goes on, and the caster gets a {@link SpellWarning} so
     * the trick does not look like it misfired for no reason.
     *
     * @return whether the trick should do nothing because the target has fallen
     */
    public static boolean skipFallenTarget(SpellContext context, @Nullable Entity target) {
        if (!hasFallen(target)) {
            return false;
        }
        SpellWarning.warn(context, SpellWarning.TARGET_FALLEN);
        return true;
    }

    /**
     * Whether the entity opted out of spells through Psi's spell-immune capability (a mod's own boss, for example).
     * Unlike {@link ISpellImmune#isImmune}, bosses, riders and sleepers are not immune here: Psi rules them out because
     * they cannot use a portal, which suits tricks that move or rewrite entities but leaves attack spells powerless
     * against the Wither and the Ender Dragon.
     */
    public static boolean isSpellImmune(Entity entity) {
        ISpellImmune immune = PsiCapabilities.spellImmune(entity);
        return immune != null && immune.isImmune();
    }

    /**
     * Psi's {@code verifyEntity} for attack tricks: the target must exist, must not opt out of spells and must not be
     * hiding behind a Trick: Parade lookalike from this caster.
     */
    public static void verifyTarget(SpellContext context, @Nullable Entity entity) throws SpellRuntimeException {
        if (entity == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (isSpellImmune(entity)) {
            throw new SpellRuntimeException(SpellRuntimeException.IMMUNE_TARGET);
        }
        if (Parades.decoyFacing(entity, context.caster) != null) {
            throw new SpellRuntimeException(Parades.ERROR_DISGUISED);
        }
    }

    // ---- damage ----

    /** Base damage x per-spell config multiplier x global multiplier x the caster's spell damage factor (事象干渉力). */
    public static float scale(double baseDamage, double perSpellMultiplier, @Nullable LivingEntity caster) {
        double global = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        double factor = spellDamageFactor(caster);
        return (float) (baseDamage * perSpellMultiplier * global * factor);
    }

    /**
     * Per-spell config multiplier x global multiplier x the caster's spell damage factor, for a trick that scales a
     * damage it computes later. {@link #scale} is the usual way.
     */
    public static double multiplier(double perSpellMultiplier, @Nullable LivingEntity caster) {
        return perSpellMultiplier * PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get() * spellDamageFactor(caster);
    }

    private static double spellDamageFactor(@Nullable LivingEntity caster) {
        return caster != null ? caster.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR) : 1.0;
    }

    /** A damage source of a Psitweaks damage type: {@code directEntity} hit, on behalf of {@code attacker}. */
    public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity directEntity,
                                      @Nullable Entity attacker) {
        Holder<DamageType> type = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
        return new DamageSource(type, directEntity, attacker);
    }

    /**
     * Deals an attack's damage, if {@link #canHarm} allows it. Give the source an attacker (usually the caster) so that
     * the kill, the drops and the target's own PvP rule count it as the caster's attack.
     *
     * @return whether the target took the damage
     */
    public static boolean harm(@Nullable LivingEntity caster, Entity target, Options options, DamageSource source, float amount) {
        return canHarm(caster, target, options) && target.hurt(source, amount);
    }

    /** {@link #harm} for an attack trick's projectile hitting the entity, by {@link #canProjectileHarm}. */
    public static boolean harmByProjectile(Projectile projectile, Entity target, Options options, DamageSource source,
                                           float amount) {
        return canProjectileHarm(projectile, target, options) && target.hurt(source, amount);
    }
}
