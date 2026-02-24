package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RadiationManager.class, remap = false)
public abstract class RadiationManagerMixin {

    @Inject(method = "getRadiationResistance(Lnet/minecraft/world/entity/LivingEntity;)D", at = @At("RETURN"), cancellable = true, require = 0)
    private void psitweaks$applyRadiationFilterResistance(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        if (entity.hasEffect(PsitweaksEffects.RADIATION_FILTER.get())) {
            cir.setReturnValue(1.0D);
        }
    }
}
