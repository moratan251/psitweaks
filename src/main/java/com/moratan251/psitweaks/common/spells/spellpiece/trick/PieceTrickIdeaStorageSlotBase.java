package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemFilters;
import com.moratan251.psitweaks.common.spells.param.ParamItemSearch;
import com.moratan251.psitweaks.common.spells.util.IdeaStorageLastTransfer;
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
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Moves items between Psi's target slot (right of the CAD by default) and the caster's Ideaspace Storage. The filter
 * is the same search input as the block logistics: the wired value decides how it matches. There are no modes.
 */
public abstract class PieceTrickIdeaStorageSlotBase extends PieceTrick {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ERROR_SLOT_TRANSFER = "psitweaks.spellerror.idea_storage_slot_transfer_failed";
    protected SpellParam<Number> quantity;
    protected SpellParam<?> filterInput;

    protected PieceTrickIdeaStorageSlotBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(50));
        setStatLabel(EnumSpellStat.COST, new StatLabel(100));
    }

    protected abstract boolean deposit();

    @Override public void initParams() {
        addParam(quantity = new ParamNumber("psi.spellparam.number", SpellParam.RED, false, false));
        addParam(filterInput = new ParamItemSearch(PsitweaksSpellParams.FILTER, PsitweaksSpellParams.ITEM_COLOR, true, false));
    }

    @Override public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 50);
        meta.addStat(EnumSpellStat.COST, 100);
    }

    protected Predicate<ItemStack> itemFilter(SpellContext context) throws SpellRuntimeException {
        return SpellItemFilters.search(getParamValue(context, (SpellParam<?>) filterInput));
    }

    /** False when the filter is unconnected or an empty String, i.e. when {@link #itemFilter} accepts everything. */
    protected boolean hasFilter(SpellContext context) throws SpellRuntimeException {
        return SpellItemFilters.isSearchSet(getParamValue(context, (SpellParam<?>) filterInput));
    }

    @Override public Object execute(SpellContext context) throws SpellRuntimeException {
        IdeaStorageLastTransfer.record(context, 0);
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
        int moved = 0;
        try {
            if (deposit()) {
                moved = IdeaStorageResourceTransfers.depositItems(storage, handler, filter, maximum);
            } else if (!current.isEmpty()) {
                // Refill only the exact item already in the slot; the filter may still veto it.
                ItemStack template = current.copy();
                moved = IdeaStorageResourceTransfers.withdrawItems(storage, handler,
                        stack -> ItemStack.isSameItemSameComponents(template, stack) && filter.test(stack), maximum);
            } else if (hasFilter(context)) {
                moved = IdeaStorageResourceTransfers.withdrawItems(storage, handler, filter, maximum);
            }
        } catch (RuntimeException failure) {
            LOGGER.warn("Ideaspace slot {} failed at slot {}", deposit() ? "deposit" : "withdrawal", slot, failure);
            throw new SpellRuntimeException(ERROR_SLOT_TRANSFER);
        }
        IdeaStorageLastTransfer.record(context, moved);
        return null;
    }

    static IItemHandler slotHandler(Inventory inventory, int slot) {
        if (slot >= 0 && slot < inventory.items.size())
            return new RangedWrapper(new PlayerMainInvWrapper(inventory), slot, slot + 1);
        return slot == Inventory.SLOT_OFFHAND ? new PlayerOffhandInvWrapper(inventory) : null;
    }

    /** A custom target slot may point at the CAD in use or the tool casting the spell; leave both alone. */
    static boolean isCasterEquipment(SpellContext context, ServerPlayer player, int slot, ItemStack current) {
        if (slot == PsiAPI.getPlayerCADSlot(player)) return true;
        return !current.isEmpty() && (current == PsiAPI.getPlayerCAD(player) || current == context.tool);
    }
}
