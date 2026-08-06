package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.spellpiece.operator.RaycastHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellHelpers;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;

abstract class PieceMacroCasterRaycastBase extends SpellPiece {
    private final RaycastHelper.Mode mode;
    private final boolean axis;
    private SpellParam<Number> maxDistance;

    protected PieceMacroCasterRaycastBase(Spell spell, RaycastHelper.Mode mode, boolean axis) {
        super(spell);
        this.mode = mode;
        this.axis = axis;
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(4.0D));
    }

    @Override
    public void initParams() {
        addParam(maxDistance = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 4);
    }

    @Override
    public EnumPieceType getPieceType() {
        return EnumPieceType.SELECTOR;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.vector3");
    }

    @Override
    public Object evaluate() {
        return null;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 origin = Vector3.fromEntity(context.caster).add(0, context.caster.getEyeHeight(), 0);
        Vector3 look = new Vector3(context.caster.getLookAngle());
        double maxLength = SpellHelpers.rangeLimitParam(this, context, maxDistance, SpellContext.MAX_DISTANCE);

        BlockHitResult hit = RaycastHelper.raycast(context.caster, origin, look, maxLength, mode);
        if (hit.getType() == HitResult.Type.MISS) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        return axis ? Vector3.fromDirection(hit.getDirection()) : Vector3.fromBlockPos(hit.getBlockPos());
    }
}

