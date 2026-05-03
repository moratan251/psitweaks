package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.common.spell.trick.potion.PieceTrickPotionBase;

public class PieceTrickFlight extends PieceTrickPotionBase {
    public PieceTrickFlight(Spell spell) {
        super(spell);
    }

    @Override
    public Holder<MobEffect> getPotion() {
        return PsitweaksEffects.FLIGHT;
    }

    @Override
    public boolean hasPower() {
        return false;
    }

    @Override
    public int getCost(int power, int time) throws SpellCompilationException {
        return (int) this.multiplySafe(300 + 30 * time, 1.0);
    }

    @Override
    public int getPotency(int power, int time) throws SpellCompilationException {
        return (int) this.multiplySafe(1000 + time, 1.0);
    }
}
