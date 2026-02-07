package com.moratan251.psitweaks.common.spells;


import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.world.effect.MobEffect;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.common.spell.trick.potion.PieceTrickPotionBase;

public class PieceTrickParade extends PieceTrickPotionBase {
    public PieceTrickParade(Spell spell) {
        super(spell);
    }

    public MobEffect getPotion() {
        return PsitweaksEffects.PARADE.get();
    }



    public int getPotency(int power, int time) throws SpellCompilationException {
        return (int)this.multiplySafe((double)power, new double[]{(double)power, (double)power, (double)time, (double)5.0F});
    }


}
