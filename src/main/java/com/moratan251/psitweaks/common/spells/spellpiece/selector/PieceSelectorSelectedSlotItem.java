package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.item.EntityInventorySlotSource;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorSelectedSlotItem extends PieceSelector {
    public PieceSelectorSelectedSlotItem(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int slot = context.getTargetSlot();
        ItemStack stack = context.caster.getInventory().getItem(slot);
        if (stack.isEmpty()) {
            return SpellItemValue.EMPTY;
        }
        return SpellItemValue.sourced(
                stack,
                new EntityInventorySlotSource(context.caster.getUUID(), context.caster.getId(), slot));
    }

    @Override
    public Class<?> getEvaluationType() {
        return SpellItemValue.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.item");
    }
}
