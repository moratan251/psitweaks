package com.moratan251.psitweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import vazkii.psi.common.core.handler.PlayerDataHandler;

@Mixin(value = PlayerDataHandler.PlayerData.class, remap = false)
public abstract class DisableRegenCooldown {

    @ModifyVariable(method = "deductPsi(IIZ)V", at = @At("HEAD"), index = 2,argsOnly = true)
    private int SetCdToZero(int cd){
        return 0;
    }

}
