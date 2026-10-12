package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.compat.IdeaStorageLogisticsMekanism;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.mode.IdeaStorageTransferModes;
import com.moratan251.psitweaks.common.spells.mode.ModeConfigurableSpellPiece;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.IdeaStorageLastTransfer;
import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageResourceTransfers;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import org.slf4j.Logger;

/**
 * Block logistics of the ideaspace. Every subclass offers a transfer mode: partial (the original behaviour, moves
 * what it can) or exact (moves the full quantity of one resource type, or nothing).
 */
public abstract class PieceTrickIdeaStorageResourceBase extends PieceTrick implements ModeConfigurableSpellPiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ERROR_TRANSFER = "psitweaks.spellerror.idea_storage_transfer_failed";
    protected SpellParam<Vector3> position, direction;
    protected SpellParam<Number> quantity;
    protected SpellParam<String> string;
    private boolean exactTransfer;

    protected PieceTrickIdeaStorageResourceBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(50));
        setStatLabel(EnumSpellStat.COST, new StatLabel(100));
    }

    protected abstract Kind kind();
    protected abstract boolean deposit();

    /** Slot tricks keep their original filter-only modes. */
    protected boolean supportsExactTransfer() { return true; }

    public final boolean isExactTransfer() { return exactTransfer && supportsExactTransfer(); }

    protected final void setExactTransfer(boolean exact) { exactTransfer = exact && supportsExactTransfer(); }

    @Override public List<PsitweaksModeOption> getAvailableModeOptions() { return IdeaStorageTransferModes.TRANSFER_ONLY; }

    @Override public PsitweaksModeOption getModeOption() {
        return isExactTransfer() ? IdeaStorageTransferModes.EXACT : IdeaStorageTransferModes.PARTIAL;
    }

    /** Only a flag changes; the parameters and their connections stay untouched. */
    @Override public void setModeOption(PsitweaksModeOption option) {
        setExactTransfer(IdeaStorageTransferModes.isExact(normalizeModeOption(option)));
    }

    /** Badge drawn over the piece in the programmer, or null for none (partial fluid/chemical keep the plain icon). */
    public PsitweaksModeOption overlayModeOption() {
        return isExactTransfer() ? IdeaStorageTransferModes.EXACT : null;
    }

    @Override public void readFromNBT(CompoundTag tag) {
        setExactTransfer(IdeaStorageTransferModes.readExact(tag));
        super.readFromNBT(tag);
    }

    @Override public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        IdeaStorageTransferModes.writeExact(tag, isExactTransfer());
    }

    @Override public void initParams() {
        initEndpointParams();
        addParam(quantity = new ParamNumber(kind() == Kind.ITEM ? "psi.spellparam.number" : "psi.spellparam.power", SpellParam.RED, false, false));
        initFilter();
    }

    protected void initEndpointParams() {
        addParam(position = new ParamVector("psi.spellparam.position", SpellParam.BLUE, false, false));
        addParam(direction = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, false, false));
    }

    protected void initFilter() {
        addParam(string = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, true, false));
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
        if (!level.hasChunkAt(blockPos)) return null;
        Direction facing = Direction.getNearest(face.x, face.y, face.z);
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
            var ids = idFilter(getParamValue(context, string));
            if (handler == null) return 0;
            Predicate<FluidStack> filter = stack -> ids.test(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
            int amount = (int) maximum;
            if (deposit()) return exact ? IdeaStorageResourceTransfers.depositFluidsExact(storage, handler, filter, amount)
                    : IdeaStorageResourceTransfers.depositFluids(storage, handler, filter, amount);
            return exact ? IdeaStorageResourceTransfers.withdrawFluidsExact(storage, handler, filter, amount)
                    : IdeaStorageResourceTransfers.withdrawFluids(storage, handler, filter, amount);
        } else if (MekanismCompat.isMekanismLoaded()) {
            var ids = idFilter(getParamValue(context, string));
            return exact ? IdeaStorageLogisticsMekanism.transferExact(storage, level, blockPos, facing, ids, maximum, deposit())
                    : IdeaStorageLogisticsMekanism.transfer(storage, level, blockPos, facing, ids, maximum, deposit());
        }
        return 0;
    }

    protected Predicate<ItemStack> itemFilter(SpellContext context) throws SpellRuntimeException {
        var filter = idFilter(getParamValue(context, string));
        return stack -> filter.test(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static Predicate<ResourceLocation> idFilter(String pattern) {
        if (pattern == null || pattern.isEmpty()) return id -> true;
        var matcher = WildcardStringMatcher.compile(pattern);
        return id -> matcher.matches(pattern.indexOf(':') >= 0 ? id.toString() : id.getPath());
    }

    private static boolean finite(Vector3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }
}
