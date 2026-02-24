package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.world.effect.MobEffect;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.common.spell.trick.potion.PieceTrickPotionBase;

public class PieceTrickRadiationFilter extends PieceTrickPotionBase {

    public PieceTrickRadiationFilter(Spell spell) {
        super(spell);
    }

    @Override
    public MobEffect getPotion() {
        return PsitweaksEffects.RADIATION_FILTER.get();
    }

    @Override
    public boolean hasPower() {
        return false;
    }
}
