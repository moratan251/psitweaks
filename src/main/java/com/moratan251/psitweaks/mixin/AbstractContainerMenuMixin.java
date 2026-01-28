package com.moratan251.psitweaks.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/*
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {


    @Inject(method = "*", at = @At("HEAD"), cancellable = true)
    private void interceptStillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        // メソッド名を確認してstillValidメソッドのみ対象にする
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        boolean isStillValidCall = false;

        for (StackTraceElement element : stack) {
            if (element.getMethodName().equals("stillValid") || element.getMethodName().contains("stillValid")) {
                isStillValidCall = true;
                break;
            }
        }

        if (!isStillValidCall) {
            return;
        }

        // Psiリモートアクセス権限をチェック
        if (hasPsiRemoteAccess(player)) {
            cir.setReturnValue(true);
        }
    }



    private boolean hasPsiRemoteAccess(Player player) {
        try {
            var playerData = player.getPersistentData();
            if (playerData.contains("psi_remote_access")) {
                long accessTime = playerData.getLong("psi_remote_access");
                long currentTime = player.level().getGameTime();

                if (currentTime - accessTime < 200) { // 10秒間有効
                    return true;
                } else {
                    playerData.remove("psi_remote_access");
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
*/
