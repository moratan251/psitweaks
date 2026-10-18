package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.combat.ParadeSight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Trick: Parade: a hidden caster also counts as invisible to the viewer, so other mods that draw over players (Neat's
 * name plates, for example) leave them out as well. Client side only.
 */
@Mixin(Entity.class)
public abstract class EntityParadeInvisibleMixin {
    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void psitweaks$paradeHidden(Player viewer, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide && ParadeSight.isHiddenFrom(self, viewer)) {
            cir.setReturnValue(true);
        }
    }
}
