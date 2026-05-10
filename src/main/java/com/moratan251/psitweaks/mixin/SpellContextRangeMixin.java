package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
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
        Boolean sableResult = SableRangeCompat.isInRadius(focalPoint, x, y, z);
        if (sableResult != null) {
            cir.setReturnValue(sableResult);
        }
    }

    private boolean psitweaks$hasThirdEyeDevice() {
        return caster != null && CuriosSlotChecker.hasItemInMagicSlot(caster, ItemThirdEyeDevice.class);
    }
}
