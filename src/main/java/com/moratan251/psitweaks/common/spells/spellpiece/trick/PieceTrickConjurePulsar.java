package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.blocks.PsitweaksBlocks;
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
import vazkii.psi.common.block.BlockConjured;

public class PieceTrickConjurePulsar extends PieceTrick {
    private static final int POTENCY = 20;
    private static final int COST = 30;
    private static final int COMPLEXITY = 2;

    private SpellParam<Vector3> position;
    private SpellParam<Number> time;

    public PieceTrickConjurePulsar(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(COMPLEXITY));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.RED, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
        meta.addStat(EnumSpellStat.COMPLEXITY, COMPLEXITY);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionValue = getParamValue(context, position);
        Number timeValue = getParamValue(context, time);
        if (positionValue == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionValue)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Level level = context.focalPoint.getCommandSenderWorld();
        BlockPos pos = positionValue.toBlockPos();
        PulsarConjurationHelper.conjure(context, level, pos, timeValue, pulsarState());
        return null;
    }

    static BlockState pulsarState() {
        return PsitweaksBlocks.CONJURED_PULSAR.get().defaultBlockState()
                .setValue(BlockConjured.SOLID, true)
                .setValue(BlockConjured.LIGHT, false);
    }
}

