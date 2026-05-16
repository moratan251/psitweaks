package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.trick.entity.PieceTrickMassExodus;

@Mixin(value = PieceTrickMassExodus.class, remap = false)
public abstract class PieceTrickMassExodusMixin {
    @ModifyVariable(method = "execute", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private Vector3 psitweaks$projectTargetPosition(Vector3 position, SpellContext context) throws SpellRuntimeException {
        if (position == null || context == null || context.focalPoint == null) {
            return position;
        }

        if (!context.isInRadius(position)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Level level = context.focalPoint.level();
        return SableRangeCompat.projectVectorForEffect(level, position);
    }
}
