package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.util.SpellPsiRefundHelper;
import java.util.List;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceTrickJumpFlex extends PieceTrickJump {
    public PieceTrickJumpFlex(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean jumpToMatchingAnchor(SpellContext context, String targetLabel) throws SpellRuntimeException {
        List<CompiledSpell.Action> actionsBeforeJump = SpellPsiRefundHelper.snapshotActions(context);
        boolean jumped = super.jumpToMatchingAnchor(context, targetLabel);
        if (jumped) {
            SpellPsiRefundHelper.refundRemovedActions(context, actionsBeforeJump);
        }
        return jumped;
    }
}
