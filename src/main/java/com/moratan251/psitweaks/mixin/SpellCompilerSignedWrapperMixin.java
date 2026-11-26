package com.moratan251.psitweaks.mixin;

import com.moratan251.psitweaks.common.spells.spellpiece.constant.PieceConstantSignedWrapper;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.common.spell.SpellCompiler;

/**
 * Validates Constant: Signed Wrapper readers once all reachable pieces have passed normal compilation and metadata
 * checks, before the spell name check so errors still appear while editing an unnamed spell.
 */
@Mixin(value = SpellCompiler.class, remap = false)
public abstract class SpellCompilerSignedWrapperMixin {
    @Shadow
    private CompiledSpell compiled;

    @Inject(
            method = "doCompile",
            at = @At(value = "FIELD", target = "Lvazkii/psi/api/spell/Spell;name:Ljava/lang/String;", ordinal = 0)
    )
    private void psitweaks$validateSignedWrappers(
            Spell spell,
            RegistryAccess registries,
            @Nullable Player player,
            CallbackInfoReturnable<CompiledSpell> cir
    ) throws SpellCompilationException {
        PieceConstantSignedWrapper.validateCompiled(this.compiled);
    }
}
