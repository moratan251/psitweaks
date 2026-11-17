package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/** 1 when the path reaches anything in the target's NBT, else 0. */
public class PieceSelectorNbtExists extends PieceSelectorNbtBase {
    public PieceSelectorNbtExists(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean pathOptional() {
        return false;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return find(context).isEmpty() ? 0.0D : 1.0D;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
