package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;

public class PieceOperatorOutsideRegion extends PieceOperatorRegionFilterBase {
    public PieceOperatorOutsideRegion(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean keepInside() {
        return false;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.entity_list_wrapper");
    }
}
