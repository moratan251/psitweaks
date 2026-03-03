package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.spell.selector.entity.PieceSelectorNearby;

import java.util.function.Predicate;

public class PieceSelectorNearbySpellGram extends PieceSelectorNearby {

    public PieceSelectorNearbySpellGram(Spell spell) {
        super(spell);
    }

    @Override
    public Predicate<Entity> getTargetPredicate(SpellContext context) {
        return entity -> entity instanceof SpellGramObject;
    }
}
