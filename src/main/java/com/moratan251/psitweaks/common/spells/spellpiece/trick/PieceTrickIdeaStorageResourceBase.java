package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.compat.IdeaStorageLogisticsMekanism;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.WildcardStringMatcher;
import com.moratan251.psitweaks.common.storage.connector.ConnectorResource.Kind;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageResourceTransfers;
import com.moratan251.psitweaks.common.storage.idea.IdeaStorageService;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import java.util.function.Predicate;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public abstract class PieceTrickIdeaStorageResourceBase extends PieceTrick {
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
        addParam(position = new ParamVector("psi.spellparam.position", SpellParam.BLUE, false, false));
        addParam(direction = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, false, false));
        addParam(quantity = new ParamNumber(kind() == Kind.ITEM ? "psi.spellparam.number" : "psi.spellparam.power", SpellParam.RED, false, false));
        initFilter();
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
        if (kind() == Kind.ITEM) {
            var handler = PieceTrickItemTransferBase.getBlockItemHandler(level, blockPos, facing);
            if (handler != null) {
                var filter = itemFilter(context);
                if (deposit()) IdeaStorageResourceTransfers.depositItems(storage, handler, filter, (int) maximum);
                else IdeaStorageResourceTransfers.withdrawItems(storage, handler, filter, (int) maximum);
            }
        } else if (kind() == Kind.FLUID) {
            var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos, facing);
            var filter = idFilter(getParamValue(context, string));
            if (handler != null) {
                if (deposit()) IdeaStorageResourceTransfers.depositFluids(storage, handler, stack -> filter.test(BuiltInRegistries.FLUID.getKey(stack.getFluid())), (int) maximum);
                else IdeaStorageResourceTransfers.withdrawFluids(storage, handler, stack -> filter.test(BuiltInRegistries.FLUID.getKey(stack.getFluid())), (int) maximum);
            }
        } else if (MekanismCompat.isMekanismLoaded()) {
            IdeaStorageLogisticsMekanism.transfer(storage, level, blockPos, facing, idFilter(getParamValue(context, string)), maximum, deposit());
        }
        return null;
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
