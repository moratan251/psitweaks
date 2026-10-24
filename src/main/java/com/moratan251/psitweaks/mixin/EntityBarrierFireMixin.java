package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.combat.BarrierShields;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Trick: Barrier: an entity with a shield up does not catch fire. Whatever would set it alight (fire, lava, Fire Aspect,
 * sunlight) leaves it one tick short of burning instead, so it catches fire as usual once the shield is gone. Both
 * sides, as the shield is synced.
 */
@Mixin(Entity.class)
public abstract class EntityBarrierFireMixin {
    @ModifyVariable(method = "setRemainingFireTicks", at = @At("HEAD"), argsOnly = true)
    private int psitweaks$shieldKeepsFireOff(int remainingFireTicks) {
        if (remainingFireTicks > 0 && (Object) this instanceof LivingEntity living && BarrierShields.isShielded(living)) {
            return -1;
        }
        return remainingFireTicks;
    }
}
