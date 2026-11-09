package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.spell.trick.entity.PieceTrickAddMotion;

/**
 * 作動式: 運動追加(単) と、それを使う運動追加(複)・大移動で、魔法式オブジェクトは置いた術者のものも含めて動かさない。
 * 複数を動かす作動式の中の 1 つで術式全体を止めないよう、エラーにはせず飛ばす。
 */
@Mixin(value = PieceTrickAddMotion.class, remap = false)
public abstract class PieceTrickAddMotionSpellGramMixin {
    @Inject(method = "addMotion(Lvazkii/psi/api/spell/SpellContext;Lnet/minecraft/world/entity/Entity;Lvazkii/psi/api/internal/Vector3;DZ)V",
            at = @At("HEAD"), cancellable = true)
    private static void psitweaks$keepSpellGramsInPlace(SpellContext context, Entity target, Vector3 direction, double speed,
                                                       boolean predicted, CallbackInfo ci) {
        if (target instanceof SpellGramObject) {
            ci.cancel();
        }
    }
}
