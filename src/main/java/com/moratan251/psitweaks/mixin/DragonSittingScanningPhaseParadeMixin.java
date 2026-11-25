package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingScanningPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Trick: Parade: the Ender Dragon, perched on the portal, looks for the nearest player to turn its fiery breath on, or to
 * charge at as it takes off. A Parade caster it finds is swapped for the caster's image ({@link Parades#lureOrKeep}), so it
 * turns to, breathes at and charges at the image instead.
 */
@Mixin(DragonSittingScanningPhase.class)
public abstract class DragonSittingScanningPhaseParadeMixin extends AbstractDragonSittingPhase {
    private DragonSittingScanningPhaseParadeMixin(EnderDragon dragon) {
        super(dragon);
    }

    @ModifyVariable(method = "doServerTick", at = @At("STORE"))
    private LivingEntity psitweaks$scanParadeImage(LivingEntity found) {
        return Parades.lureOrKeep(dragon, found);
    }
}
