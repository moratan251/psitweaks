package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.operator.entity.PieceOperatorEntityPosition;

/** Trick: Parade: other casters asking where a Parade caster is are told where the lookalike stands. */
@Mixin(value = PieceOperatorEntityPosition.class, remap = false)
public abstract class PieceOperatorEntityPositionParadeMixin {
    @Shadow
    SpellParam<Entity> target;

    @Inject(method = "execute", at = @At("RETURN"), cancellable = true)
    private void psitweaks$paradeFakesPosition(SpellContext context, CallbackInfoReturnable<Object> cir)
            throws SpellRuntimeException {
        Entity entity = ((SpellPiece) (Object) this).getParamValue(context, target);
        EntityParadeDecoy decoy = Parades.decoyFacing(entity, context.caster);
        if (decoy != null) {
            // Like the real thing for a player: the lookalike's eyes.
            cir.setReturnValue(Vector3.fromEntity(decoy).add(0, decoy.getEyeHeight(), 0));
        }
    }
}
