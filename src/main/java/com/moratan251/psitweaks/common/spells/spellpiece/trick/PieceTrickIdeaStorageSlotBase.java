package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageResourceTransfers;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerOffhandInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.slf4j.Logger;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/** Moves items between Psi's target slot (right of the CAD by default) and the caster's Ideaspace Storage. */
public abstract class PieceTrickIdeaStorageSlotBase extends PieceTrickIdeaStorageItemBase {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ERROR_SLOT_TRANSFER = "psitweaks.spellerror.idea_storage_slot_transfer_failed";

    protected PieceTrickIdeaStorageSlotBase(Spell spell) {
        super(spell);
    }

    @Override protected final Kind kind() { return Kind.ITEM; }

    /** The target slot replaces the block position and face. */
    @Override protected void initEndpointParams() { }

    @Override public Object execute(SpellContext context) throws SpellRuntimeException {
        double amount = this.<Number>getNotNullParamValue(context, quantity).doubleValue();
        if (!Double.isFinite(amount) || amount <= 0) throw new SpellRuntimeException("psi.spellerror.nonpositivevalue");
        if (!(context.caster instanceof ServerPlayer player) || player instanceof FakePlayer
                || player.isSpectator() || !player.isAlive()) return null;
        int slot = context.getTargetSlot();
        Inventory inventory = player.getInventory();
        IItemHandler handler = slotHandler(inventory, slot);
        ItemStack current = inventory.getItem(slot);
        if (handler == null || isCasterEquipment(context, player, slot, current)) return null;
        Predicate<ItemStack> filter = itemFilter(context);
        int maximum = (int) IdeaStorageResourceTransfers.amountForPower(amount, 1, Integer.MAX_VALUE);
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        try {
            if (deposit()) {
                IdeaStorageResourceTransfers.depositItems(storage, handler, filter, maximum);
            } else if (!current.isEmpty()) {
                // Refill only the exact item already in the slot; the filter may still veto it.
                ItemStack template = current.copy();
                IdeaStorageResourceTransfers.withdrawItems(storage, handler,
                        stack -> ItemStack.isSameItemSameComponents(template, stack) && filter.test(stack), maximum);
            } else if (hasFilter(context)) {
                IdeaStorageResourceTransfers.withdrawItems(storage, handler, filter, maximum);
            }
        } catch (RuntimeException failure) {
            LOGGER.warn("Ideaspace slot {} failed at slot {}", deposit() ? "deposit" : "withdrawal", slot, failure);
            throw new SpellRuntimeException(ERROR_SLOT_TRANSFER);
        }
        return null;
    }

    static IItemHandler slotHandler(Inventory inventory, int slot) {
        if (slot >= 0 && slot < inventory.items.size())
            return new RangedWrapper(new PlayerMainInvWrapper(inventory), slot, slot + 1);
        return slot == Inventory.SLOT_OFFHAND ? new PlayerOffhandInvWrapper(inventory) : null;
    }

    /** A custom target slot may point at the CAD in use or the tool casting the spell; leave both alone. */
    private static boolean isCasterEquipment(SpellContext context, ServerPlayer player, int slot, ItemStack current) {
        if (slot == PsiAPI.getPlayerCADSlot(player)) return true;
        return !current.isEmpty() && (current == PsiAPI.getPlayerCAD(player) || current == context.tool);
    }
}
