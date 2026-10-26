package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.caster.AutonomousCasterRegistry;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 自律詠唱機の Caster が撃った飛び道具（Psi の術式弾を含む）の所有者。
 * Caster は所有者の UUID を名乗るため、そのまま保存すると再読み込みの後に本物の所有者が飛び道具の所有者になる。
 * 機械ごとの内部キーを保存し、ワールドで見つからない時にそのキーで Caster を引く。撤去された機械の Caster は引かない。
 */
@Mixin(Projectile.class)
public abstract class ProjectileAutonomousCasterMixin {
    @Shadow
    @Nullable
    private UUID ownerUUID;

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void psitweaks$rememberAutonomousCasterKey(@Nullable Entity owner, CallbackInfo ci) {
        if (owner instanceof AutonomousCasterPlayer caster) {
            ownerUUID = caster.casterKey();
        }
    }

    @Inject(method = "getOwner", at = @At("RETURN"), cancellable = true)
    private void psitweaks$findAutonomousCaster(CallbackInfoReturnable<Entity> cir) {
        if (cir.getReturnValue() != null || ownerUUID == null) {
            return;
        }
        Player caster = AutonomousCasterRegistry.find(((Entity) (Object) this).level(), ownerUUID);
        if (caster != null) {
            cir.setReturnValue(caster);
        }
    }
}
