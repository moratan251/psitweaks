package com.moratan251.psitweaks.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 矢が地面に刺さっているか（作動式: 減速領域が刺さった矢を遅くしないのに使う）。 */
@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Accessor("inGround")
    boolean psitweaks$isInGround();
}
