package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceTrickPullItem extends PieceTrickItemTransferBase {

    public PieceTrickPullItem(Spell spell) {
        super(spell);
    }

    @Override
    protected boolean transfer(SpellContext context, IItemHandler handler, Predicate<ItemStack> filter, int limit)
            throws SpellRuntimeException {
        // Inventory.add はクリエイティブでは失敗時にスタックを消去する仕様のため、
        // IItemHandler 経由のシミュレート→コミットで正確に移動量を確定する
        IItemHandler playerHandler = new PlayerMainInvWrapper(context.caster.getInventory());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack slotStack = handler.getStackInSlot(slot);
            if (slotStack.isEmpty() || !filter.test(slotStack)) {
                continue;
            }

            ItemStack extracted = handler.extractItem(slot, transferAmount(slotStack, limit), true);
            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack remainder = ItemHandlerHelper.insertItem(playerHandler, extracted, true);
            int moved = extracted.getCount() - remainder.getCount();
            if (moved <= 0) {
                continue;
            }

            ItemStack taken = handler.extractItem(slot, moved, false);
            ItemHandlerHelper.insertItem(playerHandler, taken, false);
            return true;
        }
        return false;
    }
}
