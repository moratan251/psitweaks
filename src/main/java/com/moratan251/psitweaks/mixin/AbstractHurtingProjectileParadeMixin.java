package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Trick: Parade: fireball-like projectiles (the Ender Dragon's fireball, Wither skulls, ghast and blaze fireballs, wind
 * charges) skip entities that pass through blocks, which the image does; without this they would fly through the image
 * they were aimed at and burst on whatever lies behind it. They hit the image like arrows do, except the shots of the
 * image's owner and their allies, which still pass ({@code ProjectileParadeMixin}).
 */
@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileParadeMixin {
    @Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
    private void psitweaks$hitParadeImage(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && target instanceof EntityParadeDecoy decoy && decoy.canBeHitByProjectile()
                && !decoy.isFriendly(((AbstractHurtingProjectile) (Object) this).getOwner())) {
            cir.setReturnValue(true);
        }
    }
}
