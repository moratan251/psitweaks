package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.effects.PsitweaksEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.common.spell.trick.potion.PieceTrickPotionBase;

public class PieceTrickBarrier extends PieceTrickPotionBase {
    public PieceTrickBarrier(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.power", true).square()
                .mul("psi.spellparam.time", true).mul(5).add(20));
    }

    @Override
    public Holder<MobEffect> getPotion() {
        return PsitweaksEffects.BARRIER;
    }
}
