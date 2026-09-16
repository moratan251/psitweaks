package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.EntityTunnelerArrow;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {
    // Skip only the powered projectile shield; retain spawn invulnerability and other damage checks.
    @Redirect(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;isPowered()Z"))
    private boolean psitweaks$tunnelerPiercesShield(WitherBoss wither, DamageSource source, float amount) {
        return wither.isPowered() && !(source.getDirectEntity() instanceof EntityTunnelerArrow);
    }
}
