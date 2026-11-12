package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.SpellRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vazkii.psi.api.spell.SpellHelpers;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellParam;

/** レイキャストのピースの長さの上限を、術者の射程（{@link SpellRange#reach}）まで延ばす。 */
@Mixin(value = SpellHelpers.class, remap = false)
public abstract class SpellHelpersRangeLimitMixin {
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
        return SpellRange.reach(context.caster, defaultLimit);
    }
}
