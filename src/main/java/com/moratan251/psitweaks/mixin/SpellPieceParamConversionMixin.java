package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.param.SpellParamValueConversionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;

/** Lets a Vector List input read a wired Block List as its block positions. */
@Mixin(value = SpellPiece.class, remap = false)
public abstract class SpellPieceParamConversionMixin {
    @Inject(method = "getParamValue", at = @At("RETURN"), cancellable = true)
    private void psitweaks$convertParamValues(SpellContext context, SpellParam<?> param,
            CallbackInfoReturnable<Object> cir) {
        Object value = cir.getReturnValue();
        Object converted = SpellParamValueConversionHelper.convert(param, value);
        if (converted != value) {
            cir.setReturnValue(converted);
        }
    }
}
