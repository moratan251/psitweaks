package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySilenceZone;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 遮音領域（{@link EntitySilenceZone}）は、振動を羊毛と同じように遮る。振動を聞くもの（スカルクセンサー、調律された
 * スカルクセンサー、スカルクシュリーカー、ウォーデン、アレイ）は {@code VibrationSystem.Listener} を通して振動を受け取り、
 * そこで振動の起きた点から聞き手までの道筋が羊毛に遮られるかを {@code isOccluded} で見ている。道筋が遮音領域を通れば
 * 遮られたことにする。スカルクセンサーを踏んだときは {@code forceScheduleVibration} で遮りの判定を飛ばして届くので、
 * そちらも同じ道筋で止める。
 */
@Mixin(VibrationSystem.Listener.class)
public abstract class VibrationListenerSilenceZoneMixin {
    @Shadow
    @Final
    private VibrationSystem system;

    @Inject(method = "isOccluded", at = @At("HEAD"), cancellable = true)
    private static void psitweaks$silenceZoneOccludes(Level level, Vec3 eventPos, Vec3 vibrationUserPos,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (EntitySilenceZone.blocksPath(level, eventPos, vibrationUserPos)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "forceScheduleVibration", at = @At("HEAD"), cancellable = true)
    private void psitweaks$silenceZoneStopsForcedVibration(ServerLevel level, Holder<GameEvent> gameEvent,
                                                          GameEvent.Context context, Vec3 pos, CallbackInfo ci) {
        Vec3 listener = system.getVibrationUser().getPositionSource().getPosition(level).orElse(pos);
        if (EntitySilenceZone.blocksPath(level, pos, listener)) {
            ci.cancel();
        }
    }
}
