package com.moratan251.psitweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vazkii.psi.api.spell.SpellHelpers;

@Mixin(value = SpellHelpers.class, remap = false)
public abstract class SpellHelpersRangeLimitMixin {
    @ModifyVariable(method = "rangeLimitParam", at = @At("HEAD"), index = 3, argsOnly = true)
    private static double psitweaks$extendRangeLimit(double defaultLimit) {
        return 256.0D;
    }
}

