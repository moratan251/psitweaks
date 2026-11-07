package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.compat.IdeaStoragePieceMigration;
import com.moratan251.psitweaks.common.compat.OptionalSpellPieceCompat;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;

@Mixin(value = SpellPiece.class, remap = false)
public abstract class SpellPieceOptionalCompatMixin {
    @Inject(method = "createFromNBT", at = @At("HEAD"), cancellable = true)
    private static void psitweaks$handleOptionalSpellPieceCompat(Spell spell, CompoundTag cmp, CallbackInfoReturnable<SpellPiece> cir) {
        if (OptionalSpellPieceCompat.shouldRemoveMissingPhysicalPropulsion(cmp)) {
            cir.setReturnValue(OptionalSpellPieceCompat.createRemovalMarker(spell));
            return;
        }
        // Pieces renamed during 1.0.0 development load under their new ID (the copy has the new ID, so this ends).
        CompoundTag renamed = OptionalSpellPieceCompat.renamedCopy(cmp);
        if (renamed != null) {
            cir.setReturnValue(SpellPiece.createFromNBT(spell, renamed));
            return;
        }
        // Ideaspace pieces saved before the merge load from a migrated copy; the copy no longer matches, so this
        // recursion ends after one step.
        CompoundTag migrated = IdeaStoragePieceMigration.migrate(cmp);
        if (migrated != null) {
            cir.setReturnValue(SpellPiece.createFromNBT(spell, migrated));
        }
    }
}
