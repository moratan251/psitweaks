package com.moratan251.psitweaks.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Who the Ender Dragon's strafe is after, for the GameTest of Trick: Parade's hold on the dragon. */
@Mixin(DragonStrafePlayerPhase.class)
public interface DragonStrafePlayerPhaseAccessor {
    @Accessor("attackTarget")
    LivingEntity psitweaks$getAttackTarget();
}
