package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.trick.PieceTrickBreakLoop;

/**
 * 自律詠唱機の詠唱主体を焦点とする Trick: Break Loop を、機械のループ回数のリセットとして機械へ伝える。
 * Psi 側は詠唱した手のループ型詠唱だけを止めるので、ループ状態を持たない詠唱主体には何も起きない。
 */
@Mixin(value = PieceTrickBreakLoop.class, remap = false)
public abstract class PieceTrickBreakLoopAutonomousCasterMixin {
    @Shadow
    SpellParam<Number> valueParam;

    @Inject(method = "execute", at = @At("HEAD"))
    private void psitweaks$resetAutonomousCasterLoop(SpellContext context, CallbackInfoReturnable<Object> cir) {
        if (!(context.caster instanceof AutonomousCasterPlayer caster) || context.focalPoint != caster) {
            return;
        }
        try {
            Number value = ((SpellPiece) (Object) this).getParamValue(context, valueParam);
            if (value != null && Math.abs(value.doubleValue()) < 1.0D) {
                caster.breakLoop();
            }
        } catch (SpellRuntimeException ignored) {
            // 値が取れないときは、続く Psi 側の処理が同じ例外を投げる。
        }
    }
}
