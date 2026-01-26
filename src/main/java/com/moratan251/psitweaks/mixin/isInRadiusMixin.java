package com.moratan251.psitweaks.mixin;


import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import com.moratan251.psitweaks.common.items.curios.ItemThirdEyeDevice;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;

@Mixin(value = SpellContext.class, remap = false)
public abstract class isInRadiusMixin {

    @Shadow
    public Player caster;

    @Inject(method = "isInRadius(Lvazkii/psi/api/internal/Vector3;)Z", at = @At("HEAD"), cancellable = true)
    private void overwriteRadiusLimit(Vector3 vec, CallbackInfoReturnable<Boolean> ci) {
        if(CuriosSlotChecker.hasItemInMagicSlot(caster, ItemThirdEyeDevice.class)) {
            ci.setReturnValue(true);
            return;
        }
    }

    @Inject(method = "isInRadius(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void overwriteRadiusLimit_DDD(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if(CuriosSlotChecker.hasItemInMagicSlot(caster, ItemThirdEyeDevice.class)) {
            cir.setReturnValue(true);
            return;
        }
    }


}


