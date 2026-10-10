package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.common.compat.IdeaStorageLogisticsMekanism;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import com.moratan251.psitweaks.common.storage.connector.ConnectorCapabilities;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageResourceTransfers;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.storage.idea.PlayerIdeaStorage;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.slf4j.Logger;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public abstract class PieceTrickIdeaStorageResourceBase extends PieceTrick {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ERROR_TRANSFER = "psitweaks.spellerror.idea_storage_transfer_failed";
    protected SpellParam<Vector3> position, direction;
    protected SpellParam<Number> quantity;
    protected SpellParam<String> string;

    protected PieceTrickIdeaStorageResourceBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(50));
        setStatLabel(EnumSpellStat.COST, new StatLabel(100));
    }

    protected abstract Kind kind();
    protected abstract boolean deposit();

    @Override public void initParams() {
        initEndpointParams();
        addParam(quantity = new ParamNumber(kind() == Kind.ITEM ? SpellParam.GENERIC_NAME_NUMBER : SpellParam.GENERIC_NAME_POWER,
                SpellParam.RED, false, false));
        initFilter();
    }

    protected void initEndpointParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, false, false));
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
        Vector3 pos = getNonnullParamValue(context, position), face = getNonnullParamValue(context, direction);
        double amount = this.<Number>getNonnullParamValue(context, quantity).doubleValue();
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
        try {
            transfer(context, player, blockPos, facing, storage, maximum);
        } catch (RuntimeException failure) {
            LOGGER.warn("Ideaspace {} {} failed at {} face {}", kind(), deposit() ? "deposit" : "withdrawal", blockPos, facing, failure);
            throw new SpellRuntimeException(ERROR_TRANSFER);
        }
        return null;
    }

    protected void transfer(SpellContext context, ServerPlayer player, BlockPos blockPos, Direction facing,
                            PlayerIdeaStorage storage, long maximum) throws SpellRuntimeException {
        var level = player.serverLevel();
        if (kind() == Kind.ITEM) {
            var handler = PieceTrickItemTransferBase.getBlockItemHandler(level, blockPos, facing);
            if (handler != null) {
                var filter = itemFilter(context);
                if (deposit()) IdeaStorageResourceTransfers.depositItems(storage, handler, filter, (int) maximum);
                else IdeaStorageResourceTransfers.withdrawItems(storage, handler, filter, (int) maximum);
            }
        } else if (kind() == Kind.FLUID) {
            var handler = ConnectorCapabilities.get(level, blockPos, facing, ForgeCapabilities.FLUID_HANDLER);
            var filter = idFilter(getParamValue(context, string));
            if (handler != null) {
                if (deposit()) IdeaStorageResourceTransfers.depositFluids(storage, handler, stack -> filter.test(BuiltInRegistries.FLUID.getKey(stack.getFluid())), (int) maximum);
                else IdeaStorageResourceTransfers.withdrawFluids(storage, handler, stack -> filter.test(BuiltInRegistries.FLUID.getKey(stack.getFluid())), (int) maximum);
            }
        } else if (MekanismCompat.isMekanismLoaded()) {
            IdeaStorageLogisticsMekanism.transfer(storage, level, blockPos, facing, idFilter(getParamValue(context, string)), maximum, deposit());
        }
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
