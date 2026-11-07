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
        // Psi の確認（メモリの大きさ）より前に返すので、同じ確認をここでもする。大きいメモリで保存してから小さいメモリの
        // CAD に替えると、範囲外の番号の古い値が読めていた。範囲外は Psi に任せてエラーにする。
        if (slot < 0 || slot >= ((ItemCAD) (Object) this).getMemorySize(cadStack)) {
            return;
        }
        CadPlainMemory.storedVectorForPsi(cadStack, slot).ifPresent(cir::setReturnValue);
    }
}
