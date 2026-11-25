package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Trick: Parade: the Ender Dragon picks the player to swoop on and shoot fireballs at by itself, without the target
 * change event. A strafe on a Parade caster, new or under way, turns onto the caster's image
 * ({@link Parades#lureTarget}), and the dragon flies at and shoots the image instead.
 */
@Mixin(DragonStrafePlayerPhase.class)
public abstract class DragonStrafePlayerPhaseParadeMixin extends AbstractDragonPhaseInstance {
    @Shadow
    private LivingEntity attackTarget;

    @Shadow
    public abstract void setTarget(LivingEntity attackTarget);

    private DragonStrafePlayerPhaseParadeMixin(EnderDragon dragon) {
        super(dragon);
    }

    @Inject(method = "doServerTick", at = @At("HEAD"))
    private void psitweaks$strafeParadeImage(CallbackInfo ci) {
        EntityParadeDecoy decoy = Parades.lureTarget(dragon, attackTarget);
        if (decoy != null) {
            setTarget(decoy);
        }
    }
}
