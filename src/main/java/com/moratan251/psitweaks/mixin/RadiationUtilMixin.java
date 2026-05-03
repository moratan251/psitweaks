package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import mekanism.common.lib.radiation.RadiationUtil;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RadiationUtil.class, remap = false)
public abstract class RadiationUtilMixin {
    @Inject(method = "getRadiationResistance", at = @At("RETURN"), cancellable = true, require = 0)
    private static void psitweaks$applyRadiationFilterResistance(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        if (entity.hasEffect(PsitweaksEffects.RADIATION_FILTER)) {
            cir.setReturnValue(1.0D);
        }
    }
}
