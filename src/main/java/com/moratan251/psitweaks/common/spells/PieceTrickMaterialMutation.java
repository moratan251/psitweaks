package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.handler.MaterialMutationRecipeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickMaterialMutation extends PieceTrick {

    private static final int POTENCY = 1250;
    private static final int COST = 10000;

    private SpellParam<Vector3> position;

    public PieceTrickMaterialMutation(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }

        BlockPos pos = positionVal.toBlockPos();
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        if (!level.mayInteract(context.caster, pos)) {
            return null;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return null;
        }
        if (state.getDestroySpeed(level, pos) == -1) {
            return null;
        }

        ItemStack mutationOutput = MaterialMutationRecipeHandler.getMutationOutput(state.getBlock());
        if (mutationOutput.isEmpty()) {
            return null;
        }

        if (context.caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundLevelEventPacket(
                    LevelEvent.PARTICLES_DESTROY_BLOCK,
                    pos,
                    Block.getId(state),
                    false
            ));
        }

        if (!level.destroyBlock(pos, false, context.caster)) {
            return null;
        }

        dropResultStack(level, pos, mutationOutput);
        return null;
    }

    private static void dropResultStack(ServerLevel level, BlockPos pos, ItemStack resultStack) {
        int remaining = resultStack.getCount();
        int maxStackSize = resultStack.getMaxStackSize();
        while (remaining > 0) {
            int dropCount = Math.min(maxStackSize, remaining);
            ItemStack drop = resultStack.copy();
            drop.setCount(dropCount);
            Block.popResource(level, pos, drop);
            remaining -= dropCount;
        }
    }
}
