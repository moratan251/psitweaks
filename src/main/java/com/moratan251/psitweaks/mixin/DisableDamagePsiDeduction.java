package com.moratan251.psitweaks.mixin;



import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.common.core.handler.PlayerDataHandler;

@Mixin(value = PlayerDataHandler.PlayerData.class, remap = false)
public abstract class DisableDamagePsiDeduction {

    @Inject(method = "damage(F)V", at = @At("HEAD"), cancellable = true)
    private void cancelDamage(float amount, CallbackInfo ci) {

        ci.cancel();
    }
}
