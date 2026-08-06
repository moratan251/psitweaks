package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellHelpers;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;

abstract class PieceOperatorRaycastBase extends PieceOperator {
    private final RaycastHelper.Mode mode;
    private final boolean axis;

    private SpellParam<Vector3> origin;
    private SpellParam<Vector3> ray;
    private SpellParam<Number> max;

    protected PieceOperatorRaycastBase(Spell spell, RaycastHelper.Mode mode, boolean axis) {
        super(spell);
        this.mode = mode;
        this.axis = axis;
    }

    @Override
    public void initParams() {
        addParam(origin = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(ray = new ParamVector(SpellParam.GENERIC_NAME_RAY, SpellParam.GREEN, false, false));
        addParam(max = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.PURPLE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 originValue = getParamValue(context, origin);
        Vector3 rayValue = getParamValue(context, ray);
        if (originValue == null || rayValue == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        double maxLength = SpellHelpers.rangeLimitParam(this, context, max, SpellContext.MAX_DISTANCE);
        BlockHitResult hit = RaycastHelper.raycast(context.caster, originValue, rayValue, maxLength, mode);
        if (hit.getType() == HitResult.Type.MISS) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        return axis ? Vector3.fromDirection(hit.getDirection()) : Vector3.fromBlockPos(hit.getBlockPos());
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

