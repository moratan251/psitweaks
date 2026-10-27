package com.moratan251.psitweaksqol.mixin.client;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.client.gui.GuiProgrammer;

/** Psi の Ctrl+Z / Ctrl+Y は術式を差し替えるだけで名前欄を更新しないので、差し替わったら名前欄を合わせる。 */
@Mixin(value = GuiProgrammer.class, remap = false)
public abstract class GuiProgrammerSpellNameSyncMixin {
    @Shadow
    public Spell spell;
    @Shadow
    public EditBox spellNameField;

    @Unique
    private Spell psitweaks$spellBeforeKeyPress;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void psitweaks$rememberSpell(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback) {
        psitweaks$spellBeforeKeyPress = spell;
    }

    @Inject(method = "keyPressed", at = @At("RETURN"))
    private void psitweaks$syncSpellNameField(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callback) {
        Spell before = psitweaks$spellBeforeKeyPress;
        psitweaks$spellBeforeKeyPress = null;
        if (spell != null
                && spell != before
                && spellNameField != null
                && !spellNameField.getValue().equals(spell.name)) {
            spellNameField.setValue(spell.name);
        }
    }
}
