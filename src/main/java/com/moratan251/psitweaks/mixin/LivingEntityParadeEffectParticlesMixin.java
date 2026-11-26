package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.combat.ParadeSight;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Trick: Parade: a caster hidden from the local player gives off no potion effect particles there. Vanilla only thins
 * them out for entities that are really invisible, and Parade hides the caster per viewer. The particles are the last
 * thing {@code tickEffects} does, so stopping there leaves the effect ticking alone. Client side only.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityParadeEffectParticlesMixin {
    @Inject(method = "tickEffects", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
            target = "Lnet/minecraft/world/entity/LivingEntity;DATA_EFFECT_PARTICLES:Lnet/minecraft/network/syncher/EntityDataAccessor;"))
    private void psitweaks$paradeHiddenEffectParticles(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide && ParadeSight.isHiddenFromLocalPlayer(self)) {
            ci.cancel();
        }
    }
}
