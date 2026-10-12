package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.common.item.ItemCAD;

/** Psi の FakePlayer 拒否のうち、稼働中の自律詠唱機専用 Caster だけを通す。 */
@Mixin(value = ItemCAD.class, remap = false)
public abstract class ItemCADAutonomousCasterMixin {
    @Inject(method = "isTruePlayer", at = @At("HEAD"), cancellable = true)
    private static void psitweaks$allowAutonomousCaster(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AutonomousCasterPlayer caster && caster.isAttached()) {
            cir.setReturnValue(true);
        }
    }
}
