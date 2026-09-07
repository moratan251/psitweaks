package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickConjurePulsarSequence extends PieceTrick {
    private static final int POTENCY_PER_BLOCK = 20;
    private static final int COST_PER_BLOCK = 30;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> target;
    private SpellParam<Number> maxBlocks;
    private SpellParam<Number> time;

    public PieceTrickConjurePulsarSequence(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY,
                new StatLabel(SpellParam.GENERIC_NAME_MAX, true).mul(POTENCY_PER_BLOCK));
        setStatLabel(EnumSpellStat.COST,
                new StatLabel(SpellParam.GENERIC_NAME_MAX, true).mul(COST_PER_BLOCK));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.GREEN, false, false));
        addParam(maxBlocks = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, false, true));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.PURPLE, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Object evaluation = getParamEvaluation(maxBlocks);
        if (!(evaluation instanceof Number number) || number.doubleValue() <= 0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        meta.addStat(EnumSpellStat.POTENCY, (int) (number.doubleValue() * POTENCY_PER_BLOCK));
        meta.addStat(EnumSpellStat.COST, (int) (number.doubleValue() * COST_PER_BLOCK));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionValue = getParamValue(context, position);
        Vector3 targetValue = getParamValue(context, target);
        Number maxBlocksValue = getParamValue(context, maxBlocks);
        Number timeValue = getParamValue(context, time);
        if (positionValue == null || targetValue == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (maxBlocksValue == null || maxBlocksValue.doubleValue() <= 0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }

        int length = (int) Math.min(targetValue.mag(), maxBlocksValue.doubleValue());
        if (length <= 0) {
            return null;
        }
        Vector3 direction = targetValue.copy().normalize();
        Level level = context.focalPoint.getCommandSenderWorld();
        BlockState state = PieceTrickConjurePulsar.pulsarState();
        for (int i = 0; i < length; i++) {
            Vector3 blockVector = positionValue.copy().add(direction.copy().multiply(i));
            BlockPos pos = blockVector.toBlockPos();
            if (!context.isInRadius(Vector3.fromBlockPos(pos))) {
                throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
            }
            PulsarConjurationHelper.conjure(context, level, pos, timeValue, state);
        }
        return null;
    }
}

