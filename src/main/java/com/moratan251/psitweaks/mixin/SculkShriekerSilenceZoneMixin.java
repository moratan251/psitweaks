package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySilenceZone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 遮音領域（{@link EntitySilenceZone}）の中のスカルクシュリーカーは叫ばない。踏まれたときは振動を通さずに直接
 * {@code tryShriek} が呼ばれるので、シュリーカーか、叫ばせたプレイヤーが遮音領域の中なら止める（振動で叫ぶときは、
 * 道筋の遮りを {@link VibrationListenerSilenceZoneMixin} が見ている）。
 */
@Mixin(SculkShriekerBlockEntity.class)
public abstract class SculkShriekerSilenceZoneMixin {
    @Inject(method = "tryShriek", at = @At("HEAD"), cancellable = true)
    private void psitweaks$silenceZoneStopsShriek(ServerLevel level, @Nullable ServerPlayer player, CallbackInfo ci) {
        Vec3 shrieker = Vec3.atCenterOf(((BlockEntity) (Object) this).getBlockPos());
        if (EntitySilenceZone.silences(level, shrieker) || (player != null && EntitySilenceZone.silences(level, player.position()))) {
            ci.cancel();
        }
    }
}
