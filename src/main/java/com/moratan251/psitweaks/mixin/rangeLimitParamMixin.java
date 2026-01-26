package com.moratan251.psitweaks.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellHelpers;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

@Mixin(value = SpellHelpers.class, remap = false)
public abstract class rangeLimitParamMixin {

    @ModifyVariable(method = "rangeLimitParam", at = @At("HEAD"), index = 3, argsOnly = true)
    private static double overwriteRangeLimit(double fix) {
        return 256.0;
    }
}


