package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.common.network.PsiNetwork;

/**
 * 自律詠唱機専用 Caster 宛ての Psi payload をダミー接続へ送らず止める。
 * エラー位置とスパム抑制デバッグだけは作動ログへ渡す。
 */
@Mixin(value = PsiNetwork.class, remap = false)
public abstract class PsiNetworkAutonomousCasterMixin {
    @Inject(method = "sendToPlayer", at = @At("HEAD"), cancellable = true)
    private static void psitweaks$captureAutonomousCasterPayload(ServerPlayer player, CustomPacketPayload payload, CallbackInfo ci) {
        if (player instanceof AutonomousCasterPlayer caster) {
            caster.capturePsiPayload(payload);
            ci.cancel();
        }
    }
}
