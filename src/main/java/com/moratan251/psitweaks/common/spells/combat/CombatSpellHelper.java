package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.attributes.PsitweaksAttributes;
import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.ISpellImmune;
import vazkii.psi.api.spell.SpellContext;

/** Rules shared by the combat tricks: damage scaling, who may be hit, damage sources and small physics helpers. */
public final class CombatSpellHelper {
    /** Psi's default spell colour, used when the caster has no CAD. */
    public static final int DEFAULT_SPELL_COLOR = 0x13C5FF;

    private CombatSpellHelper() {
    }

    /** Base damage x per-spell config multiplier x global multiplier x the caster's spell damage factor. */
    public static float scaleDamage(double baseDamage, double perSpellMultiplier, @Nullable LivingEntity caster) {
        double global = PsitweaksConfig.COMMON.globalSpellPowerMultiplier.get();
        double factor = caster != null ? caster.getAttributeValue(PsitweaksAttributes.SPELL_DAMAGE_FACTOR) : 1.0;
        return (float) (baseDamage * perSpellMultiplier * global * factor);
    }

    /**
     * Whether a combat trick of this caster may damage the entity at all: the caster, spectators, armor stands,
     * the caster's own pets and team mates are never hit; players only when PvP is allowed (and never with the
     * Safe to Players option).
     */
    public static boolean canHarm(@Nullable LivingEntity caster, @Nullable Entity entity, boolean safeToPlayers) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isSpectator()) {
            return false;
        }
        if (living instanceof ArmorStand) {
            return false;
        }
        if (caster != null) {
            if (living == caster || living.getUUID().equals(caster.getUUID())) {
                return false;
            }
            if (caster.isAlliedTo(living)) {
                return false;
            }
            if (living instanceof OwnableEntity ownable) {
                UUID owner = ownable.getOwnerUUID();
                if (owner != null && owner.equals(caster.getUUID())) {
                    return false;
                }
            }
        }
        if (living instanceof Player player) {
            if (safeToPlayers || player.isCreative()) {
                return false;
            }
            if (caster instanceof Player casterPlayer) {
                return casterPlayer.canHarmPlayer(player);
            }
            // A caster that is gone (logged out, unloaded) only hits players where PvP is on.
            return caster != null || (player.getServer() != null && player.getServer().isPvpAllowed());
        }
        return true;
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
     * Targets that auto-aimed attacks (chain jumps, dancing blades) may pick by themselves: monsters, mobs that
     * target or are angry at the caster, and anything that recently hurt the caster. Spell-immune entities
     * (bosses) are skipped, as Psi skips them for targeted tricks.
     */
    public static boolean isHostileTarget(@Nullable LivingEntity caster, @Nullable Entity entity, boolean safeToPlayers) {
        if (!canHarm(caster, entity, safeToPlayers) || ISpellImmune.isImmune(entity)) {
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

    public static DamageSource damageSource(Level level, ResourceKey<DamageType> key,
                                            @Nullable Entity directEntity, @Nullable Entity attacker) {
        Holder<DamageType> type = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
        return new DamageSource(type, directEntity, attacker);
    }

    /** The colour of the caster's CAD colorizer, or Psi's default colour. */
    public static int spellColor(@Nullable Entity caster) {
        if (caster instanceof Player player) {
            ItemStack cad = PsiAPI.getPlayerCAD(player);
            if (!cad.isEmpty() && cad.getItem() instanceof ICAD icad) {
                return icad.getSpellColor(cad) & 0xFFFFFF;
            }
        }
        return DEFAULT_SPELL_COLOR;
    }

    @Nullable
    public static LivingEntity findLiving(Level level, @Nullable UUID uuid) {
        if (uuid == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(uuid);
        if (entity instanceof LivingEntity living) {
            return living;
        }
        Player player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
        return player != null && player.level() == level ? player : null;
    }

    /**
     * The height of the top face of the first block with a collision shape at or below the position,
     * searching at most {@code maxDepth} blocks down. Null when there is no ground in that range.
     */
    @Nullable
    public static Double findGroundY(Level level, double x, double y, double z, int maxDepth) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        for (int i = 0; i <= maxDepth; i++) {
            if (level.isOutsideBuildHeight(pos)) {
                return null;
            }
            VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
            if (!shape.isEmpty()) {
                return pos.getY() + shape.max(Direction.Axis.Y);
            }
            pos.move(Direction.DOWN);
        }
        return null;
    }

    /** Adds velocity scaled down by knockback resistance and makes sure players receive it. */
    public static void push(Entity entity, Vec3 velocity) {
        double scale = 1.0;
        if (entity instanceof LivingEntity living) {
            scale = Mth.clamp(1.0 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
        }
        if (scale <= 0.0) {
            return;
        }
        entity.setDeltaMovement(entity.getDeltaMovement().add(velocity.scale(scale)));
        entity.hasImpulse = true;
        entity.hurtMarked = true;
    }

    public static Vec3 center(Entity entity) {
        return entity.getBoundingBox().getCenter();
    }

    /** Roughly where the caster's main hand is: the point effects such as lightning start from. */
    public static Vec3 castPoint(LivingEntity caster) {
        Vec3 look = caster.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        right = right.lengthSqr() < 1.0E-6 ? Vec3.ZERO : right.normalize();
        double side = caster.getMainArm() == HumanoidArm.RIGHT ? 0.35 : -0.35;
        return caster.getEyePosition().add(look.scale(0.6)).add(right.scale(side)).add(0.0, -0.3, 0.0);
    }

    /** Whether nothing solid blocks the straight line between the two points. */
    public static boolean canSee(Level level, Vec3 from, Vec3 to, Entity viewer) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer))
                .getType() == HitResult.Type.MISS;
    }

    /** Linear falloff from full damage at the centre to {@code edgeFactor} at the radius. */
    public static float falloff(float damage, double distance, double radius, double edgeFactor) {
        if (radius <= 0.0) {
            return damage;
        }
        double normalized = Mth.clamp(distance / radius, 0.0, 1.0);
        return (float) (damage * (1.0 - (1.0 - edgeFactor) * normalized));
    }
}
