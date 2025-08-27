package com.moratan251.psitweaks.client.guis;

import com.moratan251.psitweaks.common.handler.NetworkHandler;
import net.minecraft.world.item.ItemStack;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.client.gui.GuiProgrammer;
import vazkii.psi.common.item.ItemSpellDrive;

import vazkii.psi.common.spell.SpellCompiler;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class GuiFlashRing extends GuiProgrammer {
    public GuiFlashRing(ItemStack stack) {
        super(null, ItemSpellDrive.getSpell(stack));
    }

    @Override
    public void onSpellChanged(boolean nameOnly) {
        spell.uuid = UUID.randomUUID();
        NetworkHandler.CHANNEL.sendToServer(new MessageFlashRingSync(spell));
        onSelectedChanged();
        spellNameField.setFocused(nameOnly);

        if(!nameOnly ||
                (compileResult.right().isPresent() && compileResult.right().get().getMessage().equals(SpellCompilationException.NO_NAME)) ||
                spell.name.isEmpty()) {
            compileResult = new SpellCompiler().compile(spell);
        }
    }
}
