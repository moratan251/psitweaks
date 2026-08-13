package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import java.util.function.Predicate;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceTrickSendItem extends PieceTrickItemTransferBase {

    public PieceTrickSendItem(Spell spell) {
        super(spell);
    }

    @Override
    protected void transfer(SpellContext context, IItemHandler handler, Predicate<ItemStack> filter, int limit)
            throws SpellRuntimeException {
        Inventory inventory = context.caster.getInventory();
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            ItemStack stack = inventory.items.get(slot);
            if (stack.isEmpty() || stack.getItem() instanceof ICAD || !filter.test(stack)) {
                continue;
            }

            int amount = transferAmount(stack, limit);
            ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack.copyWithCount(amount), false);
            int moved = amount - remainder.getCount();
            if (moved > 0) {
                stack.shrink(moved);
                return;
            }
        }
    }
}
