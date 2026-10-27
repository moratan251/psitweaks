package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.VirtualCad;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.common.item.ItemCAD;

/** Psi の FakePlayer 拒否のうち、稼働中の自律詠唱機専用 Caster だけを通す。内蔵 CAD は専用 Caster の外で持てない。 */
@Mixin(value = ItemCAD.class, remap = false)
public abstract class ItemCADAutonomousCasterMixin {
    @Inject(method = "isTruePlayer", at = @At("HEAD"), cancellable = true)
    private static void psitweaks$allowAutonomousCaster(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AutonomousCasterPlayer caster && caster.isAttached()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 機械の外へ出た内蔵 CAD（固定の性能を持つ）は、持ち物に入った時に消す。機械は内蔵 CAD を外へ落とさないが、
     * 以前の不具合（術式で機械自身を壊すと前面に落ちた）で出た物も使えないようにする。
     */
    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void psitweaks$discardEscapedVirtualCad(ItemStack stack, Level level, Entity entity, int slot, boolean selected,
                                                    CallbackInfo ci) {
        if (!level.isClientSide() && !(entity instanceof AutonomousCasterPlayer) && VirtualCad.isVirtualCad(stack)) {
            stack.setCount(0);
            ci.cancel();
        }
    }
}
