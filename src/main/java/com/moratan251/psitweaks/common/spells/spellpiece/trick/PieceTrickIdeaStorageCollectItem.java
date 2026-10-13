package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.BlockInventorySlotSource;
import com.moratan251.psitweaks.common.spells.item.EntityInventorySlotSource;
import com.moratan251.psitweaks.common.spells.item.ItemEntitySource;
import com.moratan251.psitweaks.common.spells.item.ItemSourceRef;
import com.moratan251.psitweaks.common.spells.item.SpellItemValue;
import com.moratan251.psitweaks.common.spells.param.ParamSpellItemFilter;
import com.moratan251.psitweaks.common.spells.util.IdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.spells.wrapper.SpellItemListWrapper;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageResourceTransfers;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import vazkii.psi.api.internal.Vector3;
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
 * Deposits Item values into the caster's Ideaspace Storage straight from where they were read: a dropped item, a slot
 * of the caster's own inventory, or a slot of a block inventory. A value moves at most its own count, and only while
 * its source still holds the same item with the same data components, so nothing is taken twice or swapped. Values
 * without a source and the inventories of other entities are skipped.
 */
public class PieceTrickIdeaStorageCollectItem extends PieceTrick {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ERROR_COLLECT = "psitweaks.spellerror.idea_storage_collect_failed";
    private static final String ERROR_ACCESS_DENIED = "psitweaks.spellerror.accessdenyed";

    private SpellParam<SpellItemValue> item;
    private SpellParam<Number> quantity;

    public PieceTrickIdeaStorageCollectItem(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(50));
        setStatLabel(EnumSpellStat.COST, new StatLabel(100));
    }

    @Override
    public void initParams() {
        addParam(item = new ParamSpellItemFilter(PsitweaksSpellParams.ITEM, PsitweaksSpellParams.ITEM_COLOR, false, false));
        addParam(quantity = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.RED, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 50);
        meta.addStat(EnumSpellStat.COST, 100);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        IdeaStorageLastTransfer.record(context, 0);
        // Read untyped: the input may be an Item List as well.
        Object value = getParamValue(context, (SpellParam<?>) item);
        Number limit = getParamValue(context, quantity);
        int maximum = Integer.MAX_VALUE;
        if (limit != null) {
            double amount = limit.doubleValue();
            if (!Double.isFinite(amount) || amount <= 0) {
                throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
            }
            maximum = (int) IdeaStorageResourceTransfers.amountForPower(amount, 1, Integer.MAX_VALUE);
        }
        if (!(context.caster instanceof ServerPlayer player)) {
            return null;
        }

        // Every source is checked first, so a protected block or a far source fails before anything moves.
        List<Pickup> pickups = new ArrayList<>();
        Set<ItemSourceRef> seen = new HashSet<>();
        for (SpellItemValue itemValue : values(value)) {
            ItemSourceRef source = itemValue.source().orElse(null);
            if (itemValue.isEmpty() || source == null || !seen.add(source)) {
                continue;
            }
            IItemHandler handler = resolve(context, player, source);
            if (handler != null) {
                pickups.add(new Pickup(handler, itemValue.snapshot()));
            }
        }

        var storage = IdeaStorageService.get(player.server, player.getUUID());
        long moved = 0;
        try {
            for (Pickup pickup : pickups) {
                if (moved >= maximum) {
                    break;
                }
                ItemStack template = pickup.template();
                int amount = (int) Math.min(template.getCount(), maximum - moved);
                moved += IdeaStorageResourceTransfers.depositItems(storage, pickup.handler(),
                        stack -> ItemStack.isSameItemSameComponents(template, stack), amount);
            }
        } catch (RuntimeException failure) {
            // What already moved stays in the storage and still counts as the latest transfer.
            IdeaStorageLastTransfer.record(context, moved);
            LOGGER.warn("Ideaspace item collection failed after {} items", moved, failure);
            throw new SpellRuntimeException(ERROR_COLLECT);
        }
        IdeaStorageLastTransfer.record(context, moved);
        return null;
    }

    private static Iterable<SpellItemValue> values(@Nullable Object value) {
        if (value instanceof SpellItemValue single) {
            return List.of(single);
        }
        return value instanceof SpellItemListWrapper list ? list : List.of();
    }

    /** A one-slot view of the source, or null when the source cannot be collected from and is skipped. */
    @Nullable
    private static IItemHandler resolve(SpellContext context, ServerPlayer player, ItemSourceRef source)
            throws SpellRuntimeException {
        ServerLevel level = player.serverLevel();
        if (source instanceof ItemEntitySource dropped) {
            Entity entity = level.getEntity(dropped.entityId());
            if (!(entity instanceof ItemEntity itemEntity) || !itemEntity.isAlive()
                    || !itemEntity.getUUID().equals(dropped.entityUuid())) {
                return null;
            }
            if (!context.isInRadius(itemEntity)) {
                throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
            }
            // Only what the caster could pick up now: no pickup delay, and not reserved for another player.
            UUID reservedFor = itemEntity.getTarget();
            if (itemEntity.hasPickUpDelay() || reservedFor != null && !reservedFor.equals(player.getUUID())) {
                return null;
            }
            return new DroppedItemView(itemEntity);
        }

        if (source instanceof EntityInventorySlotSource slotSource) {
            if (!slotSource.entityUuid().equals(player.getUUID()) || player instanceof FakePlayer
                    || player.isSpectator() || !player.isAlive()) {
                return null;
            }
            int slot = slotSource.slot();
            IItemHandler handler = PieceTrickIdeaStorageSlotBase.slotHandler(player.getInventory(), slot);
            // Armor slots have no handler; the CAD in use and the casting tool stay where they are.
            if (handler == null || PieceTrickIdeaStorageSlotBase.isCasterEquipment(context, player, slot,
                    player.getInventory().getItem(slot))) {
                return null;
            }
            return handler;
        }

        if (source instanceof BlockInventorySlotSource blockSource) {
            BlockPos pos = blockSource.pos();
            if (!level.dimension().equals(blockSource.dimension()) || !level.isLoaded(pos)) {
                return null;
            }
            if (!context.isInRadius(new Vector3(pos.getX(), pos.getY(), pos.getZ()))) {
                throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
            }
            if (!level.mayInteract(player, pos)
                    || PieceTrickItemTransferBase.isProtectedFromInteraction(player, pos, Direction.UP)) {
                throw new SpellRuntimeException(ERROR_ACCESS_DENIED);
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            // The caster's own connector is the storage itself.
            if (blockEntity instanceof IdeaspaceConnectorBlockEntity connector && player.getUUID().equals(connector.owner())) {
                return null;
            }
            // Same lookup as Selector: Internal Items, so the slot numbers agree.
            IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(level, pos, level.getBlockState(pos),
                    blockEntity, null);
            if (handler == null && blockEntity instanceof Container container) {
                handler = new InvWrapper(container);
            }
            int slot = blockSource.slot();
            return handler == null || slot < 0 || slot >= handler.getSlots() ? null : new SlotView(handler, slot);
        }

        // Hands and equipment of other living entities.
        return null;
    }

    private record Pickup(IItemHandler handler, ItemStack template) {
    }

    /** One slot of a block's handler; only extraction is used. */
    private record SlotView(IItemHandler handler, int slot) implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int index, int amount, boolean simulate) {
            return handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int index) {
            return handler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int index, ItemStack stack) {
            return false;
        }
    }

    /** A dropped item as one slot: extraction shrinks its stack, and the entity is removed once it is empty. */
    private record DroppedItemView(ItemEntity entity) implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return entity.getItem();
        }

        @Override
        public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int index, int amount, boolean simulate) {
            ItemStack current = entity.getItem();
            if (amount <= 0 || current.isEmpty() || !entity.isAlive()) {
                return ItemStack.EMPTY;
            }
            int count = Math.min(amount, current.getCount());
            ItemStack extracted = current.copyWithCount(count);
            if (!simulate) {
                if (count >= current.getCount()) {
                    entity.discard();
                } else {
                    // A new stack instance, so the smaller count is synced to clients.
                    entity.setItem(current.copyWithCount(current.getCount() - count));
                }
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int index) {
            return entity.getItem().getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int index, ItemStack stack) {
            return false;
        }
    }
}
