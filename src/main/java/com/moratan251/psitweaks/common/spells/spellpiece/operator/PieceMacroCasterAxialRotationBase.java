package com.moratan251.psitweaks.common.spells.spellpiece.operator;

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
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

abstract class PieceMacroCasterAxialRotationBase extends PieceOperator {
    private final boolean includePitch;
    private SpellParam<Vector3> direction;

    protected PieceMacroCasterAxialRotationBase(Spell spell, boolean includePitch) {
        super(spell);
        this.includePitch = includePitch;
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(4.0D));
    }

    @Override
    public void initParams() {
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 4);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 directionValue = getParamValue(context, direction);
        if (directionValue == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        CasterAxialBasis basis = includePitch
                ? CasterAxialBasis.of3D(context.caster)
                : CasterAxialBasis.of2D(context.caster);

        Vector3 result = new Vector3(0, 0, 0);
        if (directionValue.x != 0) {
            result.add(Vector3.fromDirection(basis.right).multiply(directionValue.x));
        }
        if (directionValue.y != 0) {
            result.add(Vector3.fromDirection(basis.up).multiply(directionValue.y));
        }
        if (directionValue.z != 0) {
            result.add(Vector3.fromDirection(basis.forward).multiply(directionValue.z));
        }
        return result;
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

