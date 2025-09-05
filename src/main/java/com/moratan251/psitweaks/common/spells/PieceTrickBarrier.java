package com.moratan251.psitweaks.common.spells;


import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.world.effect.MobEffect;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.spell.trick.potion.PieceTrickPotionBase;

public class PieceTrickBarrier extends PieceTrickPotionBase {
    public PieceTrickBarrier(Spell spell) {
        super(spell);
    }

    public MobEffect getPotion() {
        return PsitweaksEffects.BARRIER.get();
    }


}
