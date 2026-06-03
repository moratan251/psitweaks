package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.api.value.BlockValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.param.ParamVector;

@Mixin(value = SpellParam.class, remap = false)
public abstract class SpellParamBlockVectorCompatMixin {
    @Shadow
    protected abstract Class<?> getRequiredType();

    @Shadow
    protected abstract boolean requiresConstant();

    @Inject(method = "canAccept", at = @At("RETURN"), cancellable = true)
    private void psitweaks$acceptBlockAsVector(SpellPiece piece, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || piece == null) {
            return;
        }
        if (requiresConstant() && piece.getPieceType() != EnumPieceType.CONSTANT) {
            return;
        }

        Class<?> requiredType = getRequiredType();
        Class<?> evaluationType = piece.getEvaluationType();
        if ((Object) this instanceof ParamVector
                && requiredType == Vector3.class
                && evaluationType != null
                && BlockValue.class.isAssignableFrom(evaluationType)) {
            cir.setReturnValue(true);
        }
    }
}
