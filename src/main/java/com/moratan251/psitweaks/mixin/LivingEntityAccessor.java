package com.moratan251.psitweaks.mixin;

import java.util.Stack;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Trick: Barrier takes hits before vanilla's hurt cooldown, so it keeps {@code lastHurt} itself: for the hits it cancels
 * and for the hit that breaks a shield. NeoForge leaves the damage container of a cancelled hit on
 * {@code damageContainers}; the barrier clears it later.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastHurt")
    float psitweaks$getLastHurt();

    @Accessor("lastHurt")
    void psitweaks$setLastHurt(float lastHurt);

    @Accessor("damageContainers")
    Stack<DamageContainer> psitweaks$getDamageContainers();
}
