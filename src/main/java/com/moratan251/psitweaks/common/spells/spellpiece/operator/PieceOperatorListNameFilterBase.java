package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.spellpiece.selector.DisplayNameTargetHelper;
import com.moratan251.psitweaksqol.api.PsitweaksValueKind;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Filters an Entity, Item or Block List by display name instead of ID. The name is the one Selector: Display Name
 * gives: a player's name, a custom name, or else the name in the caster's language. The pattern rules are the same as
 * Operator: List Keep Matches (whole match, * ? [abc], case kept).
 */
public abstract class PieceOperatorListNameFilterBase extends PieceOperatorListStringFilterBase {
    protected PieceOperatorListNameFilterBase(Spell spell) {
        super(spell);
    }

    @Override
    protected PsitweaksValueKind modeOptionKindFilter() {
        return PsitweaksValueKind.CONTEXTUAL;
    }

    @Override
    protected String searchText(SpellContext context, Object value, boolean matchFullId)
            throws SpellRuntimeException {
        return DisplayNameTargetHelper.getDisplayName(context, value);
    }
}
