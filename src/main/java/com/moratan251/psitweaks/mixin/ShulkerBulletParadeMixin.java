package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Trick: Parade: shulker bullets skip entities that pass through blocks, which the image does; without this a shulker
 * that targets the image keeps firing bullets that fly through it. They hit the image like arrows do, except the shots
 * of the image's owner and their allies, which still pass ({@code ProjectileParadeMixin}).
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletParadeMixin {
    @Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
    private void psitweaks$hitParadeImage(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && target instanceof EntityParadeDecoy decoy && decoy.canBeHitByProjectile()
                && !decoy.isFriendly(((ShulkerBullet) (Object) this).getOwner())) {
            cir.setReturnValue(true);
        }
    }
}
