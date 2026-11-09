package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.spell.trick.entity.PieceTrickBlink;

/**
 * 作動式: 瞬間移動(単)・瞬間移動(全) で、魔法式オブジェクトは置いた術者のものも含めて動かさない。
 * 複数を動かす作動式の中の 1 つで術式全体を止めないよう、エラーにはせず飛ばす。
 */
@Mixin(value = PieceTrickBlink.class, remap = false)
public abstract class PieceTrickBlinkSpellGramMixin {
    @Inject(method = "blink(Lvazkii/psi/api/spell/SpellContext;Lnet/minecraft/world/entity/Entity;D)V",
            at = @At("HEAD"), cancellable = true)
    private static void psitweaks$keepSpellGramsInPlace(SpellContext context, Entity target, double distance, CallbackInfo ci) {
        if (target instanceof SpellGramObject) {
            ci.cancel();
        }
    }
}
