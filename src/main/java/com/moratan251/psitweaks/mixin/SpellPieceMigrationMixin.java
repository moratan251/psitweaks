package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.compat.IdeaStoragePieceMigration;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;

/** Loads spells saved with pieces that were later merged or renamed. */
@Mixin(value = SpellPiece.class, remap = false)
public abstract class SpellPieceMigrationMixin {
    @Inject(method = "createFromNBT", at = @At("HEAD"), cancellable = true)
    private static void psitweaks$migrateSavedPiece(Spell spell, CompoundTag cmp, CallbackInfoReturnable<SpellPiece> cir) {
        // Ideaspace pieces saved before the merge load from a migrated copy; the copy no longer matches, so this
        // recursion ends after one step.
        CompoundTag migrated = IdeaStoragePieceMigration.migrate(cmp);
        if (migrated != null) {
            cir.setReturnValue(SpellPiece.createFromNBT(spell, migrated));
        }
    }
}
