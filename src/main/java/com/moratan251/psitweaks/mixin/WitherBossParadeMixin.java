package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Trick: Parade: the Wither's side heads pick a nearby creature to shoot at by themselves, without the target change
 * event; one that picks a Parade caster aims at the caster's image instead ({@link Parades#lureTarget}).
 */
@Mixin(WitherBoss.class)
public abstract class WitherBossParadeMixin {
    @ModifyVariable(method = "setAlternativeTarget", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int psitweaks$aimAtParadeImage(int newId) {
        WitherBoss self = (WitherBoss) (Object) this;
        if (newId <= 0 || self.level().isClientSide || !(self.level().getEntity(newId) instanceof LivingEntity target)) {
            return newId;
        }
        EntityParadeDecoy decoy = Parades.lureTarget(self, target);
        return decoy != null ? decoy.getId() : newId;
    }
}
