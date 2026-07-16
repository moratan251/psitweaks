package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.items.curios.CuriosSlotChecker;
import com.moratan251.psitweaks.common.items.curios.ItemInterferenceRangeExtender;
import com.moratan251.psitweaks.common.items.curios.ItemThirdEyeDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vazkii.psi.api.spell.SpellHelpers;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellParam;

@Mixin(value = SpellHelpers.class, remap = false)
public abstract class SpellHelpersRangeLimitMixin {
    private static final double INTERFERENCE_RAYCAST_RANGE = 64.0D;
    private static final double THIRD_EYE_RAYCAST_RANGE = 256.0D;

    @ModifyVariable(method = "rangeLimitParam", at = @At("HEAD"), index = 3, argsOnly = true)
    private static double psitweaks$extendRangeLimit(
            double defaultLimit,
            SpellPiece ignoredPiece,
            SpellContext context,
            SpellParam<Number> ignoredParam,
            double ignoredOriginalDefaultLimit
    ) {
        if (context == null || context.caster == null) {
            return defaultLimit;
        }
        if (CuriosSlotChecker.hasItemInMagicSlot(context.caster, ItemThirdEyeDevice.class)) {
            return THIRD_EYE_RAYCAST_RANGE;
        }
        if (CuriosSlotChecker.hasItemInMagicSlot(context.caster, ItemInterferenceRangeExtender.class)) {
            return INTERFERENCE_RAYCAST_RANGE;
        }
        return defaultLimit;
    }
}
