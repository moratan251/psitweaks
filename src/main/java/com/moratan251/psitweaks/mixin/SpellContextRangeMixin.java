package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
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
public abstract class SpellContextRangeMixin {
    private static final double INTERFERENCE_RANGE = 64.0D;
    private static final double INTERFERENCE_RANGE_SQUARED = INTERFERENCE_RANGE * INTERFERENCE_RANGE;

    @Shadow
    public Player caster;

    @Shadow
    public Entity focalPoint;

    @Inject(method = "isInRadius(Lvazkii/psi/api/internal/Vector3;)Z", at = @At("HEAD"), cancellable = true)
    private void psitweaks$extendVectorRange(Vector3 vec, CallbackInfoReturnable<Boolean> cir) {
        if (vec == null) {
            return;
        }
        if (psitweaks$hasThirdEyeDevice()) {
            cir.setReturnValue(true);
            return;
        }
        Boolean interferenceRangeResult = psitweaks$isInInterferenceRange(vec.x, vec.y, vec.z);
        if (interferenceRangeResult != null) {
            cir.setReturnValue(interferenceRangeResult);
            return;
        }
        Boolean sableResult = SableRangeCompat.isInRadius(focalPoint, vec.x, vec.y, vec.z);
        if (sableResult != null) {
            cir.setReturnValue(sableResult);
        }
    }

    @Inject(method = "isInRadius(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void psitweaks$extendEntityRange(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity != null && psitweaks$hasThirdEyeDevice()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isInRadius(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void psitweaks$extendCoordinateRange(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (psitweaks$hasThirdEyeDevice()) {
            cir.setReturnValue(true);
            return;
        }
        Boolean interferenceRangeResult = psitweaks$isInInterferenceRange(x, y, z);
        if (interferenceRangeResult != null) {
            cir.setReturnValue(interferenceRangeResult);
            return;
        }
        Boolean sableResult = SableRangeCompat.isInRadius(focalPoint, x, y, z);
        if (sableResult != null) {
            cir.setReturnValue(sableResult);
        }
    }

    private boolean psitweaks$hasThirdEyeDevice() {
        return caster != null && CuriosSlotChecker.hasItemInMagicSlot(caster, ItemThirdEyeDevice.class);
    }

    private Boolean psitweaks$isInInterferenceRange(double x, double y, double z) {
        if (caster == null
                || focalPoint == null
                || !CuriosSlotChecker.hasItemInMagicSlot(caster, ItemInterferenceRangeExtender.class)) {
            return null;
        }
        Boolean sableResult = SableRangeCompat.isInRadius(focalPoint, x, y, z, INTERFERENCE_RANGE);
        if (sableResult != null) {
            return sableResult;
        }
        return focalPoint.distanceToSqr(x, y, z) <= INTERFERENCE_RANGE_SQUARED;
    }
}
