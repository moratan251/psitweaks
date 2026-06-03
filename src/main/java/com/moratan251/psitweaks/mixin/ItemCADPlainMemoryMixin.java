package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.memory.CadPlainMemory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.item.ItemCAD;

@Mixin(value = ItemCAD.class, remap = false)
public abstract class ItemCADPlainMemoryMixin {
    @Inject(method = "setStoredVector", at = @At("RETURN"))
    private void psitweaks$storeVectorAsPlainMemory(ItemStack cadStack, int slot, Vector3 vector, CallbackInfo ci) {
        CadPlainMemory.storeVectorFromPsi(cadStack, slot, vector);
    }

    @Inject(method = "getStoredVector", at = @At("HEAD"), cancellable = true)
    private void psitweaks$getVectorFromPlainMemory(ItemStack cadStack, int slot,
            CallbackInfoReturnable<Vector3> cir) throws SpellRuntimeException {
        CadPlainMemory.storedVectorForPsi(cadStack, slot).ifPresent(cir::setReturnValue);
    }
}
