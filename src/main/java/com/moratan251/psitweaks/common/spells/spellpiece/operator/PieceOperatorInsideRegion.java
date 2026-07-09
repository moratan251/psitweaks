package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;

public class PieceOperatorInsideRegion extends PieceOperatorRegionFilterBase {
    public PieceOperatorInsideRegion(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean keepInside() {
        return true;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.entity_list_wrapper");
    }
}
