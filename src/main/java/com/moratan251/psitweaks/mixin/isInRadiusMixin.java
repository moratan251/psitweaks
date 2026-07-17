package com.moratan251.psitweaks.mixin;


import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import com.moratan251.psitweaks.common.items.curios.ItemInterferenceRangeExtender;
import com.moratan251.psitweaks.common.items.curios.ItemThirdEyeDevice;
import net.minecraft.world.entity.Entity;
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
    private static final double INTERFERENCE_RANGE_SQUARED = 64.0D * 64.0D;

    @Shadow
    public Player caster;

    @Shadow
    public Entity focalPoint;

    @Inject(method = "isInRadius(Lvazkii/psi/api/internal/Vector3;)Z", at = @At("HEAD"), cancellable = true)
    private void overwriteRadiusLimit(Vector3 vec, CallbackInfoReturnable<Boolean> ci) {
        if (hasThirdEyeDevice()) {
            ci.setReturnValue(true);
            return;
        }
        if (hasInterferenceRangeExtender() && focalPoint != null) {
            ci.setReturnValue(focalPoint.distanceToSqr(vec.x, vec.y, vec.z) <= INTERFERENCE_RANGE_SQUARED);
        }
    }

    @Inject(method = "isInRadius(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void overwriteRadiusLimit_DDD(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (hasThirdEyeDevice()) {
            cir.setReturnValue(true);
            return;
        }
        if (hasInterferenceRangeExtender() && focalPoint != null) {
            cir.setReturnValue(focalPoint.distanceToSqr(x, y, z) <= INTERFERENCE_RANGE_SQUARED);
        }
    }

    private boolean hasThirdEyeDevice() {
        return caster != null && CuriosSlotChecker.hasItemInMagicSlot(caster, ItemThirdEyeDevice.class);
    }

    private boolean hasInterferenceRangeExtender() {
        return caster != null && CuriosSlotChecker.hasItemInMagicSlot(caster, ItemInterferenceRangeExtender.class);
    }
}


