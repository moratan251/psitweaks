package com.moratan251.psitweaks.common.spells;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
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

public class PieceTrickFreezeBlock extends PieceTrick {

    private static final int POTENCY = 80;
    private static final int COST = 250;

    private SpellParam<Vector3> position;

    public PieceTrickFreezeBlock(Spell spell) {
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
        if (!level.isLoaded(pos)) {
            return null;
        }
        if (!level.mayInteract(context.caster, pos)) {
            return null;
        }

        BlockState newState = getFrozenState(level.getBlockState(pos));
        if (newState == null) {
            return null;
        }

        level.setBlockAndUpdate(pos, newState);
        return null;
    }

    private static BlockState getFrozenState(BlockState state) {
        if (state.is(Blocks.WATER)) {
            return Blocks.ICE.defaultBlockState();
        }
        if (state.is(Blocks.ICE)) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }
        if (state.is(Blocks.PACKED_ICE)) {
            return Blocks.BLUE_ICE.defaultBlockState();
        }
        if (state.is(Blocks.LAVA)) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            return Blocks.OBSIDIAN.defaultBlockState();
        }
        return null;
    }
}
