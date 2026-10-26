package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
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
        IItemHandler playerHandler = receiver(context.caster);
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

    /**
     * 引き出し先。自律詠唱機の詠唱主体は、機械のアイテム枠に写るホットバーだけを使う。残りの枠は毎 tick 機械へ戻され、
     * 入らない分は前面に落ちるので、そこへ入れると満杯で止まらず、箱の中身を床へ出し続けてしまう。
     */
    private static IItemHandler receiver(Player caster) {
        PlayerMainInvWrapper main = new PlayerMainInvWrapper(caster.getInventory());
        return caster instanceof AutonomousCasterPlayer ? new RangedWrapper(main, 0, Inventory.getSelectionSize()) : main;
    }
}
