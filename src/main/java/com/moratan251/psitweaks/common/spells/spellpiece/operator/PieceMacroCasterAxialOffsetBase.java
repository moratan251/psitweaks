package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import net.minecraft.network.chat.Component;
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
import vazkii.psi.api.spell.piece.PieceOperator;

abstract class PieceMacroCasterAxialOffsetBase extends PieceOperator {

    private final boolean includePitch;

    private SpellParam<Vector3> position;
    private SpellParam<Number> leftRight;
    private SpellParam<Number> forwardBackward;
    private SpellParam<Number> upDown;

    protected PieceMacroCasterAxialOffsetBase(Spell spell, boolean includePitch) {
        super(spell);
        this.includePitch = includePitch;
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(4));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(leftRight = new ParamNumber(PsitweaksSpellParams.LEFT_RIGHT, SpellParam.RED, true, false));
        addParam(forwardBackward = new ParamNumber(PsitweaksSpellParams.FORWARD_BACKWARD, SpellParam.GREEN, true, false));
        addParam(upDown = new ParamNumber(PsitweaksSpellParams.UP_DOWN, SpellParam.PURPLE, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 4);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        double leftRightVal = numberOrZero(getParamValue(context, leftRight));
        double forwardBackwardVal = numberOrZero(getParamValue(context, forwardBackward));
        double upDownVal = numberOrZero(getParamValue(context, upDown));

        CasterAxialBasis basis = includePitch
                ? CasterAxialBasis.of3D(context.caster)
                : CasterAxialBasis.of2D(context.caster);

        Vector3 result = positionVal.copy();
        if (leftRightVal != 0) {
            result.add(Vector3.fromDirection(basis.right).multiply(leftRightVal));
        }
        if (forwardBackwardVal != 0) {
            result.add(Vector3.fromDirection(basis.forward).multiply(forwardBackwardVal));
        }
        if (upDownVal != 0) {
            result.add(Vector3.fromDirection(basis.up).multiply(upDownVal));
        }
        return result;
    }

    private static double numberOrZero(Number value) {
        return value == null ? 0.0D : value.doubleValue();
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.vector3");
    }
}
