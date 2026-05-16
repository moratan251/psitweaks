package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.compat.OptionalSpellPieceCompat;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.SpellGrid;

import java.util.List;

@Mixin(value = SpellGrid.class, remap = false)
public abstract class SpellGridOptionalCompatMixin {
    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void psitweaks$removeOptionalPiecesAfterNbtRead(CompoundTag cmp, CallbackInfo ci) {
        OptionalSpellPieceCompat.removeMarkedPieces((SpellGrid) (Object) this);
    }

    @Inject(method = "fromCodecData", at = @At("RETURN"))
    private static void psitweaks$removeOptionalPiecesAfterCodecRead(List<?> pieces, CallbackInfoReturnable<SpellGrid> cir) {
        OptionalSpellPieceCompat.removeMarkedPieces(cir.getReturnValue());
    }
}
