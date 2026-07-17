package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.common.core.handler.PlayerDataHandler;

@Mixin(value = PlayerDataHandler.PlayerData.class, remap = false)
public abstract class DisableDamagePsiDeduction {

    @Inject(method = "damage(F)V", at = @At("HEAD"), cancellable = true)
    private void psitweaks$cancelDamagePsiDeduction(float amount, CallbackInfo ci) {
        if (PsitweaksConfig.COMMON.disableDamagePsiDeduction.get()) {
            ci.cancel();
        }
    }
}
