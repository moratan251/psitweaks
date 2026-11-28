package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.SpellGram.EntityParadeCore;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import vazkii.psi.common.spell.operator.entity.PieceOperatorFocusedEntity;

/**
 * Trick: Parade: the core looks out from its image's eyes, so Operator: Focused Entity on the core skips the image it
 * stands in, as a player's own body never counts for their look. Psi's lookup otherwise picks any entity whose box holds
 * the starting point.
 */
@Mixin(value = PieceOperatorFocusedEntity.class, remap = false)
public abstract class PieceOperatorFocusedEntityParadeMixin {
    @ModifyArg(method = "getEntityLookedAt", index = 4, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;"
                    + "Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;"
                    + "Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"))
    private static Predicate<Entity> psitweaks$coreLooksPastItsImage(Entity looker, Vec3 start, Vec3 end, AABB area,
                                                                     Predicate<Entity> filter, double distance) {
        return looker instanceof EntityParadeCore core ? filter.and(entity -> !core.isImage(entity)) : filter;
    }
}
