package com.moratan251.psitweaks.common.spells;


import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.world.effect.MobEffect;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.common.spell.trick.potion.PieceTrickPotionBase;

public class PieceTrickFlight extends PieceTrickPotionBase {
    public PieceTrickFlight(Spell spell) {
        super(spell);
    }

    public MobEffect getPotion() {
        return PsitweaksEffects.FLIGHT.get();
    }

    public boolean hasPower() {
        return false;
    }

    public int getCost(int power, int time) throws SpellCompilationException {
        return (int)this.multiplySafe((double)(200 + 20 * time), 1.0);
    }

    public int getPotency(int power, int time) throws SpellCompilationException {
        return (int)this.multiplySafe(380 + time,1.0);
    }





}
