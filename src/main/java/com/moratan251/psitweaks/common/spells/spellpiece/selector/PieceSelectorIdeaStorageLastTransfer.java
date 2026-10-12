package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.util.IdeaStorageLastTransfer;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * Amount moved by the latest ideaspace transfer trick that ran earlier in this cast (items: count, fluids and
 * chemicals: mB, FE: FE); 0 before any of them ran.
 */
public class PieceSelectorIdeaStorageLastTransfer extends PieceSelector {
    public PieceSelectorIdeaStorageLastTransfer(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(0));
        setStatLabel(EnumSpellStat.COST, new StatLabel(0));
    }

    @Override
    public void initParams() {
    }

    @Override
    public Object execute(SpellContext context) {
        return (double) IdeaStorageLastTransfer.get(context);
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
