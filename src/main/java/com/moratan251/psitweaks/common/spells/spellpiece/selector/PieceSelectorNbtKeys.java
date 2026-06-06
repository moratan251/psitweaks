package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.wrapper.StringListWrapper;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceSelectorNbtKeys extends PieceSelectorNbtBase {
    public PieceSelectorNbtKeys(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return StringListWrapper.make(NbtTargetHelper.sortedKeys(getTargetData(context)));
    }

    @Override
    public Class<?> getEvaluationType() {
        return StringListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string_list");
    }
}
