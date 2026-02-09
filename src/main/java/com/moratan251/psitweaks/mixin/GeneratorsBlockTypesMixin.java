package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import mekanism.api.math.FloatingLong;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GeneratorsBlockTypes.class, remap = false)
public abstract class GeneratorsBlockTypesMixin {

    @Inject(method = "lambda$static$14", at = @At("HEAD"), cancellable = true, require = 0)
    private static void psitweaks$overrideGasBurningGeneratorCapacity(CallbackInfoReturnable<FloatingLong> cir) {
        long overrideCapacity = PsitweaksConfig.COMMON.gasBurningGeneratorEnergyCapacity.get();
        if (overrideCapacity > 0) {
            cir.setReturnValue(FloatingLong.createConst(overrideCapacity));
        }
    }
}
