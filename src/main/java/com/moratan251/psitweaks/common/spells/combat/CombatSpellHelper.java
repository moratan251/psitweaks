package com.moratan251.psitweaks.common.spells.combat;

import com.moratan251.psitweaks.common.caster.CasterIds;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Small helpers shared by the combat tricks: spell colour, geometry, aiming and physics. Who an attack may hit and how
 * it deals damage are in {@link AttackRules}.
 */
public final class CombatSpellHelper {
    /** Psi's default spell colour, used when the caster has no CAD. */
    public static final int DEFAULT_SPELL_COLOR = 0x13C5FF;

    private CombatSpellHelper() {
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

    /**
     * The creature under the caster's crosshair: the first living entity the caster's line of sight meets, out to Psi's
     * spell range and within {@link SpellContext#isInRadius}. Solid blocks stop the line; blocks without collision
     * (grass, flowers) and entities that are not alive in the first place (item frames, boats) do not. Null when there
     * is none.
     */
    @Nullable
    public static LivingEntity aimedCreature(SpellContext context) {
        LivingEntity caster = context.caster;
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(caster.getLookAngle().scale(SpellContext.MAX_DISTANCE));
        HitResult block = caster.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        AABB area = caster.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(caster, eye, end, area, entity -> entity instanceof LivingEntity
                && !entity.isSpectator() && entity.isPickable() && context.isInRadius(entity), eye.distanceToSqr(end));
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
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

    // ---- Deprecated: the rules moved to AttackRules. Kept so the existing GameTests stay unchanged as the behaviour
    // guard of that move; new code calls AttackRules (and CasterIds.findLiving) directly. ----

    /** @deprecated use {@link AttackRules#canHarm} */
    @Deprecated
    public static boolean canHarm(@Nullable LivingEntity caster, @Nullable Entity entity, boolean safeToPlayers) {
        return AttackRules.canHarm(caster, entity, AttackRules.Options.of(safeToPlayers));
    }

    /** @deprecated use {@link AttackRules#canProjectileHarm} */
    @Deprecated
    public static boolean canProjectileHarm(Projectile projectile, Entity target, boolean safeToPlayers) {
        return AttackRules.canProjectileHarm(projectile, target, AttackRules.Options.of(safeToPlayers));
    }

    /** @deprecated use {@link AttackRules#isHostileTarget} */
    @Deprecated
    public static boolean isHostileTarget(@Nullable LivingEntity caster, @Nullable Entity entity, boolean safeToPlayers) {
        return AttackRules.isHostileTarget(caster, entity, AttackRules.Options.of(safeToPlayers));
    }

    /** @deprecated use {@link AttackRules#isIndiscriminateTarget} */
    @Deprecated
    public static boolean isIndiscriminateTarget(@Nullable LivingEntity caster, @Nullable Entity entity, boolean safeToPlayers) {
        return AttackRules.isIndiscriminateTarget(caster, entity, AttackRules.Options.of(safeToPlayers));
    }

    /** @deprecated use {@link AttackRules#isAutoTarget} */
    @Deprecated
    public static boolean isAutoTarget(@Nullable LivingEntity caster, @Nullable Entity entity, boolean safeToPlayers,
                                       boolean indiscriminate) {
        return AttackRules.isAutoTarget(caster, entity, AttackRules.Options.of(safeToPlayers), indiscriminate);
    }

    /** @deprecated use {@link AttackRules#isIndiscriminate} */
    @Deprecated
    public static boolean isIndiscriminate(@Nullable Number value) {
        return AttackRules.isIndiscriminate(value);
    }

    /** @deprecated use {@link AttackRules#verifyTarget} */
    @Deprecated
    public static void verifyTarget(SpellContext context, @Nullable Entity entity) throws SpellRuntimeException {
        AttackRules.verifyTarget(context, entity);
    }

    /** @deprecated use {@link CasterIds#findLiving} */
    @Deprecated
    @Nullable
    public static LivingEntity findLiving(Level level, @Nullable UUID uuid) {
        return CasterIds.findLiving(level, uuid);
    }
}
