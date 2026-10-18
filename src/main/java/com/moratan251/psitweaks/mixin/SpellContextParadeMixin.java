package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Trick: Parade: a spell that names a Parade caster directly acts on the lookalike and achieves nothing, unless it is
 * the caster's own spell or one cast by someone who could not attack the caster anyway.
 */
@Mixin(value = SpellContext.class, remap = false)
public abstract class SpellContextParadeMixin {
    @Shadow
    public Player caster;

    @Inject(method = "verifyEntity", at = @At("HEAD"))
    private void psitweaks$paradeHidesTarget(Entity entity, CallbackInfo ci) throws SpellRuntimeException {
        if (Parades.decoyFacing(entity, caster) != null) {
            throw new SpellRuntimeException(Parades.ERROR_DISGUISED);
        }
    }
}
