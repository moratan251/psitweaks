package com.moratan251.psitweaks.mixin;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** ロケット花火が生き物についているか（エリトラの加速。作動式: 減速領域が遅くしないのに使う）。 */
@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
    @Invoker("isAttachedToEntity")
    boolean psitweaks$isAttachedToEntity();
}
