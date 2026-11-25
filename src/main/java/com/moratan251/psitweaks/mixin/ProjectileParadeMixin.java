package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Trick: Parade: projectiles of the image's owner and their allies (arrows, Psi's spell projectiles) fly through the
 * image, so an image in front of the caster never stops their own shots.
 */
@Mixin(Projectile.class)
public abstract class ProjectileParadeMixin {
    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private void psitweaks$passOwnParadeImage(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof EntityParadeDecoy decoy && decoy.isFriendly(((Projectile) (Object) this).getOwner())) {
            cir.setReturnValue(false);
        }
    }
}
