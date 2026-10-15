package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.api.value.BlockValue;
import com.moratan251.psitweaks.api.value.BlockValueHelper;
import com.moratan251.psitweaks.common.compat.MekanismCompat;
import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.mode.BlockResourceModes;
import com.moratan251.psitweaks.common.spells.param.ParamBlockValue;
import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.PieceTrickItemTransferBase;
import com.moratan251.psitweaks.common.storage.connector.ConnectorCapabilities;
import com.moratan251.psitweaks.common.tile.IdeaspaceConnectorBlockEntity;
import com.moratan251.psitweaksqol.api.PsitweaksModeConfigurable;
import com.moratan251.psitweaksqol.api.PsitweaksModeOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

/**
 * Reads items, fluids, chemicals or FE of a block through the side given by Direction. Unconnected, the block's
 * side-less view is read; a block that exposes faces only, such as the Ideaspace Connector, reads as its fullest face.
 * Like the ideaspace logistics tricks it asks protection mods first; a block without the capability, an unloaded chunk
 * or another dimension reads as 0.
 */
public abstract class PieceOperatorBlockResourceBase extends PieceOperator implements PsitweaksModeConfigurable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_MODE = "psitweaksMode";
    public static final String ERROR_READ = "psitweaks.spellerror.block_resource_read_failed";
    private static final String ERROR_ACCESS_DENIED = "psitweaks.spellerror.accessdenyed";

    private PsitweaksModeOption mode = BlockResourceModes.ITEM;
    private SpellParam<?> target;
    private SpellParam<Vector3> direction;
    private SpellParam<String> resource;

    protected PieceOperatorBlockResourceBase(Spell spell) {
        super(spell);
    }

    /** Whether the String input may stay unconnected (amounts read every resource then). */
    protected abstract boolean resourceOptional();

    /** {@code pooled}: every slot and tank is a view of one shared storage, as on the Ideaspace Connector. */
    protected abstract long readItems(IItemHandler handler, @Nullable String resource, boolean pooled);

    protected abstract long readFluids(IFluidHandler handler, @Nullable String resource);

    /** Called only while Mekanism is loaded; empty when the side has no chemical handler. */
    protected abstract OptionalLong readChemicals(ServerLevel level, BlockPos pos, @Nullable Direction side,
                                                  @Nullable String resource, boolean pooled);

    protected abstract long readEnergy(IEnergyStorage storage, boolean pooled);

    @Override
    public void initParams() {
        rebuildParams(null);
    }

    @Override
    public List<PsitweaksModeOption> getAvailableModeOptions() {
        return BlockResourceModes.ALL;
    }

    @Override
    public PsitweaksModeOption getModeOption() {
        return normalizeModeOption(mode);
    }

    @Override
    public void setModeOption(PsitweaksModeOption modeOption) {
        PsitweaksModeOption nextMode = normalizeModeOption(modeOption);
        if (nextMode == getModeOption()) {
            return;
        }
        Map<String, SpellParam.Side> savedSides = new LinkedHashMap<>();
        paramSides.forEach((param, side) -> savedSides.put(param.name, side));
        mode = nextMode;
        rebuildParams(savedSides);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    @Override
    public void drawAdditional(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        com.moratan251.psitweaks.client.spells.ModeOverlayRenderer.drawModeOverlay(pose, buffers, light, getModeOption());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        mode = BlockResourceModes.bySerializedId(tag.getString(TAG_MODE));
        rebuildParams(null);
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putString(TAG_MODE, getModeOption().serializedId());
    }

    private void rebuildParams(Map<String, SpellParam.Side> savedSides) {
        params.clear();
        paramSides.clear();
        addParam(target = new ParamBlockValue(SpellParam.GENERIC_NAME_TARGET, PsitweaksSpellParams.BLOCK_COLOR, false, false));
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, true, false));
        resource = null;
        if (getModeOption() != BlockResourceModes.ENERGY) {
            addParam(resource = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR,
                    resourceOptional(), false));
        }
        if (savedSides != null) {
            for (SpellParam<?> param : params.values()) {
                SpellParam.Side side = savedSides.get(param.name);
                if (side != null) {
                    paramSides.put(param, side);
                }
            }
        }
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockValue block = BlockValueHelper.getBlockValue(this, context, target);
        Vector3 face = getParamValue(context, direction);
        String resourceVal = resource == null ? null : getParamValue(context, resource);
        if (!(context.caster instanceof ServerPlayer player)) {
            return 0.0D;
        }
        ServerLevel level = player.serverLevel();
        BlockPos pos = block.blockPos();
        if (!level.dimension().equals(block.dimension()) || !level.isLoaded(pos)) {
            return 0.0D;
        }
        Direction side = face == null || face.isZero() ? null : Direction.getNearest(face.x, face.y, face.z);
        if (!level.mayInteract(player, pos)
                || PieceTrickItemTransferBase.isProtectedFromInteraction(player, pos, side == null ? Direction.UP : side)) {
            throw new SpellRuntimeException(ERROR_ACCESS_DENIED);
        }
        try {
            return (double) read(level, pos, side, resourceVal);
        } catch (RuntimeException failure) {
            LOGGER.warn("Reading {} of the block at {} face {} failed", getModeOption().serializedId(), pos, side, failure);
            throw new SpellRuntimeException(ERROR_READ);
        }
    }

    private long read(ServerLevel level, BlockPos pos, @Nullable Direction side, @Nullable String resourceVal) {
        boolean pooled = level.getBlockEntity(pos) instanceof IdeaspaceConnectorBlockEntity;
        OptionalLong reading = readSide(level, pos, side, resourceVal, pooled);
        if (reading.isPresent() || side != null) {
            return reading.orElse(0);
        }
        // Faces may each show only part of the block (a disabled face shows nothing), so take the fullest one.
        long fullest = 0;
        for (Direction face : Direction.values()) {
            fullest = Math.max(fullest, readSide(level, pos, face, resourceVal, pooled).orElse(0));
        }
        return fullest;
    }

    /** The reading through one side (null: the side-less view), or empty when the block has no handler there. */
    private OptionalLong readSide(ServerLevel level, BlockPos pos, @Nullable Direction side, @Nullable String resourceVal,
                                  boolean pooled) {
        PsitweaksModeOption current = getModeOption();
        if (current == BlockResourceModes.FLUID) {
            IFluidHandler handler = ConnectorCapabilities.get(level, pos, side, ForgeCapabilities.FLUID_HANDLER);
            return handler == null ? OptionalLong.empty() : OptionalLong.of(readFluids(handler, resourceVal));
        }
        if (current == BlockResourceModes.CHEMICAL) {
            return MekanismCompat.isMekanismLoaded()
                    ? readChemicals(level, pos, side, resourceVal, pooled) : OptionalLong.of(0);
        }
        if (current == BlockResourceModes.ENERGY) {
            IEnergyStorage storage = ConnectorCapabilities.get(level, pos, side, ForgeCapabilities.ENERGY);
            return storage == null ? OptionalLong.empty() : OptionalLong.of(readEnergy(storage, pooled));
        }
        IItemHandler handler = itemHandler(level, pos, side);
        return handler == null ? OptionalLong.empty() : OptionalLong.of(readItems(handler, resourceVal, pooled));
    }

    /** Without a side, plain containers are read whole (a sided wrapper needs a face). */
    @Nullable
    private static IItemHandler itemHandler(ServerLevel level, BlockPos pos, @Nullable Direction side) {
        if (side != null) {
            return PieceTrickItemTransferBase.getBlockItemHandler(level, pos, side);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler == null && blockEntity instanceof Container container) {
            return new InvWrapper(container);
        }
        return handler;
    }

    protected static long saturatedAdd(long total, long amount) {
        return amount > Long.MAX_VALUE - total ? Long.MAX_VALUE : total + amount;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
