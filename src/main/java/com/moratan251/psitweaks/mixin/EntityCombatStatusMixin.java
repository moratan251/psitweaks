package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.combat.CombatStatuses;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The wet state of Trick: Aqua Cutter (see {@link CombatStatuses}) works like Ars Nouveau's Soaked: a soaked creature
 * counts as in water or rain, so anything that checks for that (Trick: Chain Lightning, Ars Nouveau's Cold Snap and
 * lightning, a Riptide trident) sees it as wet, and it does not catch fire. Both sides, as the state is synced.
 * {@code isInWaterRainOrBubble} is left alone, so blazes and endermen are not hurt by it as by rain.
 */
@Mixin(Entity.class)
public abstract class EntityCombatStatusMixin {
    @Inject(method = "isInWaterOrRain", at = @At("RETURN"), cancellable = true)
    private void psitweaks$soakedCountsAsWet(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && (Object) this instanceof LivingEntity living && CombatStatuses.isSoaked(living)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "setRemainingFireTicks", at = @At("HEAD"), argsOnly = true)
    private int psitweaks$soakedKeepsFireOff(int remainingFireTicks) {
        if (remainingFireTicks > 0 && (Object) this instanceof LivingEntity living && CombatStatuses.isSoaked(living)) {
            return 0;
        }
        return remainingFireTicks;
    }
}
