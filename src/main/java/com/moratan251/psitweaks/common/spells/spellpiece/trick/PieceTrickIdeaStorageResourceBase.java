package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.compat.IdeaStorageLogisticsMekanism;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.item.SpellItemFilters;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageResourceModes;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaks.common.spells.param.ParamItemSearch;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.IdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageEnergyTransfer;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageResourceTransfers;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Block logistics of the ideaspace. The mode picks the resource kind and, for items, fluids and chemicals, the transfer
 * mode: partial (moves what it can) or exact (moves the full quantity of one resource type, or nothing). FE has no
 * exact transfer. Position, direction and quantity keep their wires across modes, and so does the filter between
 * the kinds that have one.
 */
public abstract class PieceTrickIdeaStorageResourceBase extends PieceTrick implements ModeConfigurableSpellPiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ERROR_TRANSFER = "psitweaks.spellerror.idea_storage_transfer_failed";
    public static final String TAG_MODE = "psitweaksMode";
    protected SpellParam<Vector3> position, direction;
    protected SpellParam<Number> quantity;
    /** A String filter for fluids and chemicals, an item search for items; null in FE mode. */
    protected SpellParam<?> filterInput;
    private PsitweaksModeOption mode = BlockResourceModes.ITEM;

    protected PieceTrickIdeaStorageResourceBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(50));
        setStatLabel(EnumSpellStat.COST, new StatLabel(100));
    }

    protected abstract boolean deposit();

    @Override public List<PsitweaksModeOption> getAvailableModeOptions() { return IdeaStorageResourceModes.TRANSFER; }

    @Override public PsitweaksModeOption getModeOption() { return normalizeModeOption(mode); }

    public final boolean isExactTransfer() { return IdeaStorageResourceModes.isExact(getModeOption()); }

    public final Kind kind() {
        return kindOf(mode == null ? BlockResourceModes.ITEM : getModeOption());
    }

    private static Kind kindOf(PsitweaksModeOption option) {
        PsitweaksModeOption kind = IdeaStorageResourceModes.kindOf(option);
        if (kind == BlockResourceModes.FLUID) return Kind.FLUID;
        if (kind == BlockResourceModes.CHEMICAL) return Kind.CHEMICAL;
        if (kind == BlockResourceModes.ENERGY) return Kind.ENERGY;
        return Kind.ITEM;
    }

    /** Switching only partial/exact keeps the parameters; switching the kind rebuilds them and moves the wires over. */
    @Override public void setModeOption(PsitweaksModeOption option) {
        PsitweaksModeOption next = normalizeModeOption(option);
        if (kindOf(next) == kind()) {
            mode = next;
            return;
        }
        var posSide = paramSides.get(position);
        var directionSide = paramSides.get(direction);
        var quantitySide = paramSides.get(quantity);
        var filterSide = filterInput == null ? null : paramSides.get(filterInput);
        mode = next;
        params.clear();
        paramSides.clear();
        initParams();
        if (posSide != null) paramSides.put(position, posSide);
        if (directionSide != null) paramSides.put(direction, directionSide);
        if (quantitySide != null) paramSides.put(quantity, quantitySide);
        if (filterSide != null && filterInput != null) paramSides.put(filterInput, filterSide);
    }

    @Override public void readFromNBT(CompoundTag tag) {
        mode = IdeaStorageResourceModes.bySerializedId(IdeaStorageResourceModes.TRANSFER, tag.getString(TAG_MODE));
        params.clear();
        paramSides.clear();
        initParams();
        super.readFromNBT(tag);
    }

    @Override public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    @Override public void initParams() {
        // Runs during construction too, before mode is assigned.
        Kind kind = kind();
        addParam(position = new ParamVector("psi.spellparam.position", SpellParam.BLUE, false, false));
        addParam(direction = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, false, false));
        addParam(quantity = new ParamNumber(kind == Kind.ITEM ? "psi.spellparam.number" : "psi.spellparam.power",
                SpellParam.RED, false, false));
        filterInput = null;
        if (kind == Kind.ITEM) {
            addParam(filterInput = new ParamItemSearch(PsitweaksSpellParams.FILTER, PsitweaksSpellParams.ITEM_COLOR, true, false));
        } else if (kind != Kind.ENERGY) {
            addParam(filterInput = new ParamString(PsitweaksSpellParams.FILTER, PsitweaksSpellParams.STRING_COLOR, true, false));
        }
    }

    @Override public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 50);
        meta.addStat(EnumSpellStat.COST, 100);
    }

    @Override public Object execute(SpellContext context) throws SpellRuntimeException {
        // A trick that stops early (error, no target, nothing to move) still counts as the latest transfer.
        IdeaStorageLastTransfer.record(context, 0);
        Vector3 pos = getNotNullParamValue(context, position), face = getNotNullParamValue(context, direction);
        double amount = this.<Number>getNotNullParamValue(context, quantity).doubleValue();
        if (!Double.isFinite(amount) || amount <= 0) throw new SpellRuntimeException("psi.spellerror.nonpositivevalue");
        if (!finite(pos) || !finite(face)) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        if (!context.isInRadius(pos)) throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        if (!(context.caster instanceof ServerPlayer player)) return null;
        var level = player.serverLevel();
        var blockPos = pos.toBlockPos();
        Direction facing = Direction.getNearest(face.x, face.y, face.z);
        if (kind() == Kind.ENERGY) {
            transferEnergy(context, player, blockPos, facing, amount);
            return null;
        }
        if (!level.hasChunkAt(blockPos)) return null;
        if (!level.mayInteract(player, blockPos) || PieceTrickItemTransferBase.isProtectedFromInteraction(player, blockPos, facing))
            throw new SpellRuntimeException("psitweaks.spellerror.accessdenyed");
        if (level.getBlockEntity(blockPos) instanceof IdeaspaceConnectorBlockEntity connector && player.getUUID().equals(connector.owner()))
            return null;
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        long maximum = IdeaStorageResourceTransfers.amountForPower(amount, kind() == Kind.ITEM ? 1 : 1000,
                kind() == Kind.CHEMICAL ? Long.MAX_VALUE : Integer.MAX_VALUE);
        long moved;
        try {
            moved = transfer(context, player, blockPos, facing, storage, maximum);
        } catch (RuntimeException failure) {
            LOGGER.warn("Ideaspace {} {} failed at {} face {}", kind(), deposit() ? "deposit" : "withdrawal", blockPos, facing, failure);
            throw new SpellRuntimeException(ERROR_TRANSFER);
        }
        IdeaStorageLastTransfer.record(context, moved);
        return null;
    }

    /** FE keeps the rules of the former FE Absorb/Supply tricks: Power * 4000 FE, and no effect where you may not interact. */
    private void transferEnergy(SpellContext context, ServerPlayer player, BlockPos blockPos, Direction facing, double power) {
        var level = player.serverLevel();
        if (!level.hasChunkAt(blockPos) || !level.mayInteract(player, blockPos)) return;
        var handler = level.getCapability(Capabilities.EnergyStorage.BLOCK, blockPos, facing);
        if (handler == null) return;
        var storage = IdeaStorageService.get(player.server, player.getUUID());
        int amount = IdeaStorageEnergyTransfer.amountForPower(power);
        int moved = deposit()
                ? IdeaStorageEnergyTransfer.absorb(storage, handler, amount)
                : IdeaStorageEnergyTransfer.supply(storage, handler, amount);
        IdeaStorageLastTransfer.record(context, moved);
    }

    /** Returns the amount actually moved, in items or mB. Exact transfers return either 0 or the full maximum. */
    protected long transfer(SpellContext context, ServerPlayer player, BlockPos blockPos, Direction facing,
                            PlayerIdeaStorage storage, long maximum) throws SpellRuntimeException {
        var level = player.serverLevel();
        boolean exact = isExactTransfer();
        if (kind() == Kind.ITEM) {
            var handler = PieceTrickItemTransferBase.getBlockItemHandler(level, blockPos, facing);
            if (handler == null) return 0;
            var filter = itemFilter(context);
            int amount = (int) maximum;
            if (deposit()) return exact ? IdeaStorageResourceTransfers.depositItemsExact(storage, handler, filter, amount)
                    : IdeaStorageResourceTransfers.depositItems(storage, handler, filter, amount);
            return exact ? IdeaStorageResourceTransfers.withdrawItemsExact(storage, handler, filter, amount)
                    : IdeaStorageResourceTransfers.withdrawItems(storage, handler, filter, amount);
        } else if (kind() == Kind.FLUID) {
            var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos, facing);
            var ids = SpellItemFilters.idFilter(stringFilter(context));
            if (handler == null) return 0;
            Predicate<FluidStack> filter = stack -> ids.test(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
            int amount = (int) maximum;
            if (deposit()) return exact ? IdeaStorageResourceTransfers.depositFluidsExact(storage, handler, filter, amount)
                    : IdeaStorageResourceTransfers.depositFluids(storage, handler, filter, amount);
            return exact ? IdeaStorageResourceTransfers.withdrawFluidsExact(storage, handler, filter, amount)
                    : IdeaStorageResourceTransfers.withdrawFluids(storage, handler, filter, amount);
        } else if (MekanismCompat.isMekanismLoaded()) {
            var ids = SpellItemFilters.idFilter(stringFilter(context));
            return exact ? IdeaStorageLogisticsMekanism.transferExact(storage, level, blockPos, facing, ids, maximum, deposit())
                    : IdeaStorageLogisticsMekanism.transfer(storage, level, blockPos, facing, ids, maximum, deposit());
        }
        return 0;
    }

    /** The item filter: what the search input's value picks (a String pattern, items, or a strict Item). */
    protected Predicate<ItemStack> itemFilter(SpellContext context) throws SpellRuntimeException {
        return SpellItemFilters.search(getParamValue(context, (SpellParam<?>) filterInput));
    }

    private String stringFilter(SpellContext context) throws SpellRuntimeException {
        return getParamValue(context, (SpellParam<?>) filterInput) instanceof String pattern ? pattern : null;
    }

    private static boolean finite(Vector3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }
}
